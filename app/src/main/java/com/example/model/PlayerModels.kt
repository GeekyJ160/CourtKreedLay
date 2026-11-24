package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlin.math.roundToInt

enum class FatigueLevel(val label: String, val guidance: String) {
    FRESH("Fresh", "Full 30-min intensity ready"),
    MODERATE("Moderate", "Standard pace with mindful rest"),
    TIRED("Tired / Game Day", "Light ball-handling & film study recommended");

    companion object {
        fun safeValueOf(raw: String?): FatigueLevel =
            entries.firstOrNull { it.name == raw } ?: FRESH
    }
}

enum class SchoolLoad(val label: String) {
    NONE("No Team Session"),
    PRACTICE("School Practice"),
    GAME("Game Day"),
    REST("Active Recovery");

    companion object {
        fun safeValueOf(raw: String?): SchoolLoad =
            entries.firstOrNull { it.name == raw } ?: NONE
    }
}

enum class AuthorRole(val label: String) {
    PARENT("Coach / Parent"),
    PLAYER("Player Courage Log"),
    COACH_IQ("Coach IQ");

    companion object {
        fun safeValueOf(raw: String?): AuthorRole =
            entries.firstOrNull { it.name == raw } ?: PARENT
    }
}

@Keep
@IgnoreExtraProperties
data class PlayerProfile(
    val userId: String = "",
    val playerName: String = "Future Floor General",
    val position: String = "PG / SG",
    val gradYear: Int = 2032,
    val currentLevel: Int = 1,
    val xp: Int = 250,
    val streakDays: Int = 1,
    val dailyAffirmation: String = "I don't need permission to be great. I trust my preparation.",
    val assignedFocus: String = "Control, shooting form & fearless confidence",
    val weeklyGoal: String = "Call for the ball 5 times in pickup or scrimmage",
    val fatigueLevel: String = FatigueLevel.FRESH.name,
    val tryoutReadiness: Int = 45,
    val shooting: Int = 65,
    val ballHandling: Int = 40,
    val finishing: Int = 45,
    val defense: Int = 35,
    val courtVision: Int = 40,
    val confidence: Int = 35,
    val leadership: Int = 30,
    val weakHand: Int = 25,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

fun PlayerProfile.computeOverallRating(): Int {
    val sum = shooting + ballHandling + finishing + defense +
        courtVision + confidence + leadership + weakHand
    return (sum / 8.0).roundToInt().coerceIn(0, 99)
}

@Keep
@IgnoreExtraProperties
data class WorkoutLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dayKey: String = "DAY_01",
    val title: String = "",
    val focusArea: String = "",
    val durationMinutes: Int = 30,
    val drillsCompleted: Int = 0,
    val totalDrills: Int = 1,
    val xpEarned: Int = 0,
    val rpeFatigue: Int = 5,
    val notes: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ShotZoneStat(
    @DocumentId val id: String = "",
    val userId: String = "",
    val zoneName: String = "",
    val makes: Int = 0,
    val attempts: Int = 0,
    val sessionMakes: Int = 0,
    val sessionAttempts: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

fun ShotZoneStat.careerAccuracyPercent(): Int =
    if (attempts <= 0) 0 else ((makes.toDouble() / attempts.toDouble()) * 100.0).roundToInt()

fun ShotZoneStat.sessionAccuracyPercent(): Int =
    if (sessionAttempts <= 0) 0 else ((sessionMakes.toDouble() / sessionAttempts.toDouble()) * 100.0).roundToInt()

@Keep
@IgnoreExtraProperties
data class MindsetMission(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "CONFIDENCE",
    val completed: Boolean = false,
    val targetCount: Int = 1,
    val currentCount: Int = 0,
    val xpReward: Int = 50,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class CoachObservation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorRole: String = AuthorRole.PARENT.name,
    val focusTag: String = "Confidence",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ScheduleDay(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dayIndex: Int = 0,
    val dayLabel: String = "Monday",
    val focusTitle: String = "Handles and weak hand",
    val durationMinutes: Int = 30,
    val schoolLoad: String = SchoolLoad.NONE.name,
    val completedThisWeek: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)
