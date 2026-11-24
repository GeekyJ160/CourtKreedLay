package com.example.data

import android.util.Log
import com.example.model.CoachObservation
import com.example.model.FatigueLevel
import com.example.model.MindsetMission
import com.example.model.PlayerProfile
import com.example.model.ShotZoneStat
import com.example.model.careerAccuracyPercent
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

data class CoachChatMessage(
    val id: String,
    val fromCoach: Boolean,
    val text: String,
)

data class PersonalizedFocusPlan(
    val headline: String,
    val confidenceCue: String,
    val drills: List<String>,
    val parentNote: String,
)

class CoachIqAiRepository {

    private val chatModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are Coach IQ, a supportive, sharp, and encouraging personal basketball development coach for a rising 7th-grade girl (currently finishing 6th grade) preparing for middle school basketball tryouts as a combo guard (PG/SG).
                    Her dad and she are building her game together.
                    Core coaching philosophy:
                    - Reward making the RIGHT basketball decision (taking an open shot, attacking an open lane, calling for the ball, speaking up in a huddle) even when the shot misses.
                    - Emphasize controlled aggression, weak-hand confidence, close-to-midrange shooting mechanics before forcing deep range, and vocal leadership.
                    - Reference her actual player models when helpful: Caitlin Clark (range & vision), Paige Bueckers (midrange pace & composure), JuJu Watkins (attacking rim & assertiveness), Steph Curry (form & off-ball movement), Chelsea Gray (floor general passing & leadership).
                    - Keep answers concise, actionable, energizing, and age-appropriate (no clichés, clear 2–4 bullet drills or mindset cues).
                    """.trimIndent()
                )
            }
        )
    }

    private val focusPlanSchema = Schema.obj(
        properties = mapOf(
            "headline" to Schema.string(description = "Short energetic title for today's focus plan"),
            "confidenceCue" to Schema.string(description = "1-2 sentence mindset & decision-making encouragement"),
            "drills" to Schema.array(
                items = Schema.string(),
                description = "Exactly 3 specific, actionable 5-10 minute drills tailored to her weak spots and observations"
            ),
            "parentNote" to Schema.string(description = "1 short tip for how Dad can rebound, cue, or encourage her during this session")
        )
    )

    private val structuredPlanModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = focusPlanSchema
            }
        )
    }

    fun buildPlayerContextSummary(
        profile: PlayerProfile?,
        shotZones: List<ShotZoneStat>,
        missions: List<MindsetMission>,
        observations: List<CoachObservation>,
    ): String {
        if (profile == null) return "Player profile is initializing."
        val weakestAttr = listOf(
            "Shooting" to profile.shooting,
            "Ball Handling" to profile.ballHandling,
            "Finishing" to profile.finishing,
            "Defense" to profile.defense,
            "Court Vision" to profile.courtVision,
            "Confidence" to profile.confidence,
            "Leadership" to profile.leadership,
            "Weak Hand" to profile.weakHand,
        ).minByOrNull { it.second }

        val trackedZones = shotZones.filter { it.attempts > 0 }
        val shotSummary = if (trackedZones.isEmpty()) {
            "No shots logged yet in Shot Lab."
        } else {
            trackedZones.joinToString("; ") {
                "${it.zoneName}: ${it.makes}/${it.attempts} (${it.careerAccuracyPercent()}%)"
            }
        }
        val completedMissions = missions.count { it.completed }
        val recentNotes = observations.take(3).joinToString(" | ") {
            "[${it.authorRole} - ${it.focusTag}]: ${it.note}"
        }.ifBlank { "No recent parent/player notes yet." }

        val fatigue = FatigueLevel.safeValueOf(profile.fatigueLevel)

        return """
            Player: ${profile.playerName} (${profile.position}, Level ${profile.currentLevel}, XP ${profile.xp}, Readiness ${profile.tryoutReadiness}%)
            Ratings: Shooting=${profile.shooting}, BallHandling=${profile.ballHandling}, Finishing=${profile.finishing}, Defense=${profile.defense}, CourtVision=${profile.courtVision}, Confidence=${profile.confidence}, Leadership=${profile.leadership}, WeakHand=${profile.weakHand}
            Lowest Attribute: ${weakestAttr?.first} (${weakestAttr?.second})
            Fatigue / School Load: ${fatigue.label} (${fatigue.guidance})
            Parent Assigned Focus: ${profile.assignedFocus}
            Weekly Goal: ${profile.weeklyGoal}
            Fearless Challenges Completed: $completedMissions / ${missions.size}
            Shot Lab Stats: $shotSummary
            Recent Observations: $recentNotes
        """.trimIndent()
    }

    fun streamCoachResponse(
        userMessage: String,
        playerContext: String,
    ): Flow<String> = flow {
        val prompt = """
            Current Player Context:
            $playerContext

            Player / Parent Question:
            $userMessage
        """.trimIndent()

        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                chatModel.generateContentStream(prompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("Coach IQ returned an empty response.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Coach IQ streaming timed out", e)
            throw IllegalStateException("Coach IQ timed out after 60 seconds. Please try again.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Coach IQ streaming failed", e)
            throw e
        }
    }

    suspend fun generateStructuredFocusPlan(playerContext: String): PersonalizedFocusPlan {
        val prompt = """
            Based on this 6th-grade combo guard's live training data, create a personalized 25-minute Focus Plan that builds her confidence, weak hand, and smart decision-making.
            $playerContext
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                structuredPlanModel.generateContent(prompt)
            }
            val jsonString = response.text
                ?: throw IllegalStateException("Coach IQ returned an empty focus plan.")
            val json = JSONObject(jsonString)
            val headline = json.getString("headline")
            val confidenceCue = json.getString("confidenceCue")
            val drillsArray = json.getJSONArray("drills")
            val drills = (0 until drillsArray.length()).map { drillsArray.getString(it) }
            val parentNote = json.getString("parentNote")
            PersonalizedFocusPlan(
                headline = headline,
                confidenceCue = confidenceCue,
                drills = drills,
                parentNote = parentNote
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Structured plan timed out", e)
            throw IllegalStateException("Focus plan generation timed out after 60 seconds.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate structured focus plan", e)
            throw e
        }
    }

    private companion object {
        const val TAG = "CoachIqAiRepo"
    }
}
