package com.example

import com.example.data.ProgressionSystem
import com.example.model.PlayerProfile
import com.example.model.ShotZoneStat
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressionSystemTest {
    @Test
    fun shotXpRewardsAttemptsAndMakes() {
        assertEquals(25, ProgressionSystem.computeShotXpGain(makes = 3, attempts = 5))
    }

    @Test
    fun shotXpClampsInvalidInput() {
        assertEquals(0, ProgressionSystem.computeShotXpGain(makes = 4, attempts = -2))
        assertEquals(28, ProgressionSystem.computeShotXpGain(makes = 9, attempts = 4))
    }

    @Test
    fun levelsFollowXpThresholdsWithoutDowngrading() {
        assertEquals(1, ProgressionSystem.levelForXp(399))
        assertEquals(2, ProgressionSystem.levelForXp(400))
        assertEquals(6, ProgressionSystem.levelForXp(3_000))
        assertEquals(4, ProgressionSystem.resolveLevel(currentLevel = 4, xp = 0))
    }

    @Test
    fun progressionCountsShotLabXpAcrossZones() {
        val profile = PlayerProfile(xp = 250, currentLevel = 1)
        val zones = listOf(
            ShotZoneStat(id = "paint", makes = 4, attempts = 6),
            ShotZoneStat(id = "top_key_3", makes = 2, attempts = 3)
        )

        val snapshot = ProgressionSystem.evaluateProgression(profile, zones)

        assertEquals(48, snapshot.shotLabXp)
        assertEquals(1, snapshot.badges.first { it.id == "first_swish" }.progressCurrent)
        assertEquals(9, snapshot.badges.first { it.id == "rep_club_25" }.progressCurrent)
    }
}
