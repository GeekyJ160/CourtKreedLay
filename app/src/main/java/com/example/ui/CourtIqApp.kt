package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.LocalShotLabStore
import com.example.firebase.attemptAutoSignIn
import com.example.firebase.authStateFlow
import com.example.firebase.signOut
import com.example.viewmodel.CourtIqViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CourtIqApp(auth: FirebaseAuth = Firebase.auth) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val currentUser by auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = auth.currentUser)

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            AuthenticatedCourtIqHost(
                currentUserId = user.uid,
                userDisplayName = user.displayName,
                userEmail = user.email,
                onSignOut = {
                    signOut(
                        credentialManager = credentialManager,
                        scope = scope
                    )
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedCourtIqHost(
    currentUserId: String,
    userDisplayName: String?,
    userEmail: String?,
    onSignOut: () -> Unit,
) {
    val viewModel: CourtIqViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                CourtIqViewModel(
                    repository = CourtIqRepository(db),
                    aiRepository = CoachIqAiRepository(),
                    localShotLabStore = LocalShotLabStore(app, currentUserId),
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
            onSignOut = onSignOut
        )
    }
}
