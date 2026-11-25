package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BasketballCurriculum
import com.example.data.CoachChatMessage
import com.example.data.CoachIqAiRepository
import com.example.data.CourtIqRepository
import com.example.data.CourtZoneDef
import com.example.data.LocalCourtIqStore
import com.example.data.LocalShotLabStore
import com.example.data.PersonalizedFocusPlan
import com.example.data.ProgressionSystem
import com.example.data.WorkoutProgram
import com.example.model.AuthorRole
import com.example.model.CoachObservation
import com.example.model.FatigueLevel
import com.example.model.MindsetMission
import com.example.model.PlayerProfile
import com.example.model.ScheduleDay
import com.example.model.SchoolLoad
import com.example.model.ShotZoneStat
import com.example.model.WorkoutLog
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface AiStreamState {
    data object Idle : AiStreamState
    data object Loading : AiStreamState
    data class Streaming(val text: String) : AiStreamState
    data class Success(val text: String) : AiStreamState
    data class Error(val message: String) : AiStreamState
}

sealed interface FocusPlanState {
    data object Idle : FocusPlanState
    data object Loading : FocusPlanState
    data class Success(val plan: PersonalizedFocusPlan) : FocusPlanState
    data class Error(val message: String) : FocusPlanState
}

data class DashboardBundle(
    val profile: PlayerProfile?,
    val workouts: List<WorkoutLog>,
    val shotZones: List<ShotZoneStat>,
    val missions: List<MindsetMission>,
    val observations: List<CoachObservation>,
    val schedule: List<ScheduleDay>,
)

data class ActiveWorkoutState(
    val program: WorkoutProgram,
    val scheduleDayId: String?,
    val currentDrillIndex: Int = 0,
    val secondsRemaining: Int = program.drills.firstOrNull()?.durationSeconds ?: 180,
    val isResting: Boolean = false,
    val isRunning: Boolean = false,
    val completedDrillIds: Set<String> = emptySet(),
    val voiceEnabled: Boolean = true,
    val completedWorkoutSummary: String? = null,
)

class CourtIqViewModel(
    private val repository: CourtIqRepository,
    private val aiRepository: CoachIqAiRepository,
    private val localCourtIqStore: LocalCourtIqStore,
    private val localShotLabStore: LocalShotLabStore,
    private val currentUserId: String?,
    initialDisplayName: String?,
) : ViewModel() {

    private fun <T> Flow<T>.unwrapFirestoreErrors(label: String): Flow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing $label", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load $label"))
            }

    val dashboardState: StateFlow<UiState<DashboardBundle>> = if (currentUserId == null) {
        combine(
            localCourtIqStore.profile,
            localCourtIqStore.workouts,
            localShotLabStore.shotZones,
            localCourtIqStore.missions,
            localCourtIqStore.observations,
            localCourtIqStore.schedule,
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            UiState.Success(
                DashboardBundle(
                    profile = values[0] as PlayerProfile?,
                    workouts = values[1] as List<WorkoutLog>,
                    shotZones = values[2] as List<ShotZoneStat>,
                    missions = values[3] as List<MindsetMission>,
                    observations = values[4] as List<CoachObservation>,
                    schedule = values[5] as List<ScheduleDay>,
                )
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Success(
                DashboardBundle(
                    profile = localCourtIqStore.profile.value,
                    workouts = localCourtIqStore.workouts.value,
                    shotZones = localShotLabStore.shotZones.value,
                    missions = localCourtIqStore.missions.value,
                    observations = localCourtIqStore.observations.value,
                    schedule = localCourtIqStore.schedule.value,
                )
            )
        )
    } else {
        combine(
            repository.observeProfile(currentUserId).unwrapFirestoreErrors("profile"),
            repository.observeWorkouts(currentUserId).unwrapFirestoreErrors("workouts"),
            repository.observeShotZones(currentUserId).unwrapFirestoreErrors("shotZones"),
            repository.observeMissions(currentUserId).unwrapFirestoreErrors("missions"),
            repository.observeObservations(currentUserId).unwrapFirestoreErrors("observations"),
            repository.observeSchedule(currentUserId).unwrapFirestoreErrors("schedule"),
            localShotLabStore.shotZones,
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val profileRes = values[0] as UiState<PlayerProfile?>
            @Suppress("UNCHECKED_CAST")
            val workoutsRes = values[1] as UiState<List<WorkoutLog>>
            @Suppress("UNCHECKED_CAST")
            val shotsRes = values[2] as UiState<List<ShotZoneStat>>
            @Suppress("UNCHECKED_CAST")
            val missionsRes = values[3] as UiState<List<MindsetMission>>
            @Suppress("UNCHECKED_CAST")
            val obsRes = values[4] as UiState<List<CoachObservation>>
            @Suppress("UNCHECKED_CAST")
            val schedRes = values[5] as UiState<List<ScheduleDay>>
            @Suppress("UNCHECKED_CAST")
            val localShots = values[6] as List<ShotZoneStat>

            if (shotsRes is UiState.Success) {
                localShotLabStore.seedFromCloudIfEmpty(shotsRes.data)
            }

            val firstError = listOf(profileRes, workoutsRes, shotsRes, missionsRes, obsRes, schedRes)
                .filterIsInstance<UiState.Error>()
                .firstOrNull()
            if (firstError != null) {
                firstError
            } else if (
                profileRes is UiState.Success &&
                workoutsRes is UiState.Success &&
                missionsRes is UiState.Success &&
                obsRes is UiState.Success &&
                schedRes is UiState.Success
            ) {
                UiState.Success(
                    DashboardBundle(
                        profile = profileRes.data,
                        workouts = workoutsRes.data,
                        shotZones = localShots,
                        missions = missionsRes.data,
                        observations = obsRes.data,
                        schedule = schedRes.data,
                    )
                )
            } else {
                UiState.Loading
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )
    }

    private val _selectedZoneId = MutableStateFlow(BasketballCurriculum.courtZones.first().id)
    val selectedZoneId: StateFlow<String> = _selectedZoneId.asStateFlow()

    private val _activeWorkout = MutableStateFlow<ActiveWorkoutState?>(null)
    val activeWorkout: StateFlow<ActiveWorkoutState?> = _activeWorkout.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _chatMessages = MutableStateFlow(
        listOf(
            CoachChatMessage(
                id = "welcome",
                fromCoach = true,
                text = "Hey! I'm Coach IQ — your personal guard development assistant. Remember: your goal isn't to make every shot. Your goal is to recognize the right shot, attack open lanes, and lead with your voice. What are we working on today?"
            )
        )
    )
    val chatMessages: StateFlow<List<CoachChatMessage>> = _chatMessages.asStateFlow()

    private val _aiStreamState = MutableStateFlow<AiStreamState>(AiStreamState.Idle)
    val aiStreamState: StateFlow<AiStreamState> = _aiStreamState.asStateFlow()

    private val _focusPlanState = MutableStateFlow<FocusPlanState>(FocusPlanState.Idle)
    val focusPlanState: StateFlow<FocusPlanState> = _focusPlanState.asStateFlow()

    private var timerJob: Job? = null

    init {
        if (currentUserId != null) {
            viewModelScope.launch {
                val result = repository.ensureInitialData(currentUserId, initialDisplayName)
                result.onFailure { e ->
                    _statusBanner.value = e.localizedMessage ?: "Failed to initialize player profile."
                }
            }
        }
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun selectCourtZone(zoneId: String) {
        _selectedZoneId.value = zoneId
    }

    fun startWorkout(program: WorkoutProgram, scheduleDayId: String? = null) {
        timerJob?.cancel()
        val firstDrill = program.drills.firstOrNull()
        _activeWorkout.value = ActiveWorkoutState(
            program = program,
            scheduleDayId = scheduleDayId,
            currentDrillIndex = 0,
            secondsRemaining = firstDrill?.durationSeconds ?: 180,
            isResting = false,
            isRunning = true,
            completedDrillIds = emptySet(),
            voiceEnabled = true,
            completedWorkoutSummary = null
        )
        runWorkoutTimer()
    }

    fun closeWorkoutPlayer() {
        timerJob?.cancel()
        _activeWorkout.value = null
    }

    fun toggleWorkoutRunning() {
        val current = _activeWorkout.value ?: return
        val nextRunning = !current.isRunning
        _activeWorkout.value = current.copy(isRunning = nextRunning)
        if (nextRunning) {
            runWorkoutTimer()
        } else {
            timerJob?.cancel()
        }
    }

    fun resetCurrentWorkout() {
        val current = _activeWorkout.value ?: return
        timerJob?.cancel()
        val first = current.program.drills.firstOrNull()
        _activeWorkout.value = current.copy(
            currentDrillIndex = 0,
            secondsRemaining = first?.durationSeconds ?: 180,
            isResting = false,
            isRunning = false,
            completedDrillIds = emptySet(),
            completedWorkoutSummary = null
        )
    }

    fun toggleVoiceCue() {
        val current = _activeWorkout.value ?: return
        _activeWorkout.value = current.copy(voiceEnabled = !current.voiceEnabled)
    }

    fun toggleDrillChecked(drillId: String) {
        val current = _activeWorkout.value ?: return
        val updatedSet = if (drillId in current.completedDrillIds) {
            current.completedDrillIds - drillId
        } else {
            current.completedDrillIds + drillId
        }
        _activeWorkout.value = current.copy(completedDrillIds = updatedSet)
    }

    fun jumpToDrill(index: Int) {
        val current = _activeWorkout.value ?: return
        val drills = current.program.drills
        if (index !in drills.indices) return
        _activeWorkout.value = current.copy(
            currentDrillIndex = index,
            secondsRemaining = drills[index].durationSeconds,
            isResting = false,
        )
    }

    fun advanceToNextDrill() {
        val current = _activeWorkout.value ?: return
        val drills = current.program.drills
        val activeDrill = drills.getOrNull(current.currentDrillIndex)
        val updatedCompleted = if (activeDrill != null) {
            current.completedDrillIds + activeDrill.id
        } else current.completedDrillIds

        val nextIdx = current.currentDrillIndex + 1
        if (nextIdx < drills.size) {
            _activeWorkout.value = current.copy(
                currentDrillIndex = nextIdx,
                secondsRemaining = drills[nextIdx].durationSeconds,
                isResting = false,
                completedDrillIds = updatedCompleted
            )
        } else {
            timerJob?.cancel()
            _activeWorkout.value = current.copy(
                isRunning = false,
                completedDrillIds = updatedCompleted,
            )
        }
    }

    private fun runWorkoutTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val state = _activeWorkout.value ?: break
                if (!state.isRunning) break
                if (state.secondsRemaining > 1) {
                    _activeWorkout.value = state.copy(secondsRemaining = state.secondsRemaining - 1)
                } else {
                    val drills = state.program.drills
                    val curDrill = drills.getOrNull(state.currentDrillIndex)
                    if (!state.isResting && curDrill != null && curDrill.restSeconds > 0 && state.currentDrillIndex < drills.lastIndex) {
                        _activeWorkout.value = state.copy(
                            isResting = true,
                            secondsRemaining = curDrill.restSeconds,
                            completedDrillIds = state.completedDrillIds + curDrill.id
                        )
                    } else {
                        val nextIdx = state.currentDrillIndex + 1
                        val newCompleted = if (curDrill != null) state.completedDrillIds + curDrill.id else state.completedDrillIds
                        if (nextIdx < drills.size) {
                            _activeWorkout.value = state.copy(
                                currentDrillIndex = nextIdx,
                                isResting = false,
                                secondsRemaining = drills[nextIdx].durationSeconds,
                                completedDrillIds = newCompleted
                            )
                        } else {
                            _activeWorkout.value = state.copy(
                                isRunning = false,
                                isResting = false,
                                secondsRemaining = 0,
                                completedDrillIds = newCompleted
                            )
                            break
                        }
                    }
                }
            }
        }
    }

    fun finishAndSaveWorkout(rpeFatigue: Int, notes: String?) {
        val current = _activeWorkout.value ?: return
        val bundle = (dashboardState.value as? UiState.Success)?.data
        val profile = bundle?.profile ?: return
        timerJob?.cancel()
        val drillsDone = current.completedDrillIds.size.coerceAtLeast(1)
        if (currentUserId == null) {
            localCourtIqStore.completeWorkout(
                workout = current.program,
                drillsCompleted = drillsDone,
                rpeFatigue = rpeFatigue,
                notes = notes,
                scheduleDayId = current.scheduleDayId
            )
            _activeWorkout.value = current.copy(
                isRunning = false,
                completedWorkoutSummary = "Workout logged! +${current.program.xpReward} XP earned. Keep building your game!"
            )
            return
        }
        viewModelScope.launch {
            val result = repository.completeWorkout(
                userId = currentUserId,
                profile = profile,
                workout = current.program,
                drillsCompleted = drillsDone,
                rpeFatigue = rpeFatigue,
                notes = notes,
                scheduleDayId = current.scheduleDayId
            )
            result.onSuccess {
                _activeWorkout.value = current.copy(
                    isRunning = false,
                    completedWorkoutSummary = "Workout logged! +${current.program.xpReward} XP earned. Keep building your game!"
                )
            }.onFailure { e ->
                _statusBanner.value = e.localizedMessage ?: "Could not save workout."
            }
        }
    }

    fun recordShot(zone: CourtZoneDef, made: Boolean) {
        val addMakes = if (made) 1 else 0
        recordBatchShots(zone = zone, makes = addMakes, attempts = 1)
    }

    fun recordBatchShots(zone: CourtZoneDef, makes: Int, attempts: Int) {
        val currentBundle = (dashboardState.value as? UiState.Success)?.data
        val prevProfile = currentBundle?.profile ?: PlayerProfile()
        val prevZones = currentBundle?.shotZones ?: localShotLabStore.shotZones.value
        val beforeProg = ProgressionSystem.evaluateProgression(prevProfile, prevZones)

        localShotLabStore.recordShots(zone = zone, addMakes = makes, addAttempts = attempts)
        val updatedZones = localShotLabStore.shotZones.value
        val xpGain = ProgressionSystem.computeShotXpGain(makes, attempts)

        val uid = currentUserId
        if (uid == null) {
            val (oldLevel, newLevel) = localCourtIqStore.awardShotLabXp(makes, attempts)
            val updatedProfile = localCourtIqStore.profile.value ?: prevProfile.copy(
                xp = prevProfile.xp + xpGain,
                currentLevel = newLevel
            )
            val afterProg = ProgressionSystem.evaluateProgression(updatedProfile, updatedZones)
            announceProgressionEvent(beforeProg, afterProg, oldLevel, newLevel, xpGain)
            return
        }

        val predictedXp = (prevProfile.xp + xpGain).coerceIn(0, 1_000_000)
        val predictedLevel = ProgressionSystem.resolveLevel(prevProfile.currentLevel, predictedXp)
        val afterProg = ProgressionSystem.evaluateProgression(
            prevProfile.copy(xp = predictedXp, currentLevel = predictedLevel),
            updatedZones
        )
        announceProgressionEvent(beforeProg, afterProg, beforeProg.resolvedLevel, predictedLevel, xpGain)

        viewModelScope.launch {
            repository.recordShots(
                userId = uid,
                zone = zone,
                addMakes = makes,
                addAttempts = attempts,
                currentProfile = prevProfile
            )
        }
    }

    private fun announceProgressionEvent(
        before: com.example.data.ProgressionSnapshot,
        after: com.example.data.ProgressionSnapshot,
        oldLevel: Int,
        newLevel: Int,
        xpGain: Int,
    ) {
        val newlyUnlocked = after.badges.filter { afterBadge ->
            afterBadge.unlocked && before.badges.none { it.id == afterBadge.id && it.unlocked }
        }
        when {
            newLevel > oldLevel -> {
                val levelTitle = BasketballCurriculum.evolutionLevels
                    .firstOrNull { it.levelNumber == newLevel }?.title ?: "Next Tier"
                val badgeNote = if (newlyUnlocked.isNotEmpty()) {
                    " · Badge unlocked: ${newlyUnlocked.first().title}!"
                } else ""
                _statusBanner.value = "LEVEL UP! Level $newLevel ($levelTitle) · +$xpGain XP$badgeNote"
            }
            newlyUnlocked.isNotEmpty() -> {
                _statusBanner.value = "Badge Unlocked: ${newlyUnlocked.first().title}! (+$xpGain XP)"
            }
        }
    }

    fun resetShotSession() {
        localShotLabStore.resetSession()
        _statusBanner.value = "Session shot counters reset."
        val uid = currentUserId ?: return
        val bundle = (dashboardState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.resetShotSession(uid, bundle.shotZones)
        }
    }

    fun incrementMission(mission: MindsetMission) {
        val uid = currentUserId
        if (uid == null) {
            localCourtIqStore.incrementMission(mission)
            return
        }
        val profile = (dashboardState.value as? UiState.Success)?.data?.profile
        viewModelScope.launch {
            repository.incrementMissionProgress(uid, profile, mission)
                .onFailure { e -> _statusBanner.value = e.localizedMessage ?: "Failed to update mission." }
        }
    }

    fun toggleMission(mission: MindsetMission) {
        val uid = currentUserId
        if (uid == null) {
            localCourtIqStore.toggleMission(mission)
            return
        }
        val profile = (dashboardState.value as? UiState.Success)?.data?.profile
        viewModelScope.launch {
            repository.toggleMissionComplete(uid, profile, mission)
                .onFailure { e -> _statusBanner.value = e.localizedMessage ?: "Failed to update mission." }
        }
    }

    fun addCustomMission(title: String, category: String, targetCount: Int) {
        val uid = currentUserId
        if (uid == null) {
            if (title.isNotBlank()) {
                localCourtIqStore.addCustomMission(title, category, targetCount)
                _statusBanner.value = "New Fearless Challenge added!"
            }
            return
        }
        viewModelScope.launch {
            repository.addCustomMission(uid, title, category, targetCount)
                .onSuccess { _statusBanner.value = "New Fearless Challenge added!" }
                .onFailure { e -> _statusBanner.value = e.localizedMessage ?: "Failed to add challenge." }
        }
    }

    fun resetWeeklyMissions() {
        val uid = currentUserId
        if (uid == null) {
            localCourtIqStore.resetWeeklyMissions()
            _statusBanner.value = "Weekly Fearless Challenges reset for a new week!"
            return
        }
        val missions = (dashboardState.value as? UiState.Success)?.data?.missions ?: return
        viewModelScope.launch {
            repository.resetWeeklyMissions(uid, missions)
                .onSuccess { _statusBanner.value = "Weekly Fearless Challenges reset for a new week!" }
                .onFailure { e -> _statusBanner.value = e.localizedMessage ?: "Failed to reset challenges." }
        }
    }

    fun addObservation(authorRole: AuthorRole, focusTag: String, note: String) {
        val uid = currentUserId
        if (uid == null) {
            if (note.isNotBlank()) {
                localCourtIqStore.addObservation(authorRole, focusTag, note)
                _statusBanner.value = "Note saved to your Court IQ log."
            }
            return
        }
        viewModelScope.launch {
            repository.addObservation(uid, authorRole, focusTag, note)
                .onSuccess { _statusBanner.value = "Note saved to your Court IQ log." }
                .onFailure { e -> _statusBanner.value = e.localizedMessage ?: "Failed to save note." }
        }
    }

    fun deleteObservation(observationId: String) {
        val uid = currentUserId
        if (uid == null) {
            localCourtIqStore.deleteObservation(observationId)
            return
        }
        viewModelScope.launch {
            repository.deleteObservation(uid, observationId)
                .onFailure { e -> _statusBanner.value = e.localizedMessage ?: "Failed to delete note." }
        }
    }

    fun saveCoachCalibration(
        playerName: String,
        assignedFocus: String,
        weeklyGoal: String,
        dailyAffirmation: String,
        fatigueLevel: FatigueLevel,
        currentLevel: Int,
        tryoutReadiness: Int,
        shooting: Int,
        ballHandling: Int,
        finishing: Int,
        defense: Int,
        courtVision: Int,
        confidence: Int,
        leadership: Int,
        weakHand: Int,
    ) {
        val uid = currentUserId
        if (uid == null) {
            localCourtIqStore.updateProfile(
                playerName = playerName,
                assignedFocus = assignedFocus,
                weeklyGoal = weeklyGoal,
                dailyAffirmation = dailyAffirmation,
                fatigueLevel = fatigueLevel,
                currentLevel = currentLevel,
                tryoutReadiness = tryoutReadiness,
                shooting = shooting,
                ballHandling = ballHandling,
                finishing = finishing,
                defense = defense,
                courtVision = courtVision,
                confidence = confidence,
                leadership = leadership,
                weakHand = weakHand
            )
            _statusBanner.value = "Player ratings & coach targets saved!"
            return
        }
        viewModelScope.launch {
            repository.updateProfile(
                userId = uid,
                playerName = playerName,
                assignedFocus = assignedFocus,
                weeklyGoal = weeklyGoal,
                dailyAffirmation = dailyAffirmation,
                fatigueLevel = fatigueLevel,
                currentLevel = currentLevel,
                tryoutReadiness = tryoutReadiness,
                shooting = shooting,
                ballHandling = ballHandling,
                finishing = finishing,
                defense = defense,
                courtVision = courtVision,
                confidence = confidence,
                leadership = leadership,
                weakHand = weakHand
            ).onSuccess {
                _statusBanner.value = "Player ratings & coach targets saved!"
            }.onFailure { e ->
                _statusBanner.value = e.localizedMessage ?: "Failed to save ratings."
            }
        }
    }

    fun updateScheduleDay(
        day: ScheduleDay,
        focusTitle: String,
        durationMinutes: Int,
        schoolLoad: SchoolLoad,
        completedThisWeek: Boolean,
    ) {
        val uid = currentUserId
        if (uid == null) {
            localCourtIqStore.updateScheduleDay(
                dayId = day.id,
                focusTitle = focusTitle,
                durationMinutes = durationMinutes,
                schoolLoad = schoolLoad,
                completedThisWeek = completedThisWeek
            )
            return
        }
        viewModelScope.launch {
            repository.updateScheduleDay(
                userId = uid,
                dayId = day.id,
                focusTitle = focusTitle,
                durationMinutes = durationMinutes,
                schoolLoad = schoolLoad,
                completedThisWeek = completedThisWeek
            ).onFailure { e ->
                _statusBanner.value = e.localizedMessage ?: "Failed to update schedule."
            }
        }
    }

    fun askCoachIq(question: String) {
        val cleanQuestion = question.trim()
        if (cleanQuestion.isEmpty()) return
        val bundle = (dashboardState.value as? UiState.Success)?.data
        val contextSummary = aiRepository.buildPlayerContextSummary(
            profile = bundle?.profile,
            shotZones = bundle?.shotZones ?: emptyList(),
            missions = bundle?.missions ?: emptyList(),
            observations = bundle?.observations ?: emptyList(),
        )

        val priorHistory = _chatMessages.value
        val userMsg = CoachChatMessage(
            id = UUID.randomUUID().toString(),
            fromCoach = false,
            text = cleanQuestion
        )
        _chatMessages.value = priorHistory + userMsg

        viewModelScope.launch {
            _aiStreamState.value = AiStreamState.Loading
            var finalReply = ""
            try {
                aiRepository.streamCoachResponse(
                    userMessage = cleanQuestion,
                    playerContext = contextSummary,
                    recentHistory = priorHistory
                ).collect { partial ->
                    finalReply = partial
                    _aiStreamState.value = AiStreamState.Streaming(partial)
                }
                _chatMessages.value = _chatMessages.value + CoachChatMessage(
                    id = UUID.randomUUID().toString(),
                    fromCoach = true,
                    text = finalReply
                )
                _aiStreamState.value = AiStreamState.Success(finalReply)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "askCoachIq failed", e)
                _aiStreamState.value = AiStreamState.Error(
                    e.localizedMessage ?: "Coach IQ could not respond right now."
                )
            }
        }
    }

    fun generatePersonalizedFocusPlan() {
        val bundle = (dashboardState.value as? UiState.Success)?.data
        val contextSummary = aiRepository.buildPlayerContextSummary(
            profile = bundle?.profile,
            shotZones = bundle?.shotZones ?: emptyList(),
            missions = bundle?.missions ?: emptyList(),
            observations = bundle?.observations ?: emptyList(),
        )

        viewModelScope.launch {
            _focusPlanState.value = FocusPlanState.Loading
            try {
                val plan = aiRepository.generateStructuredFocusPlan(contextSummary)
                _focusPlanState.value = FocusPlanState.Success(plan)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "generatePersonalizedFocusPlan failed", e)
                _focusPlanState.value = FocusPlanState.Error(
                    e.localizedMessage ?: "Failed to generate focus plan."
                )
            }
        }
    }

    private companion object {
        const val TAG = "CourtIqViewModel"
    }
}
