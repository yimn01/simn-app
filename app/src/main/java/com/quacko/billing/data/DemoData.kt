package com.quacko.billing.data

import com.quacko.billing.data.model.Billing
import com.quacko.billing.data.model.CallOrigin
import com.quacko.billing.data.model.CallRecord
import com.quacko.billing.data.model.Modality
import com.quacko.billing.data.model.SessionMode
import com.quacko.billing.data.model.SessionStart

/** Realistic offline dataset so the APK is fully explorable without a backend. */
object DemoData {

    data class DemoClient(val id: String, val name: String)

    val clients = listOf(
        DemoClient("100482", "Mercy General Hospital"),
        DemoClient("100517", "Riverside Legal Aid"),
        DemoClient("100538", "Unified School District 21"),
        DemoClient("100561", "Bexar County Courts")
    )

    private val languages = listOf(
        "Spanish", "Mandarin", "Vietnamese", "Arabic", "ASL", "Russian", "Haitian Creole"
    )
    private val interpreters = listOf(
        "amaya.r", "chen.l", "nguyen.t", "haddad.s", "morales.j", "ivanova.k", "pierre.g"
    )

    val calls: List<CallRecord> by lazy { build() }

    private fun build(): List<CallRecord> {
        val out = ArrayList<CallRecord>()
        var seq = 0
        for (day in 1..12) {
            val callsToday = 3 + (day % 3)
            for (c in 0 until callsToday) {
                val client = clients[(seq) % clients.size]
                val isPhone = (seq % 5 == 0) || (seq % 7 == 0)
                val lang = languages[(seq * 3) % languages.size]
                val interp = interpreters[(seq * 2) % interpreters.size]
                val rawMinutes = when {
                    seq % 11 == 0 -> 137 + (seq % 40)
                    else -> 4 + (seq * 7) % 58
                }
                val capped = rawMinutes > Billing.CAP_MINUTES
                val billed = if (capped) Billing.CAP_MINUTES else rawMinutes
                val hh = 8 + (seq % 9)
                val mm = (seq * 13) % 60
                out.add(
                    CallRecord(
                        id = "CDR-${1000 + seq}",
                        date = "2026-07-%02d %02d:%02d".format(day, hh, mm),
                        clientId = client.id,
                        clientName = client.name,
                        origin = if (isPhone) CallOrigin.PHONE_800 else CallOrigin.WEB,
                        language = lang,
                        interpreter = interp,
                        minutes = billed,
                        capped = capped
                    )
                )
                seq++
            }
        }
        return out.sortedByDescending { it.date }
    }

    /**
     * Demo session. For VRI we open a real public Jitsi room so the video screen
     * genuinely works end-to-end in the demo; for OPI we return a dial number+PIN.
     */
    fun demoSession(modality: Modality, language: String): SessionStart {
        val id = "S-" + (System.currentTimeMillis() % 100000)
        return if (modality == Modality.VRI) {
            val room = "SIMNdemo" + (System.currentTimeMillis() % 100000)
            SessionStart(
                sessionId = id,
                mode = SessionMode.ROOM,
                modality = modality,
                language = language,
                roomUrl = "https://meet.jit.si/$room#config.prejoinPageEnabled=false",
                interpreter = "Demo Interpreter"
            )
        } else {
            SessionStart(
                sessionId = id,
                mode = SessionMode.DIAL,
                modality = modality,
                language = language,
                dialNumber = "+18005551234",
                pin = "4827",
                interpreter = "Demo Interpreter"
            )
        }
    }

    const val ADMIN_USER = "admin"
    const val ADMIN_PASS = "admin"
    const val CLIENT_USER = "client"
    const val CLIENT_PASS = "client"
    const val CLIENT_ID = "100482"
}
