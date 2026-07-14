package com.quacko.billing

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.quacko.billing.data.Config

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private var splash: View? = null
    private var splashStart = 0L
    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var audioManager: AudioManager

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) { updateAudioRouting() }
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) { updateAudioRouting() }
    }

    private val fileChooser = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        fileCallback?.onReceiveValue(uris)
        fileCallback = null
    }

    private val permissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview)
        splash = findViewById(R.id.splash)
        splashStart = System.currentTimeMillis()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        volumeControlStream = AudioManager.STREAM_VOICE_CALL

        permissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            javaScriptCanOpenWindowsAutomatically = true
            allowFileAccess = true
            allowContentAccess = true
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url
                return when (url.scheme?.lowercase()) {
                    "tel", "mailto", "sms", "whatsapp" -> {
                        try { startActivity(Intent(Intent.ACTION_VIEW, url)) } catch (_: ActivityNotFoundException) {}
                        true
                    }
                    else -> false
                }
            }
            override fun onPageFinished(view: WebView, url: String) {
                // Keep the splash up long enough to read the branding (min ~2.2s)
                val minSplashMs = 2200L
                val elapsed = System.currentTimeMillis() - splashStart
                val remaining = (minSplashMs - elapsed).coerceAtLeast(0L)
                webView.postDelayed({ splash?.visibility = View.GONE }, remaining)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                runOnUiThread { request.grant(request.resources) }
            }
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = filePathCallback
                return try {
                    fileChooser.launch(fileChooserParams.createIntent())
                    true
                } catch (_: ActivityNotFoundException) {
                    fileCallback = null
                    false
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack()
                else { isEnabled = false; onBackPressedDispatcher.onBackPressed() }
            }
        })

        if (savedInstanceState == null) webView.loadUrl(Config.portalUrl)
    }

    /**
     * VoIP audio routing: communication mode for echo cancellation, and send the
     * call to a headset when one is connected (wired / USB / Bluetooth); only fall
     * back to the loudspeaker when nothing is plugged in.
     */
    @Suppress("DEPRECATION")
    private fun updateAudioRouting() {
        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val hasWired = outputs.any {
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                it.type == AudioDeviceInfo.TYPE_USB_HEADSET
            }
            val hasBluetooth = outputs.any {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
            }
            when {
                hasBluetooth -> {
                    audioManager.isSpeakerphoneOn = false
                    try { audioManager.startBluetoothSco(); audioManager.isBluetoothScoOn = true } catch (_: Exception) {}
                }
                hasWired -> {
                    try { audioManager.stopBluetoothSco() } catch (_: Exception) {}
                    audioManager.isBluetoothScoOn = false
                    audioManager.isSpeakerphoneOn = false   // route to the wired/USB headset
                }
                else -> {
                    try { audioManager.stopBluetoothSco() } catch (_: Exception) {}
                    audioManager.isBluetoothScoOn = false
                    audioManager.isSpeakerphoneOn = true    // no headset -> loud hands-free
                }
            }
        } catch (_: Exception) {}
    }

    @Suppress("DEPRECATION")
    private fun restoreAudio() {
        try {
            audioManager.stopBluetoothSco()
            audioManager.isBluetoothScoOn = false
            audioManager.isSpeakerphoneOn = false
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (_: Exception) {}
    }

    override fun onResume() {
        super.onResume()
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, Handler(Looper.getMainLooper()))
        updateAudioRouting()
    }

    override fun onPause() {
        super.onPause()
        try { audioManager.unregisterAudioDeviceCallback(audioDeviceCallback) } catch (_: Exception) {}
        restoreAudio()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState); webView.saveState(outState)
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState); webView.restoreState(savedInstanceState)
    }
    override fun onDestroy() { webView.destroy(); super.onDestroy() }
}
