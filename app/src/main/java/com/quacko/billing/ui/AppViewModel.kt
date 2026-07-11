package com.quacko.billing.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quacko.billing.data.Repository
import com.quacko.billing.data.model.Modality
import com.quacko.billing.data.model.Rating
import com.quacko.billing.data.model.Session
import com.quacko.billing.data.model.SessionStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {

    val repo = Repository()

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

    private val _loggingIn = MutableStateFlow(false)
    val loggingIn: StateFlow<Boolean> = _loggingIn.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Active VRI/OPI session (non-null => show the call screen)
    private val _activeCall = MutableStateFlow<SessionStart?>(null)
    val activeCall: StateFlow<SessionStart?> = _activeCall.asStateFlow()

    // Session awaiting a rating after it ended (non-null => show rating screen)
    private val _ratingFor = MutableStateFlow<SessionStart?>(null)
    val ratingFor: StateFlow<SessionStart?> = _ratingFor.asStateFlow()

    private val _requesting = MutableStateFlow(false)
    val requesting: StateFlow<Boolean> = _requesting.asStateFlow()

    private val _requestError = MutableStateFlow<String?>(null)
    val requestError: StateFlow<String?> = _requestError.asStateFlow()

    fun login(username: String, password: String) {
        if (_loggingIn.value) return
        _error.value = null
        _loggingIn.value = true
        viewModelScope.launch {
            try {
                _session.value = repo.login(username, password)
            } catch (e: Exception) {
                _error.value = e.message ?: "Login failed"
            } finally {
                _loggingIn.value = false
            }
        }
    }

    fun logout() {
        _session.value = null
        _activeCall.value = null
        _ratingFor.value = null
        _error.value = null
    }

    fun requestSession(modality: Modality, language: String) {
        val s = _session.value ?: return
        if (_requesting.value) return
        _requestError.value = null
        _requesting.value = true
        viewModelScope.launch {
            try {
                _activeCall.value = repo.requestSession(s, modality, language)
            } catch (e: Exception) {
                _requestError.value = e.message ?: "Could not start the session"
            } finally {
                _requesting.value = false
            }
        }
    }

    fun clearRequestError() { _requestError.value = null }

    /** End the current call and move to the rating screen. */
    fun endCall() {
        val call = _activeCall.value
        _activeCall.value = null
        if (call != null) _ratingFor.value = call
    }

    fun skipRating() { _ratingFor.value = null }

    fun submitRating(stars: Int, comment: String) {
        val s = _session.value
        val call = _ratingFor.value
        _ratingFor.value = null
        if (s == null || call == null) return
        viewModelScope.launch {
            try {
                repo.submitRating(s, Rating(call.sessionId, stars, comment))
            } catch (_: Exception) {
                // Rating is best-effort; ignore failures so it never blocks the user.
            }
        }
    }
}
