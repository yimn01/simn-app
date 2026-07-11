package com.quacko.billing.data

/**
 * Runtime configuration.
 */
object Config {
    /**
     * The live client portal the app loads. This is your existing quack-o web
     * client (index.html), which already handles login + VRI/OPI calling + usage.
     * Change this to point the app at a staging/test URL if you want.
     */
    var portalUrl: String = "https://quack-o.com/"

    // --- Legacy (used only by the older native/demo screens, not the WebView) ---
    var baseUrl: String = "https://quack-o.com"
    var demoMode: Boolean = true
    fun apiEndpoint(): String = baseUrl.trimEnd('/') + "/api.php"
}
