package com.example.data

import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.AuthorRole
import com.example.model.CoachObservation
import com.example.model.FatigueLevel
import com.example.model.MindsetMission
import com.example.model.PlayerProfile
import com.example.model.ScheduleDay
import com.example.model.SchoolLoad
import com.example.model.ShotZoneStat
import com.example.model.WorkoutLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CourtIqRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    private fun requireUserId(expectedUserId: String? = null): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        if (expectedUserId != null && expectedUserId != uid) {
            throw IllegalStateException("Authenticated user mismatch.")
        }
        return uid
    }

    private fun <T> Flow<T>.handleFirestoreErrors(
        operationType: OperationType,
        path: String,
        uid: String,
    ): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
        throw error
    }

    fun observeProfile(userId: String): Flow<PlayerProfile?> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            PlayerProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .handleFirestoreErrors(OperationType.GET, path, uid)
        )
    }

    fun observeWorkouts(userId: String): Flow<List<WorkoutLog>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/workouts"
        emitAll(
            db.collection("users").document(uid).collection("workouts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(30)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        WorkoutLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .handleFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeShotZones(userId: String): Flow<List<ShotZoneStat>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/shotZones"
        emitAll(
            db.collection("users").document(uid).collection("shotZones")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        ShotZoneStat::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .handleFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeMissions(userId: String): Flow<List<MindsetMission>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/mindsetMissions"
        emitAll(
            db.collection("users").document(uid).collection("mindsetMissions")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        MindsetMission::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .handleFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeObservations(userId: String): Flow<List<CoachObservation>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/observations"
        emitAll(
            db.collection("users").document(uid).collection("observations")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(25)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        CoachObservation::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .handleFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeSchedule(userId: String): Flow<List<ScheduleDay>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/schedule"
        emitAll(
            db.collection("users").document(uid).collection("schedule")
                .orderBy("dayIndex", Query.Direction.ASCENDING)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        ScheduleDay::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .handleFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun ensureInitialData(userId: String, displayName: String?): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val userRef = db.collection("users").document(uid)
            val existing = userRef.get().await()
            if (existing.exists()) {
                return Result.success(Unit)
            }

            val cleanName = displayName?.trim()?.takeIf { it.isNotEmpty() }?.take(36)
                ?: "Future Floor General"
            val batch = db.batch()
            val now = FieldValue.serverTimestamp()

            val profileMap = mapOf(
                "userId" to uid,
                "playerName" to cleanName,
                "position" to "PG / SG",
                "gradYear" to 2032,
                "currentLevel" to 1,
                "xp" to 250,
                "streakDays" to 1,
                "dailyAffirmation" to BasketballCurriculum.affirmations.first(),
                "assignedFocus" to "Control, close-range form & fearless voice",
                "weeklyGoal" to "Call for the ball 5 times in pickup or scrimmage",
                "fatigueLevel" to FatigueLevel.FRESH.name,
                "tryoutReadiness" to 42,
                "shooting" to 65,
                "ballHandling" to 40,
                "finishing" to 45,
                "defense" to 35,
                "courtVision" to 40,
                "confidence" to 35,
                "leadership" to 30,
                "weakHand" to 25,
                "createdAt" to now,
                "updatedAt" to now,
            )
            batch.set(userRef, profileMap)

            BasketballCurriculum.courtZones.forEach { zone ->
                val zoneRef = userRef.collection("shotZones").document(zone.id)
                batch.set(
                    zoneRef,
                    mapOf(
                        "userId" to uid,
                        "zoneName" to zone.name,
                        "makes" to 0,
                        "attempts" to 0,
                        "sessionMakes" to 0,
                        "sessionAttempts" to 0,
                        "createdAt" to now,
                        "updatedAt" to now,
                    )
                )
            }

            BasketballCurriculum.defaultMissions.forEachIndexed { idx, (title, cat, target) ->
                val mRef = userRef.collection("mindsetMissions").document("mission_$idx")
                batch.set(
                    mRef,
                    mapOf(
                        "userId" to uid,
                        "title" to title,
                        "category" to cat,
                        "completed" to false,
                        "targetCount" to target,
                        "currentCount" to 0,
                        "xpReward" to 60,
                        "createdAt" to now,
                        "updatedAt" to now,
                    )
                )
            }

            BasketballCurriculum.defaultSchedule.forEachIndexed { idx, (label, focus, mins) ->
                val sRef = userRef.collection("schedule").document("day_$idx")
                val load = if (idx == 6) SchoolLoad.REST.name else SchoolLoad.NONE.name
                batch.set(
                    sRef,
                    mapOf(
                        "userId" to uid,
                        "dayIndex" to idx,
                        "dayLabel" to label,
                        "focusTitle" to focus,
                        "durationMinutes" to mins,
                        "schoolLoad" to load,
                        "completedThisWeek" to false,
                        "createdAt" to now,
                        "updatedAt" to now,
                    )
                )
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun completeWorkout(
        userId: String,
        profile: PlayerProfile,
        workout: WorkoutProgram,
        drillsCompleted: Int,
        rpeFatigue: Int,
        notes: String?,
        scheduleDayId: String?,
    ): Result<Unit> {
        val path = "users/$userId/workouts"
        return try {
            val uid = requireUserId(userId)
            val userRef = db.collection("users").document(uid)
            val workoutRef = userRef.collection("workouts").document()
            val safeDrills = drillsCompleted.coerceIn(0, workout.drills.size.coerceAtLeast(1))
            val safeRpe = rpeFatigue.coerceIn(1, 10)
            val earnedXp = if (safeDrills >= workout.drills.size) {
                workout.xpReward
            } else {
                ((workout.xpReward.toDouble() / workout.drills.size.coerceAtLeast(1)) * safeDrills).toInt()
                    .coerceAtLeast(25)
            }
            val now = FieldValue.serverTimestamp()

            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "dayKey" to workout.dayKey.take(20),
                "title" to workout.title.take(100),
                "focusArea" to workout.levelTag.take(60),
                "durationMinutes" to workout.durationMinutes.coerceIn(1, 180),
                "drillsCompleted" to safeDrills,
                "totalDrills" to workout.drills.size.coerceIn(1, 20),
                "xpEarned" to earnedXp.coerceIn(0, 2000),
                "rpeFatigue" to safeRpe,
                "createdAt" to now,
                "updatedAt" to now,
            )
            val trimmedNotes = notes?.trim()?.takeIf { it.isNotEmpty() }?.take(300)
            if (trimmedNotes != null) {
                payload["notes"] = trimmedNotes
            }

            val newFatigue = when {
                safeRpe >= 8 -> FatigueLevel.TIRED.name
                safeRpe >= 5 -> FatigueLevel.MODERATE.name
                else -> FatigueLevel.FRESH.name
            }

            val nextXp = (profile.xp + earnedXp).coerceIn(0, 1_000_000)
            val nextLevel = ProgressionSystem.resolveLevel(profile.currentLevel, nextXp)
            val batch = db.batch()
            batch.set(workoutRef, payload)
            batch.update(
                userRef,
                mapOf(
                    "xp" to nextXp,
                    "currentLevel" to nextLevel,
                    "streakDays" to (profile.streakDays + 1).coerceIn(0, 5000),
                    "weakHand" to (profile.weakHand + 1).coerceIn(0, 99),
                    "ballHandling" to (profile.ballHandling + 1).coerceIn(0, 99),
                    "confidence" to (profile.confidence + 1).coerceIn(0, 99),
                    "fatigueLevel" to newFatigue,
                    "tryoutReadiness" to (profile.tryoutReadiness + 2).coerceIn(0, 100),
                    "updatedAt" to now,
                )
            )
            if (!scheduleDayId.isNullOrBlank()) {
                val schedRef = userRef.collection("schedule").document(scheduleDayId)
                batch.update(
                    schedRef,
                    mapOf(
                        "completedThisWeek" to true,
                        "updatedAt" to now,
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun recordShots(
        userId: String,
        zone: CourtZoneDef,
        addMakes: Int,
        addAttempts: Int,
        currentProfile: PlayerProfile? = null,
    ): Result<Unit> {
        val docPath = "users/$userId/shotZones/${zone.id}"
        return try {
            val uid = requireUserId(userId)
            require(addAttempts > 0 && addMakes in 0..addAttempts) {
                "Invalid shot count: makes=$addMakes, attempts=$addAttempts"
            }
            val userRef = db.collection("users").document(uid)
            val zoneRef = userRef.collection("shotZones").document(zone.id)
            val now = FieldValue.serverTimestamp()
            val xpGain = ProgressionSystem.computeShotXpGain(addMakes, addAttempts)

            db.runTransaction { tx ->
                val snap = tx.get(zoneRef)
                if (!snap.exists()) {
                    tx.set(
                        zoneRef,
                        mapOf(
                            "userId" to uid,
                            "zoneName" to zone.name.take(50),
                            "makes" to addMakes.coerceIn(0, 100000),
                            "attempts" to addAttempts.coerceIn(0, 100000),
                            "sessionMakes" to addMakes.coerceIn(0, 10000),
                            "sessionAttempts" to addAttempts.coerceIn(0, 10000),
                            "createdAt" to now,
                            "updatedAt" to now,
                        )
                    )
                } else {
                    val curMakes = (snap.getLong("makes") ?: 0L).toInt()
                    val curAttempts = (snap.getLong("attempts") ?: 0L).toInt()
                    val curSessMakes = (snap.getLong("sessionMakes") ?: 0L).toInt()
                    val curSessAttempts = (snap.getLong("sessionAttempts") ?: 0L).toInt()
                    val nextAttempts = (curAttempts + addAttempts).coerceIn(0, 100000)
                    val nextMakes = (curMakes + addMakes).coerceIn(0, nextAttempts)
                    val nextSessAttempts = (curSessAttempts + addAttempts).coerceIn(0, 10000)
                    val nextSessMakes = (curSessMakes + addMakes).coerceIn(0, nextSessAttempts)
                    tx.update(
                        zoneRef,
                        mapOf(
                            "makes" to nextMakes,
                            "attempts" to nextAttempts,
                            "sessionMakes" to nextSessMakes,
                            "sessionAttempts" to nextSessAttempts,
                            "updatedAt" to now,
                        )
                    )
                }
                if (currentProfile != null && xpGain > 0) {
                    val nextXp = (currentProfile.xp + xpGain).coerceIn(0, 1_000_000)
                    val nextLevel = ProgressionSystem.resolveLevel(currentProfile.currentLevel, nextXp)
                    tx.update(
                        userRef,
                        mapOf(
                            "xp" to nextXp,
                            "currentLevel" to nextLevel,
                            "updatedAt" to now,
                        )
                    )
                }
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun resetShotSession(userId: String, zones: List<ShotZoneStat>): Result<Unit> {
        val path = "users/$userId/shotZones"
        return try {
            val uid = requireUserId(userId)
            val batch = db.batch()
            val now = FieldValue.serverTimestamp()
            zones.forEach { z ->
                if (z.id.isNotBlank()) {
                    val ref = db.collection("users").document(uid)
                        .collection("shotZones").document(z.id)
                    batch.update(
                        ref,
                        mapOf(
                            "sessionMakes" to 0,
                            "sessionAttempts" to 0,
                            "updatedAt" to now,
                        )
                    )
                }
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun incrementMissionProgress(
        userId: String,
        profile: PlayerProfile?,
        mission: MindsetMission,
    ): Result<Unit> {
        val path = "users/$userId/mindsetMissions/${mission.id}"
        return try {
            val uid = requireUserId(userId)
            require(mission.id.isNotBlank()) { "Mission ID required" }
            val nextCount = if (mission.completed) {
                mission.targetCount
            } else {
                (mission.currentCount + 1).coerceAtMost(mission.targetCount)
            }
            val nowCompleted = nextCount >= mission.targetCount
            val becameCompleted = !mission.completed && nowCompleted
            val now = FieldValue.serverTimestamp()
            val userRef = db.collection("users").document(uid)
            val missionRef = userRef.collection("mindsetMissions").document(mission.id)

            val batch = db.batch()
            batch.update(
                missionRef,
                mapOf(
                    "currentCount" to nextCount,
                    "completed" to nowCompleted,
                    "updatedAt" to now,
                )
            )
            if (becameCompleted && profile != null) {
                val nextXp = (profile.xp + mission.xpReward).coerceIn(0, 1_000_000)
                val nextLevel = ProgressionSystem.resolveLevel(profile.currentLevel, nextXp)
                batch.update(
                    userRef,
                    mapOf(
                        "xp" to nextXp,
                        "currentLevel" to nextLevel,
                        "confidence" to (profile.confidence + 2).coerceIn(0, 99),
                        "leadership" to (profile.leadership + 2).coerceIn(0, 99),
                        "updatedAt" to now,
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleMissionComplete(
        userId: String,
        profile: PlayerProfile?,
        mission: MindsetMission,
    ): Result<Unit> {
        val path = "users/$userId/mindsetMissions/${mission.id}"
        return try {
            val uid = requireUserId(userId)
            require(mission.id.isNotBlank()) { "Mission ID required" }
            val nextCompleted = !mission.completed
            val nextCount = if (nextCompleted) mission.targetCount else 0
            val now = FieldValue.serverTimestamp()
            val userRef = db.collection("users").document(uid)
            val missionRef = userRef.collection("mindsetMissions").document(mission.id)

            val batch = db.batch()
            batch.update(
                missionRef,
                mapOf(
                    "completed" to nextCompleted,
                    "currentCount" to nextCount,
                    "updatedAt" to now,
                )
            )
            if (nextCompleted && profile != null) {
                val nextXp = (profile.xp + mission.xpReward).coerceIn(0, 1_000_000)
                val nextLevel = ProgressionSystem.resolveLevel(profile.currentLevel, nextXp)
                batch.update(
                    userRef,
                    mapOf(
                        "xp" to nextXp,
                        "currentLevel" to nextLevel,
                        "confidence" to (profile.confidence + 1).coerceIn(0, 99),
                        "updatedAt" to now,
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun addCustomMission(
        userId: String,
        title: String,
        category: String,
        targetCount: Int,
    ): Result<Unit> {
        val path = "users/$userId/mindsetMissions"
        return try {
            val uid = requireUserId(userId)
            val cleanTitle = title.trim().take(140)
            require(cleanTitle.isNotEmpty()) { "Mission title cannot be empty" }
            val safeTarget = targetCount.coerceIn(1, 50)
            val now = FieldValue.serverTimestamp()
            val ref = db.collection("users").document(uid)
                .collection("mindsetMissions").document()
            ref.set(
                mapOf(
                    "userId" to uid,
                    "title" to cleanTitle,
                    "category" to category.trim().ifEmpty { "CONFIDENCE" }.take(40),
                    "completed" to false,
                    "targetCount" to safeTarget,
                    "currentCount" to 0,
                    "xpReward" to 65,
                    "createdAt" to now,
                    "updatedAt" to now,
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun resetWeeklyMissions(userId: String, missions: List<MindsetMission>): Result<Unit> {
        val path = "users/$userId/mindsetMissions"
        return try {
            val uid = requireUserId(userId)
            val batch = db.batch()
            val now = FieldValue.serverTimestamp()
            missions.forEach { m ->
                if (m.id.isNotBlank()) {
                    val ref = db.collection("users").document(uid)
                        .collection("mindsetMissions").document(m.id)
                    batch.update(
                        ref,
                        mapOf(
                            "completed" to false,
                            "currentCount" to 0,
                            "updatedAt" to now,
                        )
                    )
                }
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun addObservation(
        userId: String,
        authorRole: AuthorRole,
        focusTag: String,
        note: String,
    ): Result<Unit> {
        val path = "users/$userId/observations"
        return try {
            val uid = requireUserId(userId)
            val cleanNote = note.trim().take(500)
            val cleanTag = focusTag.trim().ifEmpty { "Confidence" }.take(40)
            require(cleanNote.isNotEmpty()) { "Observation note cannot be empty" }
            val now = FieldValue.serverTimestamp()
            val ref = db.collection("users").document(uid)
                .collection("observations").document()
            ref.set(
                mapOf(
                    "userId" to uid,
                    "authorRole" to authorRole.name,
                    "focusTag" to cleanTag,
                    "note" to cleanNote,
                    "createdAt" to now,
                    "updatedAt" to now,
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteObservation(userId: String, observationId: String): Result<Unit> {
        val path = "users/$userId/observations/$observationId"
        return try {
            val uid = requireUserId(userId)
            require(observationId.isNotBlank()) { "Observation ID required" }
            db.collection("users").document(uid)
                .collection("observations").document(observationId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        userId: String,
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
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val updates = mapOf(
                "playerName" to playerName.trim().ifEmpty { "Future Floor General" }.take(40),
                "assignedFocus" to assignedFocus.trim().ifEmpty { "Ball control & confidence" }.take(100),
                "weeklyGoal" to weeklyGoal.trim().ifEmpty { "Attack open lanes" }.take(200),
                "dailyAffirmation" to dailyAffirmation.trim().ifEmpty { BasketballCurriculum.affirmations.first() }.take(220),
                "fatigueLevel" to fatigueLevel.name,
                "currentLevel" to currentLevel.coerceIn(1, 6),
                "tryoutReadiness" to tryoutReadiness.coerceIn(0, 100),
                "shooting" to shooting.coerceIn(0, 99),
                "ballHandling" to ballHandling.coerceIn(0, 99),
                "finishing" to finishing.coerceIn(0, 99),
                "defense" to defense.coerceIn(0, 99),
                "courtVision" to courtVision.coerceIn(0, 99),
                "confidence" to confidence.coerceIn(0, 99),
                "leadership" to leadership.coerceIn(0, 99),
                "weakHand" to weakHand.coerceIn(0, 99),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            db.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateScheduleDay(
        userId: String,
        dayId: String,
        focusTitle: String,
        durationMinutes: Int,
        schoolLoad: SchoolLoad,
        completedThisWeek: Boolean,
    ): Result<Unit> {
        val path = "users/$userId/schedule/$dayId"
        return try {
            val uid = requireUserId(userId)
            require(dayId.isNotBlank()) { "Schedule day ID required" }
            db.collection("users").document(uid)
                .collection("schedule").document(dayId)
                .update(
                    mapOf(
                        "focusTitle" to focusTitle.trim().ifEmpty { "Skill session" }.take(80),
                        "durationMinutes" to durationMinutes.coerceIn(0, 120),
                        "schoolLoad" to schoolLoad.name,
                        "completedThisWeek" to completedThisWeek,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}
