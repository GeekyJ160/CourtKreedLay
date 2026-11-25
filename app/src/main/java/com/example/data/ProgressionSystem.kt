package com.example.data

import com.example.model.PlayerProfile
import com.example.model.ShotZoneStat

data class TrainingBadge(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val unlocked: Boolean,
    val progressCurrent: Int,
    val progressTarget: Int,
)

data class ProgressionSnapshot(
    val resolvedLevel: Int,
    val totalXp: Int,
    val shotLabXp: Int,
    val badges: List<TrainingBadge>,
    val unlockedCount: Int,
)

object ProgressionSystem {

    const val XP_PER_ATTEMPT = 2
    const val XP_BONUS_PER_MAKE = 5

    fun computeShotXpGain(makes: Int, attempts: Int): Int {
        val safeAttempts = attempts.coerceAtLeast(0)
        val safeMakes = makes.coerceIn(0, safeAttempts)
        return (safeAttempts * XP_PER_ATTEMPT) + (safeMakes * XP_BONUS_PER_MAKE)
    }

    fun computeShotLabXp(shotZones: List<ShotZoneStat>): Int =
        shotZones.sumOf { computeShotXpGain(it.makes, it.attempts) }

    fun levelForXp(xp: Int): Int {
        val safeXp = xp.coerceAtLeast(0)
        return BasketballCurriculum.evolutionLevels
            .lastOrNull { safeXp >= it.minXp }
            ?.levelNumber
            ?.coerceIn(1, 6)
            ?: 1
    }

    fun resolveLevel(currentLevel: Int, xp: Int): Int =
        maxOf(currentLevel.coerceIn(1, 6), levelForXp(xp))

    fun evaluateProgression(
        profile: PlayerProfile,
        shotZones: List<ShotZoneStat>,
    ): ProgressionSnapshot {
        val shotLabXp = computeShotLabXp(shotZones)
        val resolvedLevel = resolveLevel(profile.currentLevel, profile.xp)

        val totalMakes = shotZones.sumOf { it.makes }
        val totalAttempts = shotZones.sumOf { it.attempts }

        val byId = shotZones.associateBy { it.id }
        val rimMakes = listOf("paint", "left_short", "right_short").sumOf { byId[it]?.makes ?: 0 }
        val midMakes = listOf("left_elbow", "right_elbow", "ft_top").sumOf { byId[it]?.makes ?: 0 }
        val threeMakes = listOf(
            "left_corner_3",
            "right_corner_3",
            "left_wing_3",
            "right_wing_3",
            "top_key_3"
        ).sumOf { byId[it]?.makes ?: 0 }

        val badges = listOf(
            TrainingBadge(
                id = "first_swish",
                title = "First Swish",
                category = "SHOT LAB",
                description = "Make your first recorded shot in Shot Lab",
                unlocked = totalMakes >= 1,
                progressCurrent = totalMakes.coerceAtMost(1),
                progressTarget = 1,
            ),
            TrainingBadge(
                id = "rep_club_25",
                title = "25-Shot Rep Club",
                category = "VOLUME",
                description = "Log 25 total shot attempts in Shot Lab",
                unlocked = totalAttempts >= 25,
                progressCurrent = totalAttempts.coerceAtMost(25),
                progressTarget = 25,
            ),
            TrainingBadge(
                id = "rim_attacker",
                title = "Rim Attacker",
                category = "FINISHING",
                description = "Make 10 shots in Paint & Short Corners",
                unlocked = rimMakes >= 10,
                progressCurrent = rimMakes.coerceAtMost(10),
                progressTarget = 10,
            ),
            TrainingBadge(
                id = "midrange_tech",
                title = "Midrange Technician",
                category = "MIDRANGE",
                description = "Make 10 shots from Elbows & Free Throw",
                unlocked = midMakes >= 10,
                progressCurrent = midMakes.coerceAtMost(10),
                progressTarget = 10,
            ),
            TrainingBadge(
                id = "splash_range",
                title = "Splash Range",
                category = "3PT RANGE",
                description = "Make 10 three-pointers in Shot Lab",
                unlocked = threeMakes >= 10,
                progressCurrent = threeMakes.coerceAtMost(10),
                progressTarget = 10,
            ),
            TrainingBadge(
                id = "century_shooter",
                title = "100-Shot Scorer",
                category = "DEDICATION",
                description = "Log 100 total shot attempts in Shot Lab",
                unlocked = totalAttempts >= 100,
                progressCurrent = totalAttempts.coerceAtMost(100),
                progressTarget = 100,
            ),
            TrainingBadge(
                id = "level_2_unlock",
                title = "Handle Specialist",
                category = "LEVEL UP",
                description = "Reach Level 2 (400+ XP)",
                unlocked = resolvedLevel >= 2,
                progressCurrent = profile.xp.coerceAtMost(400),
                progressTarget = 400,
            ),
            TrainingBadge(
                id = "level_3_unlock",
                title = "Shot Creator",
                category = "LEVEL UP",
                description = "Reach Level 3 (900+ XP)",
                unlocked = resolvedLevel >= 3,
                progressCurrent = profile.xp.coerceAtMost(900),
                progressTarget = 900,
            ),
        )

        return ProgressionSnapshot(
            resolvedLevel = resolvedLevel,
            totalXp = profile.xp,
            shotLabXp = shotLabXp,
            badges = badges,
            unlockedCount = badges.count { it.unlocked },
        )
    }
}
