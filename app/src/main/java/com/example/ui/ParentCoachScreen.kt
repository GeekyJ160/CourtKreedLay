package com.example.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BasketballCurriculum
import com.example.model.AuthorRole
import com.example.model.FatigueLevel
import com.example.model.PlayerProfile
import com.example.model.ScheduleDay
import com.example.model.SchoolLoad
import com.example.ui.theme.ArenaCyan
import com.example.ui.theme.CourtOrange
import com.example.ui.theme.ElectricVolt
import com.example.ui.theme.MambaGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.viewmodel.DashboardBundle

@Composable
fun ParentCoachScreen(
    bundle: DashboardBundle,
    userEmail: String?,
    onSaveCalibration: (
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
    ) -> Unit,
    onAddObservation: (AuthorRole, String, String) -> Unit,
    onDeleteObservation: (String) -> Unit,
    onUpdateScheduleDay: (ScheduleDay, String, Int, SchoolLoad, Boolean) -> Unit,
    onSignOut: () -> Unit,
    onRequestSignIn: (() -> Unit)? = null,
    cloudStorageAvailable: Boolean = false,
) {
    val profile = bundle.profile ?: PlayerProfile()

    var playerName by remember(profile.playerName) { mutableStateOf(profile.playerName) }
    var assignedFocus by remember(profile.assignedFocus) { mutableStateOf(profile.assignedFocus) }
    var weeklyGoal by remember(profile.weeklyGoal) { mutableStateOf(profile.weeklyGoal) }
    var dailyAffirmation by remember(profile.dailyAffirmation) { mutableStateOf(profile.dailyAffirmation) }
    var fatigueLevel by remember(profile.fatigueLevel) { mutableStateOf(FatigueLevel.safeValueOf(profile.fatigueLevel)) }
    var currentLevel by remember(profile.currentLevel) { mutableIntStateOf(profile.currentLevel) }
    var tryoutReadiness by remember(profile.tryoutReadiness) { mutableIntStateOf(profile.tryoutReadiness) }

    var shooting by remember(profile.shooting) { mutableIntStateOf(profile.shooting) }
    var ballHandling by remember(profile.ballHandling) { mutableIntStateOf(profile.ballHandling) }
    var finishing by remember(profile.finishing) { mutableIntStateOf(profile.finishing) }
    var defense by remember(profile.defense) { mutableIntStateOf(profile.defense) }
    var courtVision by remember(profile.courtVision) { mutableIntStateOf(profile.courtVision) }
    var confidence by remember(profile.confidence) { mutableIntStateOf(profile.confidence) }
    var leadership by remember(profile.leadership) { mutableIntStateOf(profile.leadership) }
    var weakHand by remember(profile.weakHand) { mutableIntStateOf(profile.weakHand) }

    var obsFocusTag by remember { mutableStateOf("Confidence") }
    var obsNote by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 640.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PRIVATE WORKSPACE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MambaGold
                        )
                        Text(
                            text = "PARENT / COACH MODE",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Surface(
                        color = ObsidianSurfaceHigh,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = ElectricVolt, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Private", style = MaterialTheme.typography.labelSmall, color = ElectricVolt)
                        }
                    }
                }

                // Player Profile & Weekly Goals Calibration
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianBorder, MaterialTheme.shapes.large)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "PLAYER GOALS & WORKLOAD",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = playerName,
                            onValueChange = { if (it.length <= 40) playerName = it },
                            label = { Text("Player Name / Nickname") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = assignedFocus,
                            onValueChange = { if (it.length <= 100) assignedFocus = it },
                            label = { Text("Assigned Workout Focus") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = weeklyGoal,
                            onValueChange = { if (it.length <= 200) weeklyGoal = it },
                            label = { Text("Weekly Goal") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = dailyAffirmation,
                            onValueChange = { if (it.length <= 220) dailyAffirmation = it },
                            label = { Text("Daily Affirmation") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Fatigue / School Practice Load",
                            style = MaterialTheme.typography.titleSmall,
                            color = ArenaCyan
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FatigueLevel.entries.forEach { level ->
                                FilterChip(
                                    selected = fatigueLevel == level,
                                    onClick = { fatigueLevel = level },
                                    label = { Text(level.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Text(
                            text = "Evolution Level Sign-Off: Level 0$currentLevel (${BasketballCurriculum.evolutionLevels[currentLevel - 1].title})",
                            style = MaterialTheme.typography.titleSmall,
                            color = ElectricVolt
                        )
                        Slider(
                            value = currentLevel.toFloat(),
                            onValueChange = { currentLevel = it.toInt().coerceIn(1, 6) },
                            valueRange = 1f..6f,
                            steps = 4
                        )

                        Text(
                            text = "7th Grade Tryout Readiness: $tryoutReadiness%",
                            style = MaterialTheme.typography.titleSmall,
                            color = MambaGold
                        )
                        Slider(
                            value = tryoutReadiness.toFloat(),
                            onValueChange = { tryoutReadiness = it.toInt().coerceIn(0, 100) },
                            valueRange = 0f..100f
                        )
                    }
                }

                // Skill Assessment Sliders
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianBorder, MaterialTheme.shapes.large)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "SKILL ASSESSMENT CALIBRATION",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Calibrate ratings from live driveway or pickup evaluations (feeds Coach IQ).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CoachRatingSlider("Shooting", shooting) { shooting = it }
                        CoachRatingSlider("Ball Handling", ballHandling) { ballHandling = it }
                        CoachRatingSlider("Finishing", finishing) { finishing = it }
                        CoachRatingSlider("Defense", defense) { defense = it }
                        CoachRatingSlider("Court Vision", courtVision) { courtVision = it }
                        CoachRatingSlider("Confidence", confidence) { confidence = it }
                        CoachRatingSlider("Leadership", leadership) { leadership = it }
                        CoachRatingSlider("Weak Hand", weakHand) { weakHand = it }

                        Spacer(Modifier.height(6.dp))
                        Button(
                            onClick = {
                                onSaveCalibration(
                                    playerName, assignedFocus, weeklyGoal, dailyAffirmation,
                                    fatigueLevel, currentLevel, tryoutReadiness,
                                    shooting, ballHandling, finishing, defense,
                                    courtVision, confidence, leadership, weakHand
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_coach_calibration_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricVolt,
                                contentColor = ObsidianBg
                            )
                        ) {
                            Text("Save Assessments & Targets", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                // Record Parent Observation
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CourtOrange.copy(alpha = 0.45f), MaterialTheme.shapes.large)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "RECORD OBSERVATION",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Confidence", "Weak Hand", "Shooting", "Leadership").forEach { tag ->
                                FilterChip(
                                    selected = obsFocusTag == tag,
                                    onClick = { obsFocusTag = tag },
                                    label = { Text(tag, maxLines = 1) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = obsNote,
                            onValueChange = { if (it.length <= 450) obsNote = it },
                            label = { Text("Practice or game observation") },
                            placeholder = { Text("e.g., Called for the ball twice in Q3 and drove left cleanly!") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (obsNote.isNotBlank()) {
                                    onAddObservation(AuthorRole.PARENT, obsFocusTag, obsNote)
                                    obsNote = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_parent_observation_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CourtOrange,
                                contentColor = ObsidianBg
                            )
                        ) {
                            Text("Save Observation")
                        }
                    }
                }

                // School Load Toggle for Weekly Schedule
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianBorder, MaterialTheme.shapes.large)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "SCHOOL PRACTICE & GAME LOAD",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        bundle.schedule.forEach { day ->
                            val currentLoad = SchoolLoad.safeValueOf(day.schoolLoad)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = day.dayLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = day.focusTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        val nextLoad = when (currentLoad) {
                                            SchoolLoad.NONE -> SchoolLoad.PRACTICE
                                            SchoolLoad.PRACTICE -> SchoolLoad.GAME
                                            SchoolLoad.GAME -> SchoolLoad.REST
                                            SchoolLoad.REST -> SchoolLoad.NONE
                                        }
                                        onUpdateScheduleDay(
                                            day,
                                            day.focusTitle,
                                            day.durationMinutes,
                                            nextLoad,
                                            day.completedThisWeek
                                        )
                                    }
                                ) {
                                    Text(currentLoad.label, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Observation History
        items(bundle.observations, key = { it.id }) { obs ->
            val role = AuthorRole.safeValueOf(obs.authorRole)
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .border(1.dp, ObsidianBorder, MaterialTheme.shapes.medium)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${role.label.uppercase()} · ${obs.focusTag.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (role == AuthorRole.PARENT) MambaGold else ArenaCyan
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = obs.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { onDeleteObservation(obs.id) }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete observation",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Storage / Account footer
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .border(1.dp, ObsidianBorder, MaterialTheme.shapes.large)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (userEmail == null) {
                        Text(
                            text = "ON-DEVICE STORAGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArenaCyan
                        )
                        Text(
                            text = "All workouts, Shot Lab stats, Player DNA, and Coach notes are saved locally on this device — no sign-up required.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (onRequestSignIn != null) {
                            OutlinedButton(
                                onClick = onRequestSignIn,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sign_in_button")
                            ) {
                                Text("Sign in with Google")
                            }
                            Text(
                                text = "Your local profile stays separate and will not be merged automatically.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (!cloudStorageAvailable) {
                            Text(
                                text = "Cloud sign-in is not configured for this build. Your local profile is ready to use.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "ACCOUNT & SYNC",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArenaCyan
                        )
                        Text(
                            text = "Signed in as $userEmail",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sign_out_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sign Out")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachRatingSlider(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(value.toString(), style = MaterialTheme.typography.labelLarge, color = ElectricVolt)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt().coerceIn(0, 99)) },
            valueRange = 10f..99f
        )
    }
}
