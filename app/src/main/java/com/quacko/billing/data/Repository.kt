package com.quacko.billing.data

import com.quacko.billing.data.model.AdminTotals
import com.quacko.billing.data.model.Billing
import com.quacko.billing.data.model.CallOrigin
import com.quacko.billing.data.model.CallRecord
import com.quacko.billing.data.model.ClientBillingSummary
import com.quacko.billing.data.model.Modality
import com.quacko.billing.data.model.Rating
import com.quacko.billing.data.model.ReportFilter
import com.quacko.billing.data.model.Session
import com.quacko.billing.data.model.SessionMode
import com.quacko.billing.data.model.SessionStart
import com.quacko.billing.data.model.UserRole
import org.json.JSONArray
import org.json.JSONObject

/**
 * Single source of truth for the UI. In demo mode everything is served locally;
 * otherwise it calls api.php on quack-o through [ApiClient].
 *
 * ==== api.php contract this app expects (adjust names here once confirmed) ====
 *   POST action=client_login   username, password
 *        -> { ok:true, data:{ client_id, client_name, token, role } }
 *   POST action=request_session client_id, token, modality(VRI|OPI), language
 *        -> { ok:true, data:{ session_id, mode:"room"|"dial",
 *                              room_url, dial_number, pin, interpreter } }
 *   POST action=rate_session   session_id, token, stars, comment
 *        -> { ok:true }
 *   GET  action=usage          client_id, token [, from, to]
 *        -> { ok:true, data:[ { id,date,client_id,client_name,origin,
 *                               language,interpreter,minutes,capped } ] }
 * ============================================================================
 */
class Repository(private val api: ApiClient = ApiClient()) {

    // ---- Auth -------------------------------------------------------------

    suspend fun login(username: String, password: String): Session {
        if (Config.demoMode) return demoLogin(username, password)

        val res = api.post("client_login", mapOf("username" to username, "password" to password))
        val obj = res as? JSONObject ?: throw ApiException("Bad login response")
        if (!obj.optBoolean("ok", false)) throw ApiException(obj.optString("error", "Login failed"))
        val data = obj.getJSONObject("data")
        val role = if (data.optString("role").equals("admin", true)) UserRole.ADMIN else UserRole.CLIENT
        return Session(
            username = data.optString("username", username),
            role = role,
            clientId = data.optString("client_id").ifBlank { null },
            clientName = data.optString("client_name").ifBlank { null },
            token = data.optString("token").ifBlank { null }
        )
    }

    private fun demoLogin(username: String, password: String): Session {
        val u = username.trim().lowercase()
        return when {
            u == DemoData.ADMIN_USER && password == DemoData.ADMIN_PASS ->
                Session(username, UserRole.ADMIN, null, null, token = "demo-admin")
            u == DemoData.CLIENT_USER && password == DemoData.CLIENT_PASS -> {
                val c = DemoData.clients.first { it.id == DemoData.CLIENT_ID }
                Session(username, UserRole.CLIENT, c.id, c.name, token = "demo-client")
            }
            else -> throw ApiException("Invalid credentials. Demo: client/client or admin/admin.")
        }
    }

    // ---- Sessions (VRI / OPI) --------------------------------------------

    suspend fun requestSession(session: Session, modality: Modality, language: String): SessionStart {
        if (Config.demoMode) return DemoData.demoSession(modality, language)

        val res = api.post(
            "request_session",
            mapOf(
                "client_id" to session.clientId,
                "token" to session.token,
                "modality" to modality.code,
                "language" to language
            )
        )
        val obj = res as? JSONObject ?: throw ApiException("Bad session response")
        if (!obj.optBoolean("ok", false)) throw ApiException(obj.optString("error", "Could not start session"))
        val d = obj.getJSONObject("data")
        val mode = if (d.optString("mode", "room").equals("dial", true)) SessionMode.DIAL else SessionMode.ROOM
        return SessionStart(
            sessionId = d.optString("session_id", "S-0"),
            mode = mode,
            modality = modality,
            language = language,
            roomUrl = d.optString("room_url").ifBlank { null },
            dialNumber = d.optString("dial_number").ifBlank { null },
            pin = d.optString("pin").ifBlank { null },
            interpreter = d.optString("interpreter").ifBlank { null }
        )
    }

    suspend fun submitRating(session: Session, rating: Rating): Boolean {
        if (Config.demoMode) return true
        val res = api.post(
            "rate_session",
            mapOf(
                "session_id" to rating.sessionId,
                "token" to session.token,
                "stars" to rating.stars.toString(),
                "comment" to rating.comment
            )
        )
        val obj = res as? JSONObject ?: return false
        return obj.optBoolean("ok", false)
    }

    // ---- Usage ------------------------------------------------------------

    suspend fun calls(filter: ReportFilter): List<CallRecord> {
        if (Config.demoMode) return DemoData.calls.filter { matches(it, filter) }
        val res = api.get(
            "usage",
            mapOf("client_id" to filter.clientId, "from" to filter.fromDate, "to" to filter.toDate)
        )
        return parseCalls(unwrapArray(res))
    }

    suspend fun clientSummary(clientId: String): ClientBillingSummary {
        val rows = calls(ReportFilter(clientId = clientId))
        return if (rows.isEmpty())
            ClientBillingSummary(clientId, clientId, 0, 0, 0, 0, 0, Billing.DEFAULT_RATE)
        else rollup(rows)
    }

    suspend fun summaries(filter: ReportFilter): List<ClientBillingSummary> =
        calls(filter).groupBy { it.clientId }.map { (_, rows) -> rollup(rows) }
            .sortedByDescending { it.amount }

    suspend fun adminTotals(filter: ReportFilter): AdminTotals {
        val list = calls(filter)
        val webMin = list.filter { it.origin == CallOrigin.WEB }.sumOf { it.minutes }
        val phoneMin = list.filter { it.origin == CallOrigin.PHONE_800 }.sumOf { it.minutes }
        val totalMin = webMin + phoneMin
        return AdminTotals(
            totalClients = list.map { it.clientId }.distinct().size,
            totalCalls = list.size,
            totalMinutes = totalMin,
            webMinutes = webMin,
            phoneMinutes = phoneMin,
            cappedCalls = list.count { it.capped },
            totalAmount = totalMin * Billing.DEFAULT_RATE
        )
    }

    // ---- helpers ----------------------------------------------------------

    private fun rollup(rows: List<CallRecord>): ClientBillingSummary {
        val web = rows.filter { it.origin == CallOrigin.WEB }
        val phone = rows.filter { it.origin == CallOrigin.PHONE_800 }
        val first = rows.first()
        return ClientBillingSummary(
            clientId = first.clientId,
            clientName = first.clientName,
            webCalls = web.size,
            phoneCalls = phone.size,
            webMinutes = web.sumOf { it.minutes },
            phoneMinutes = phone.sumOf { it.minutes },
            cappedCalls = rows.count { it.capped },
            ratePerMinute = Billing.DEFAULT_RATE
        )
    }

    private fun matches(c: CallRecord, f: ReportFilter): Boolean {
        if (f.clientId != null && c.clientId != f.clientId) return false
        val day = c.date.substringBefore(' ')
        if (f.fromDate != null && day < f.fromDate) return false
        if (f.toDate != null && day > f.toDate) return false
        return true
    }

    private fun unwrapArray(res: Any): JSONArray = when (res) {
        is JSONArray -> res
        is JSONObject -> res.optJSONArray("data") ?: JSONArray()
        else -> JSONArray()
    }

    private fun parseCalls(arr: JSONArray): List<CallRecord> {
        val out = ArrayList<CallRecord>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val minutes = o.optInt("minutes", 0)
            val capped = o.optBoolean("capped", minutes >= Billing.CAP_MINUTES)
            val originStr = o.optString("origin", "web")
            out.add(
                CallRecord(
                    id = o.optString("id", "CDR-$i"),
                    date = o.optString("date"),
                    clientId = o.optString("client_id"),
                    clientName = o.optString("client_name", o.optString("client_id")),
                    origin = if (originStr.equals("800", true) || originStr.equals("phone", true) || originStr.equals("opi", true))
                        CallOrigin.PHONE_800 else CallOrigin.WEB,
                    language = o.optString("language", "—"),
                    interpreter = o.optString("interpreter", "—"),
                    minutes = if (capped) minOf(minutes, Billing.CAP_MINUTES) else minutes,
                    capped = capped
                )
            )
        }
        return out
    }
}
