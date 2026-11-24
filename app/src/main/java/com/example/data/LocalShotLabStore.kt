package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ShotZoneStat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists Shot Lab makes, attempts, and session counters locally on-device
 * using SharedPreferences so shot tracking works immediately and offline.
 */
class LocalShotLabStore(
    context: Context,
    private val userId: String,
) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        "court_iq_shot_lab_${userId.ifBlank { "local" }}",
        Context.MODE_PRIVATE
    )

    private val _shotZones = MutableStateFlow(loadAllZones())
    val shotZones: StateFlow<List<ShotZoneStat>> = _shotZones.asStateFlow()

    @Synchronized
    fun recordShots(
        zone: CourtZoneDef,
        addMakes: Int,
        addAttempts: Int,
    ) {
        if (addAttempts <= 0 || addMakes !in 0..addAttempts) return

        val curMakes = prefs.getInt(key(zone.id, "makes"), 0)
        val curAttempts = prefs.getInt(key(zone.id, "attempts"), 0)
        val curSessionMakes = prefs.getInt(key(zone.id, "sessionMakes"), 0)
        val curSessionAttempts = prefs.getInt(key(zone.id, "sessionAttempts"), 0)

        val nextAttempts = (curAttempts + addAttempts).coerceIn(0, 100_000)
        val nextMakes = (curMakes + addMakes).coerceIn(0, nextAttempts)
        val nextSessionAttempts = (curSessionAttempts + addAttempts).coerceIn(0, 10_000)
        val nextSessionMakes = (curSessionMakes + addMakes).coerceIn(0, nextSessionAttempts)

        prefs.edit()
            .putInt(key(zone.id, "makes"), nextMakes)
            .putInt(key(zone.id, "attempts"), nextAttempts)
            .putInt(key(zone.id, "sessionMakes"), nextSessionMakes)
            .putInt(key(zone.id, "sessionAttempts"), nextSessionAttempts)
            .apply()

        _shotZones.value = loadAllZones()
    }

    @Synchronized
    fun resetSession() {
        val editor = prefs.edit()
        BasketballCurriculum.courtZones.forEach { zone ->
            editor.putInt(key(zone.id, "sessionMakes"), 0)
            editor.putInt(key(zone.id, "sessionAttempts"), 0)
        }
        editor.apply()
        _shotZones.value = loadAllZones()
    }

    @Synchronized
    fun seedFromCloudIfEmpty(cloudZones: List<ShotZoneStat>) {
        if (cloudZones.isEmpty()) return
        val localTotalAttempts = _shotZones.value.sumOf { it.attempts }
        val cloudTotalAttempts = cloudZones.sumOf { it.attempts }
        if (localTotalAttempts == 0 && cloudTotalAttempts > 0) {
            val editor = prefs.edit()
            cloudZones.forEach { stat ->
                if (stat.id.isNotBlank()) {
                    editor.putInt(key(stat.id, "makes"), stat.makes.coerceAtLeast(0))
                    editor.putInt(key(stat.id, "attempts"), stat.attempts.coerceAtLeast(0))
                    editor.putInt(key(stat.id, "sessionMakes"), stat.sessionMakes.coerceAtLeast(0))
                    editor.putInt(key(stat.id, "sessionAttempts"), stat.sessionAttempts.coerceAtLeast(0))
                }
            }
            editor.apply()
            _shotZones.value = loadAllZones()
        }
    }

    private fun loadAllZones(): List<ShotZoneStat> =
        BasketballCurriculum.courtZones.map { zone ->
            ShotZoneStat(
                id = zone.id,
                userId = userId,
                zoneName = zone.name,
                makes = prefs.getInt(key(zone.id, "makes"), 0),
                attempts = prefs.getInt(key(zone.id, "attempts"), 0),
                sessionMakes = prefs.getInt(key(zone.id, "sessionMakes"), 0),
                sessionAttempts = prefs.getInt(key(zone.id, "sessionAttempts"), 0),
            )
        }

    private fun key(zoneId: String, field: String): String = "${zoneId}_$field"
}
