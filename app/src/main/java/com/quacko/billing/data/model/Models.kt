package com.quacko.billing.data.model

/** Whether the person logging in sees the client view or the admin console. */
enum class UserRole { CLIENT, ADMIN }

data class Session(
    val username: String,
    val role: UserRole,
    val clientId: String?,      // ivr_client_id for CLIENT sessions
    val clientName: String?,
    val token: String? = null   // opaque auth token returned by api.php
)

/** Interpreting modality. VRI = video remote interpreting; OPI = over-the-phone (audio). */
enum class Modality(val code: String, val label: String) {
    VRI("VRI", "Video interpreter"),
    OPI("OPI", "Audio interpreter")
}

data class Language(val code: String, val name: String)

/** How the app should connect the session once QI has routed it. */
enum class SessionMode { ROOM, DIAL }

/**
 * Result of asking QI/quack-o to start a session. For VRI (and web OPI) QI returns
 * a room URL the app opens in a WebView; for phone OPI it returns a dial number + PIN.
 */
data class SessionStart(
    val sessionId: String,
    val mode: SessionMode,
    val modality: Modality,
    val language: String,
    val roomUrl: String? = null,
    val dialNumber: String? = null,
    val pin: String? = null,
    val interpreter: String? = null
)

data class Rating(
    val sessionId: String,
    val stars: Int,
    val comment: String
)

/** Origin of a usage record. WEB = web/VRI call; PHONE_800 = inbound 800/OPI. */
enum class CallOrigin(val label: String) {
    WEB("Web"),
    PHONE_800("800")
}

/** A single usage/call record from the consolidated QI source. */
data class CallRecord(
    val id: String,
    val date: String,
    val clientId: String,
    val clientName: String,
    val origin: CallOrigin,
    val language: String,
    val interpreter: String,
    val minutes: Int,
    val capped: Boolean
)

/** Per-client usage/billing rollup. */
data class ClientBillingSummary(
    val clientId: String,
    val clientName: String,
    val webCalls: Int,
    val phoneCalls: Int,
    val webMinutes: Int,
    val phoneMinutes: Int,
    val cappedCalls: Int,
    val ratePerMinute: Double
) {
    val totalCalls: Int get() = webCalls + phoneCalls
    val totalMinutes: Int get() = webMinutes + phoneMinutes
    val amount: Double get() = totalMinutes * ratePerMinute
}

data class AdminTotals(
    val totalClients: Int,
    val totalCalls: Int,
    val totalMinutes: Int,
    val webMinutes: Int,
    val phoneMinutes: Int,
    val cappedCalls: Int,
    val totalAmount: Double
)

data class ReportFilter(
    val clientId: String? = null,
    val fromDate: String? = null,
    val toDate: String? = null
)

object Billing {
    const val CAP_MINUTES = 120
    const val DEFAULT_RATE = 1.25
}

object Languages {
    val ALL = listOf(
        Language("es", "Spanish"),
        Language("zh", "Mandarin"),
        Language("vi", "Vietnamese"),
        Language("ar", "Arabic"),
        Language("asl", "ASL"),
        Language("ru", "Russian"),
        Language("ht", "Haitian Creole"),
        Language("fr", "French"),
        Language("pt", "Portuguese"),
        Language("ko", "Korean")
    )
}
