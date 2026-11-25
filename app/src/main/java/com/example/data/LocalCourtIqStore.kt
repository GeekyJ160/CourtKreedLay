package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AuthorRole
import com.example.model.CoachObservation
import com.example.model.FatigueLevel
import com.example.model.MindsetMission
import com.example.model.PlayerProfile
import com.example.model.ScheduleDay
import com.example.model.SchoolLoad
import com.example.model.WorkoutLog
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Device-local persistence for player profile, workouts, mindset missions,
 * observations, and weekly schedule when running without a signed-in account.
 */
class LocalCourtIqStore(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        "court_iq_local_profile",
        Context.MODE_PRIVATE
    )

    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<PlayerProfile?> = _profile.asStateFlow()

    private val _workouts = MutableStateFlow(loadWorkouts())
    val workouts: StateFlow<List<WorkoutLog>> = _workouts.asStateFlow()

    private val _missions = MutableStateFlow(loadMissions())
    val missions: StateFlow<List<MindsetMission>> = _missions.asStateFlow()

    private val _observations = MutableStateFlow(loadObservations())
    val observations: StateFlow<List<CoachObservation>> = _observations.asStateFlow()

    private val _schedule = MutableStateFlow(loadSchedule())
    val schedule: StateFlow<List<ScheduleDay>> = _schedule.asStateFlow()

    @Synchronized
    fun completeWorkout(
        workout: WorkoutProgram,
        drillsCompleted: Int,
        rpeFatigue: Int,
        notes: String?,
        scheduleDayId: String?,
    ) {
        val currentProfile = _profile.value ?: PlayerProfile()
        val safeDrills = drillsCompleted.coerceIn(0, workout.drills.size.coerceAtLeast(1))
        val safeRpe = rpeFatigue.coerceIn(1, 10)
        val earnedXp = if (safeDrills >= workout.drills.size) {
            workout.xpReward
        } else {
            ((workout.xpReward.toDouble() / workout.drills.size.coerceAtLeast(1)) * safeDrills).toInt()
                .coerceAtLeast(25)
        }

        val newLog = WorkoutLog(
            id = UUID.randomUUID().toString(),
            userId = "local",
            dayKey = workout.dayKey,
            title = workout.title,
            focusArea = workout.levelTag,
            durationMinutes = workout.durationMinutes,
            drillsCompleted = safeDrills,
            totalDrills = workout.drills.size.coerceAtLeast(1),
            xpEarned = earnedXp,
            rpeFatigue = safeRpe,
            notes = notes?.trim()?.takeIf { it.isNotEmpty() }?.take(300),
        )
        val updatedWorkouts = (listOf(newLog) + _workouts.value).take(30)
        saveWorkouts(updatedWorkouts)
        _workouts.value = updatedWorkouts

        val newFatigue = when {
            safeRpe >= 8 -> FatigueLevel.TIRED.name
            safeRpe >= 5 -> FatigueLevel.MODERATE.name
            else -> FatigueLevel.FRESH.name
        }
        val newXp = (currentProfile.xp + earnedXp).coerceIn(0, 1_000_000)
        val newLevel = ProgressionSystem.resolveLevel(currentProfile.currentLevel, newXp)
        val updatedProfile = currentProfile.copy(
            xp = newXp,
            currentLevel = newLevel,
            streakDays = (currentProfile.streakDays + 1).coerceIn(0, 5000),
            weakHand = (currentProfile.weakHand + 1).coerceIn(0, 99),
            ballHandling = (currentProfile.ballHandling + 1).coerceIn(0, 99),
            confidence = (currentProfile.confidence + 1).coerceIn(0, 99),
            fatigueLevel = newFatigue,
            tryoutReadiness = (currentProfile.tryoutReadiness + 2).coerceIn(0, 100),
        )
        saveProfile(updatedProfile)
        _profile.value = updatedProfile

        if (!scheduleDayId.isNullOrBlank()) {
            val updatedSchedule = _schedule.value.map { day ->
                if (day.id == scheduleDayId) day.copy(completedThisWeek = true) else day
            }
            saveSchedule(updatedSchedule)
            _schedule.value = updatedSchedule
        }
    }

    @Synchronized
    fun incrementMission(mission: MindsetMission) {
        val nextCount = if (mission.completed) {
            mission.targetCount
        } else {
            (mission.currentCount + 1).coerceAtMost(mission.targetCount)
        }
        val nowCompleted = nextCount >= mission.targetCount
        val becameCompleted = !mission.completed && nowCompleted

        val updated = _missions.value.map {
            if (it.id == mission.id) it.copy(currentCount = nextCount, completed = nowCompleted) else it
        }
        saveMissions(updated)
        _missions.value = updated

        if (becameCompleted) {
            val cur = _profile.value ?: PlayerProfile()
            val newXp = (cur.xp + mission.xpReward).coerceIn(0, 1_000_000)
            val updatedProfile = cur.copy(
                xp = newXp,
                currentLevel = ProgressionSystem.resolveLevel(cur.currentLevel, newXp),
                confidence = (cur.confidence + 2).coerceIn(0, 99),
                leadership = (cur.leadership + 2).coerceIn(0, 99),
            )
            saveProfile(updatedProfile)
            _profile.value = updatedProfile
        }
    }

    @Synchronized
    fun toggleMission(mission: MindsetMission) {
        val nextCompleted = !mission.completed
        val nextCount = if (nextCompleted) mission.targetCount else 0
        val updated = _missions.value.map {
            if (it.id == mission.id) it.copy(currentCount = nextCount, completed = nextCompleted) else it
        }
        saveMissions(updated)
        _missions.value = updated

        if (nextCompleted) {
            val cur = _profile.value ?: PlayerProfile()
            val newXp = (cur.xp + mission.xpReward).coerceIn(0, 1_000_000)
            val updatedProfile = cur.copy(
                xp = newXp,
                currentLevel = ProgressionSystem.resolveLevel(cur.currentLevel, newXp),
                confidence = (cur.confidence + 1).coerceIn(0, 99),
            )
            saveProfile(updatedProfile)
            _profile.value = updatedProfile
        }
    }

    @Synchronized
    fun awardShotLabXp(makes: Int, attempts: Int): Pair<Int, Int> {
        val xpGain = ProgressionSystem.computeShotXpGain(makes, attempts)
        val cur = _profile.value ?: PlayerProfile()
        val oldLevel = ProgressionSystem.resolveLevel(cur.currentLevel, cur.xp)
        val newXp = (cur.xp + xpGain).coerceIn(0, 1_000_000)
        val newLevel = ProgressionSystem.resolveLevel(oldLevel, newXp)
        val updated = cur.copy(
            xp = newXp,
            currentLevel = newLevel,
            shooting = if (makes >= 5) (cur.shooting + 1).coerceIn(0, 99) else cur.shooting,
        )
        saveProfile(updated)
        _profile.value = updated
        return oldLevel to newLevel
    }

    @Synchronized
    fun addCustomMission(title: String, category: String, targetCount: Int) {
        val cleanTitle = title.trim().take(140)
        if (cleanTitle.isEmpty()) return
        val newMission = MindsetMission(
            id = "custom_${UUID.randomUUID()}",
            userId = "local",
            title = cleanTitle,
            category = category.trim().ifEmpty { "CONFIDENCE" }.take(40),
            completed = false,
            targetCount = targetCount.coerceIn(1, 50),
            currentCount = 0,
            xpReward = 65,
        )
        val updated = _missions.value + newMission
        saveMissions(updated)
        _missions.value = updated
    }

    @Synchronized
    fun resetWeeklyMissions() {
        val updated = _missions.value.map { it.copy(completed = false, currentCount = 0) }
        saveMissions(updated)
        _missions.value = updated
    }

    @Synchronized
    fun addObservation(authorRole: AuthorRole, focusTag: String, note: String) {
        val cleanNote = note.trim().take(500)
        if (cleanNote.isEmpty()) return
        val newObs = CoachObservation(
            id = UUID.randomUUID().toString(),
            userId = "local",
            authorRole = authorRole.name,
            focusTag = focusTag.trim().ifEmpty { "Confidence" }.take(40),
            note = cleanNote,
        )
        val updated = (listOf(newObs) + _observations.value).take(25)
        saveObservations(updated)
        _observations.value = updated
    }

    @Synchronized
    fun deleteObservation(observationId: String) {
        val updated = _observations.value.filterNot { it.id == observationId }
        saveObservations(updated)
        _observations.value = updated
    }

    @Synchronized
    fun updateProfile(
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
        val cur = _profile.value ?: PlayerProfile()
        val updated = cur.copy(
            playerName = playerName.trim().ifEmpty { "Future Floor General" }.take(40),
            assignedFocus = assignedFocus.trim().ifEmpty { "Ball control & confidence" }.take(100),
            weeklyGoal = weeklyGoal.trim().ifEmpty { "Attack open lanes" }.take(200),
            dailyAffirmation = dailyAffirmation.trim().ifEmpty { BasketballCurriculum.affirmations.first() }.take(220),
            fatigueLevel = fatigueLevel.name,
            currentLevel = currentLevel.coerceIn(1, 6),
            tryoutReadiness = tryoutReadiness.coerceIn(0, 100),
            shooting = shooting.coerceIn(0, 99),
            ballHandling = ballHandling.coerceIn(0, 99),
            finishing = finishing.coerceIn(0, 99),
            defense = defense.coerceIn(0, 99),
            courtVision = courtVision.coerceIn(0, 99),
            confidence = confidence.coerceIn(0, 99),
            leadership = leadership.coerceIn(0, 99),
            weakHand = weakHand.coerceIn(0, 99),
        )
        saveProfile(updated)
        _profile.value = updated
    }

    @Synchronized
    fun updateScheduleDay(
        dayId: String,
        focusTitle: String,
        durationMinutes: Int,
        schoolLoad: SchoolLoad,
        completedThisWeek: Boolean,
    ) {
        val updated = _schedule.value.map { day ->
            if (day.id == dayId) {
                day.copy(
                    focusTitle = focusTitle.trim().ifEmpty { "Skill session" }.take(80),
                    durationMinutes = durationMinutes.coerceIn(0, 120),
                    schoolLoad = schoolLoad.name,
                    completedThisWeek = completedThisWeek,
                )
            } else day
        }
        saveSchedule(updated)
        _schedule.value = updated
    }

    private fun loadProfile(): PlayerProfile {
        val raw = prefs.getString(KEY_PROFILE, null) ?: return defaultProfile()
        return try {
            val j = JSONObject(raw)
            PlayerProfile(
                userId = "local",
                playerName = j.optString("playerName", "Future Floor General"),
                position = j.optString("position", "PG / SG"),
                gradYear = j.optInt("gradYear", 2032),
                currentLevel = j.optInt("currentLevel", 1),
                xp = j.optInt("xp", 250),
                streakDays = j.optInt("streakDays", 1),
                dailyAffirmation = j.optString("dailyAffirmation", BasketballCurriculum.affirmations.first()),
                assignedFocus = j.optString("assignedFocus", "Control, shooting form & fearless confidence"),
                weeklyGoal = j.optString("weeklyGoal", "Call for the ball 5 times in pickup or scrimmage"),
                fatigueLevel = j.optString("fatigueLevel", FatigueLevel.FRESH.name),
                tryoutReadiness = j.optInt("tryoutReadiness", 45),
                shooting = j.optInt("shooting", 65),
                ballHandling = j.optInt("ballHandling", 40),
                finishing = j.optInt("finishing", 45),
                defense = j.optInt("defense", 35),
                courtVision = j.optInt("courtVision", 40),
                confidence = j.optInt("confidence", 35),
                leadership = j.optInt("leadership", 30),
                weakHand = j.optInt("weakHand", 25),
            )
        } catch (_: Exception) {
            defaultProfile()
        }
    }

    private fun saveProfile(profile: PlayerProfile) {
        val j = JSONObject().apply {
            put("playerName", profile.playerName)
            put("position", profile.position)
            put("gradYear", profile.gradYear)
            put("currentLevel", profile.currentLevel)
            put("xp", profile.xp)
            put("streakDays", profile.streakDays)
            put("dailyAffirmation", profile.dailyAffirmation)
            put("assignedFocus", profile.assignedFocus)
            put("weeklyGoal", profile.weeklyGoal)
            put("fatigueLevel", profile.fatigueLevel)
            put("tryoutReadiness", profile.tryoutReadiness)
            put("shooting", profile.shooting)
            put("ballHandling", profile.ballHandling)
            put("finishing", profile.finishing)
            put("defense", profile.defense)
            put("courtVision", profile.courtVision)
            put("confidence", profile.confidence)
            put("leadership", profile.leadership)
            put("weakHand", profile.weakHand)
        }
        prefs.edit().putString(KEY_PROFILE, j.toString()).apply()
    }

    private fun loadWorkouts(): List<WorkoutLog> {
        val raw = prefs.getString(KEY_WORKOUTS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val j = arr.getJSONObject(idx)
                WorkoutLog(
                    id = j.optString("id"),
                    userId = "local",
                    dayKey = j.optString("dayKey", "DAY_01"),
                    title = j.optString("title"),
                    focusArea = j.optString("focusArea"),
                    durationMinutes = j.optInt("durationMinutes", 30),
                    drillsCompleted = j.optInt("drillsCompleted", 1),
                    totalDrills = j.optInt("totalDrills", 1),
                    xpEarned = j.optInt("xpEarned", 0),
                    rpeFatigue = j.optInt("rpeFatigue", 5),
                    notes = j.optString("notes").takeIf { it.isNotBlank() },
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveWorkouts(list: List<WorkoutLog>) {
        val arr = JSONArray()
        list.forEach { w ->
            arr.put(
                JSONObject().apply {
                    put("id", w.id)
                    put("dayKey", w.dayKey)
                    put("title", w.title)
                    put("focusArea", w.focusArea)
                    put("durationMinutes", w.durationMinutes)
                    put("drillsCompleted", w.drillsCompleted)
                    put("totalDrills", w.totalDrills)
                    put("xpEarned", w.xpEarned)
                    put("rpeFatigue", w.rpeFatigue)
                    put("notes", w.notes.orEmpty())
                }
            )
        }
        prefs.edit().putString(KEY_WORKOUTS, arr.toString()).apply()
    }

    private fun loadMissions(): List<MindsetMission> {
        val raw = prefs.getString(KEY_MISSIONS, null)
        if (raw == null) {
            val seeded = defaultMissions()
            saveMissions(seeded)
            return seeded
        }
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val j = arr.getJSONObject(idx)
                MindsetMission(
                    id = j.optString("id"),
                    userId = "local",
                    title = j.optString("title"),
                    category = j.optString("category", "CONFIDENCE"),
                    completed = j.optBoolean("completed", false),
                    targetCount = j.optInt("targetCount", 1),
                    currentCount = j.optInt("currentCount", 0),
                    xpReward = j.optInt("xpReward", 60),
                )
            }
        } catch (_: Exception) {
            defaultMissions()
        }
    }

    private fun saveMissions(list: List<MindsetMission>) {
        val arr = JSONArray()
        list.forEach { m ->
            arr.put(
                JSONObject().apply {
                    put("id", m.id)
                    put("title", m.title)
                    put("category", m.category)
                    put("completed", m.completed)
                    put("targetCount", m.targetCount)
                    put("currentCount", m.currentCount)
                    put("xpReward", m.xpReward)
                }
            )
        }
        prefs.edit().putString(KEY_MISSIONS, arr.toString()).apply()
    }

    private fun loadObservations(): List<CoachObservation> {
        val raw = prefs.getString(KEY_OBSERVATIONS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val j = arr.getJSONObject(idx)
                CoachObservation(
                    id = j.optString("id"),
                    userId = "local",
                    authorRole = j.optString("authorRole", AuthorRole.PARENT.name),
                    focusTag = j.optString("focusTag", "Confidence"),
                    note = j.optString("note"),
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveObservations(list: List<CoachObservation>) {
        val arr = JSONArray()
        list.forEach { o ->
            arr.put(
                JSONObject().apply {
                    put("id", o.id)
                    put("authorRole", o.authorRole)
                    put("focusTag", o.focusTag)
                    put("note", o.note)
                }
            )
        }
        prefs.edit().putString(KEY_OBSERVATIONS, arr.toString()).apply()
    }

    private fun loadSchedule(): List<ScheduleDay> {
        val raw = prefs.getString(KEY_SCHEDULE, null)
        if (raw == null) {
            val seeded = defaultSchedule()
            saveSchedule(seeded)
            return seeded
        }
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val j = arr.getJSONObject(idx)
                ScheduleDay(
                    id = j.optString("id"),
                    userId = "local",
                    dayIndex = j.optInt("dayIndex", idx),
                    dayLabel = j.optString("dayLabel"),
                    focusTitle = j.optString("focusTitle"),
                    durationMinutes = j.optInt("durationMinutes", 30),
                    schoolLoad = j.optString("schoolLoad", SchoolLoad.NONE.name),
                    completedThisWeek = j.optBoolean("completedThisWeek", false),
                )
            }
        } catch (_: Exception) {
            defaultSchedule()
        }
    }

    private fun saveSchedule(list: List<ScheduleDay>) {
        val arr = JSONArray()
        list.forEach { s ->
            arr.put(
                JSONObject().apply {
                    put("id", s.id)
                    put("dayIndex", s.dayIndex)
                    put("dayLabel", s.dayLabel)
                    put("focusTitle", s.focusTitle)
                    put("durationMinutes", s.durationMinutes)
                    put("schoolLoad", s.schoolLoad)
                    put("completedThisWeek", s.completedThisWeek)
                }
            )
        }
        prefs.edit().putString(KEY_SCHEDULE, arr.toString()).apply()
    }

    private fun defaultProfile(): PlayerProfile = PlayerProfile(
        userId = "local",
        playerName = "Future Floor General",
        position = "PG / SG",
        gradYear = 2032,
        currentLevel = 1,
        xp = 250,
        streakDays = 1,
        dailyAffirmation = BasketballCurriculum.affirmations.first(),
        assignedFocus = "Control, close-range form & fearless voice",
        weeklyGoal = "Call for the ball 5 times in pickup or scrimmage",
        fatigueLevel = FatigueLevel.FRESH.name,
        tryoutReadiness = 42,
        shooting = 65,
        ballHandling = 40,
        finishing = 45,
        defense = 35,
        courtVision = 40,
        confidence = 35,
        leadership = 30,
        weakHand = 25,
    )

    private fun defaultMissions(): List<MindsetMission> =
        BasketballCurriculum.defaultMissions.mapIndexed { idx, (title, cat, target) ->
            MindsetMission(
                id = "mission_$idx",
                userId = "local",
                title = title,
                category = cat,
                completed = false,
                targetCount = target,
                currentCount = 0,
                xpReward = 60,
            )
        }

    private fun defaultSchedule(): List<ScheduleDay> =
        BasketballCurriculum.defaultSchedule.mapIndexed { idx, (label, focus, mins) ->
            ScheduleDay(
                id = "day_$idx",
                userId = "local",
                dayIndex = idx,
                dayLabel = label,
                focusTitle = focus,
                durationMinutes = mins,
                schoolLoad = if (idx == 6) SchoolLoad.REST.name else SchoolLoad.NONE.name,
                completedThisWeek = false,
            )
        }

    private companion object {
        const val KEY_PROFILE = "profile_json"
        const val KEY_WORKOUTS = "workouts_json"
        const val KEY_MISSIONS = "missions_json"
        const val KEY_OBSERVATIONS = "observations_json"
        const val KEY_SCHEDULE = "schedule_json"
    }
}
