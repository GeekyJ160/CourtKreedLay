package com.example.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.CoachIqAiRepository
import com.example.data.CourtIqRepository
import com.example.data.LocalCourtIqStore
import com.example.data.LocalShotLabStore
import com.example.firebase.authStateFlow
import com.example.firebase.signOut
import com.example.viewmodel.CourtIqViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.flowOf

private const val TAG = "CourtIqApp"

@Composable
fun CourtIqApp(auth: FirebaseAuth? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val firebaseApp = remember(context) { FirebaseApp.getApps(context).firstOrNull() }
    val activeAuth = remember(firebaseApp, auth) {
        auth ?: firebaseApp?.let { FirebaseAuth.getInstance(it) }
    }
    val userFlow = remember(activeAuth) {
        activeAuth?.authStateFlow() ?: flowOf(null)
    }
    val currentUser by userFlow.collectAsStateWithLifecycle(
        initialValue = activeAuth?.currentUser
    )
    val repository = remember(firebaseApp, activeAuth, context) {
        val app = firebaseApp
        val firebaseAuth = activeAuth
        if (app == null || firebaseAuth == null) {
            null
        } else {
            runCatching {
                CourtIqRepository(
                    db = FirebaseFirestore.getInstance(
                        app,
                        context.getString(R.string.firestore_database_id)
                    ),
                    auth = firebaseAuth
                )
            }.onFailure { error ->
                Log.w(TAG, "Firebase storage unavailable; using on-device storage.", error)
            }.getOrNull()
        }
    }
    val aiRepository = remember(firebaseApp) {
        if (firebaseApp == null) null else CoachIqAiRepository()
    }
    var showAuthScreen by remember(activeAuth) { mutableStateOf(false) }
    val appUser = currentUser.takeIf { repository != null }
    val onSignOut: () -> Unit = {
        activeAuth?.let { authInstance ->
            signOut(
                auth = authInstance,
                credentialManager = credentialManager,
                scope = scope
            )
        }
    }
    val onRequestSignIn: (() -> Unit)? =
        if (activeAuth != null && repository != null && appUser == null) {
            { showAuthScreen = true }
        } else null

    if (showAuthScreen && activeAuth != null) {
        AuthScreen(
            auth = activeAuth,
            onAuthSuccess = { showAuthScreen = false },
            onContinueLocal = { showAuthScreen = false }
        )
    } else {
        key(appUser?.uid ?: "local") {
            AuthenticatedCourtIqHost(
                repository = repository,
                aiRepository = aiRepository,
                currentUserId = appUser?.uid,
                userDisplayName = appUser?.displayName,
                userEmail = appUser?.email,
                onSignOut = onSignOut,
                onRequestSignIn = onRequestSignIn
            )
        }
    }
}

@Composable
private fun AuthenticatedCourtIqHost(
    repository: CourtIqRepository?,
    aiRepository: CoachIqAiRepository?,
    currentUserId: String?,
    userDisplayName: String?,
    userEmail: String?,
    onSignOut: () -> Unit,
    onRequestSignIn: (() -> Unit)?,
) {
    val viewModel: CourtIqViewModel = viewModel(
        key = currentUserId ?: "local",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                CourtIqViewModel(
                    cloudRepository = repository,
                    aiRepository = aiRepository,
                    localCourtIqStore = LocalCourtIqStore(app),
                    localShotLabStore = LocalShotLabStore(app, currentUserId ?: "local"),
                    currentUserId = currentUserId,
                    initialDisplayName = userDisplayName
                )
            }
        }
    )

    val activeWorkout by viewModel.activeWorkout.collectAsStateWithLifecycle()
    val workoutState = activeWorkout

    if (workoutState != null) {
        WorkoutPlayerScreen(
            state = workoutState,
            onBack = { viewModel.closeWorkoutPlayer() },
            onToggleRunning = { viewModel.toggleWorkoutRunning() },
            onReset = { viewModel.resetCurrentWorkout() },
            onToggleVoice = { viewModel.toggleVoiceCue() },
            onToggleDrillChecked = { viewModel.toggleDrillChecked(it) },
            onSelectDrill = { viewModel.jumpToDrill(it) },
            onNextDrill = { viewModel.advanceToNextDrill() },
            onFinishWorkout = { rpe, notes -> viewModel.finishAndSaveWorkout(rpe, notes) }
        )
    } else {
        MainScreen(
            viewModel = viewModel,
            userEmail = userEmail,
            onSignOut = onSignOut,
            onRequestSignIn = onRequestSignIn,
            cloudStorageAvailable = repository != null
        )
    }
}
