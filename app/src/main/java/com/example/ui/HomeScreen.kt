package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BasketballCurriculum
import com.example.data.ProgressionSystem
import com.example.data.TrainingBadge
import com.example.data.WorkoutProgram
import com.example.model.FatigueLevel
import com.example.model.PlayerProfile
import com.example.model.ScheduleDay
import com.example.model.SchoolLoad
import com.example.model.computeOverallRating
import com.example.ui.theme.ArenaCyan
import com.example.ui.theme.CourtOrange
import com.example.ui.theme.ElectricVolt
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MambaGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.viewmodel.DashboardBundle
import java.util.Calendar

@Composable
fun HomeScreen(
    bundle: DashboardBundle,
    onStartWorkout: (WorkoutProgram, String?) -> Unit,
    onToggleScheduleComplete: (ScheduleDay) -> Unit,
    onOpenMindset: () -> Unit,
    onOpenShotLab: () -> Unit,
    onOpenPlayerDna: () -> Unit = {},
) {
    Dashboard(
        bundle = bundle,
        onStartWorkout = onStartWorkout,
        onToggleScheduleComplete = onToggleScheduleComplete,
        onOpenMindset = onOpenMindset,
        onOpenShotLab = onOpenShotLab,
        onOpenPlayerDna = onOpenPlayerDna,
    )
}

@Composable
fun Dashboard(
    bundle: DashboardBundle,
    onStartWorkout: (WorkoutProgram, String?) -> Unit,
    onToggleScheduleComplete: (ScheduleDay) -> Unit,
    onOpenMindset: () -> Unit,
    onOpenShotLab: () -> Unit,
    onOpenPlayerDna: () -> Unit = {},
) {
    val profile = bundle.profile ?: PlayerProfile()
    val progression = remember(profile, bundle.shotZones) {
        ProgressionSystem.evaluateProgression(profile, bundle.shotZones)
    }
    val resolvedLevel = progression.resolvedLevel
    val overall = profile.computeOverallRating()
    val levelMeta = BasketballCurriculum.evolutionLevels
        .firstOrNull { it.levelNumber == resolvedLevel }
        ?: BasketballCurriculum.evolutionLevels.first()
    val nextLevelMeta = BasketballCurriculum.evolutionLevels
        .firstOrNull { it.levelNumber == resolvedLevel + 1 }

    val dayOfWeekIndex = rememberTodayMondayIndex()
    val todaySchedule = bundle.schedule.firstOrNull { it.dayIndex == dayOfWeekIndex }
    val recommendedWorkout = rememberRecommendedWorkout(dayOfWeekIndex, profile.fatigueLevel)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 640.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Player Level Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_level_header"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = ArenaCyan.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(
                                1.dp,
                                ArenaCyan.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                        ) {
                            Text(
                                text = "LEVEL $resolvedLevel · ${levelMeta.title.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArenaCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = profile.playerName,
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${profile.position} · Class of ${profile.gradYear} · Tryout Readiness ${profile.tryoutReadiness}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // NBA 2K Overall Rating Badge
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(CourtOrange, MambaGold)
                                )
                            )
                            .padding(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                                .background(ObsidianBg),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "OVR",
                                style = MaterialTheme.typography.labelSmall,
                                color = MambaGold
                            )
                            Text(
                                text = overall.toString(),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Current XP Summary View
                CurrentXpSummaryCard(
                    currentXp = progression.totalXp,
                    shotLabXp = progression.shotLabXp,
                    currentLevel = resolvedLevel,
                    levelTitle = levelMeta.title,
                    curLevelMinXp = levelMeta.minXp,
                    nextLevelTitle = nextLevelMeta?.title,
                    nextLevelMinXp = nextLevelMeta?.minXp,
                    streakDays = profile.streakDays,
                    workoutsLogged = bundle.workouts.size,
                )

                // Training Badges Showcase
                TrainingBadgesCard(
                    badges = progression.badges,
                    unlockedCount = progression.unlockedCount,
                    onOpenShotLab = onOpenShotLab,
                )

                // Player Stat Progress Bars (Shooting, Ball Handling, Finishing, etc.)
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianBorder, MaterialTheme.shapes.large)
                        .testTag("dashboard_stats_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PLAYER STATS",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${bundle.workouts.size} WORKOUTS LOGGED",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArenaCyan
                            )
                        }
                        AttributeRow("Shooting", profile.shooting, CourtOrange)
                        AttributeRow("Ball Handling", profile.ballHandling, ElectricVolt)
                        AttributeRow("Finishing", profile.finishing, MambaGold)
                        AttributeRow("Defense", profile.defense, ArenaCyan)
                        AttributeRow("Court Vision", profile.courtVision, ArenaCyan)
                        AttributeRow("Confidence", profile.confidence, ElectricVolt)
                        AttributeRow("Leadership", profile.leadership, MambaGold)
                        AttributeRow("Weak Hand", profile.weakHand, CourtOrange)
                    }
                }

                // Today's Guided Workout Hero Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .border(1.dp, CourtOrange.copy(alpha = 0.55f), MaterialTheme.shapes.large)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_court),
                        contentDescription = "Today's workout banner",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        ObsidianBg.copy(alpha = 0.58f),
                                        ObsidianBg.copy(alpha = 0.94f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = CourtOrange,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "TODAY'S WORKOUT · ${recommendedWorkout.durationMinutes} MIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ObsidianBg,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "+${recommendedWorkout.xpReward} XP",
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricVolt
                            )
                        }

                        Text(
                            text = recommendedWorkout.title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = recommendedWorkout.focusSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val fatigue = FatigueLevel.safeValueOf(profile.fatigueLevel)
                        Surface(
                            color = ObsidianSurfaceHigh.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = ArenaCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Coach Focus: ${profile.assignedFocus} · Status: ${fatigue.label}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onStartWorkout(recommendedWorkout, todaySchedule?.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("start_todays_workout_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricVolt,
                                    contentColor = ObsidianBg
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Start Workout (${recommendedWorkout.drills.size} Drills)",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                // Daily Affirmation Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MambaGold.copy(alpha = 0.35f), MaterialTheme.shapes.large)
                        .clickable { onOpenMindset() }
                        .testTag("daily_affirmation_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MambaGold.copy(alpha = 0.16f),
                            shape = CircleShape,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = "Daily affirmation",
                                    tint = MambaGold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DAILY AFFIRMATION",
                                style = MaterialTheme.typography.labelSmall,
                                color = MambaGold
                            )
                            Text(
                                text = "\"${profile.dailyAffirmation}\"",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Weekly Goal: ${profile.weeklyGoal}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }



                // Weekly Training Schedule
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WEEKLY TRAINING SCHEDULE",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "TAP TO CHECK OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(bundle.schedule, key = { it.id }) { day ->
                            val isToday = day.dayIndex == dayOfWeekIndex
                            ScheduleDayCard(
                                day = day,
                                isToday = isToday,
                                onToggleComplete = { onToggleScheduleComplete(day) }
                            )
                        }
                    }
                }

                // Quick Jump to Shot Lab & Workouts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val totalAttempts = bundle.shotZones.sumOf { it.attempts }
                    val totalMakes = bundle.shotZones.sumOf { it.makes }
                    val pct = if (totalAttempts == 0) 0 else (totalMakes * 100) / totalAttempts
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceHigh),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenShotLab() }
                            .testTag("home_shot_lab_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "SHOT LAB",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArenaCyan
                            )
                            Text(
                                text = if (totalAttempts == 0) "Tap to log" else "$pct% FG",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$totalMakes / $totalAttempts makes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    val missionsDone = bundle.missions.count { it.completed }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceHigh),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenMindset() }
                            .testTag("home_mindset_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "FEARLESS CHALLENGES",
                                style = MaterialTheme.typography.labelSmall,
                                color = MambaGold
                            )
                            Text(
                                text = "$missionsDone / ${bundle.missions.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Weekly mindset track",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Player DNA Radar Quick Entry Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ElectricVolt.copy(alpha = 0.45f), MaterialTheme.shapes.large)
                        .clickable { onOpenPlayerDna() }
                        .testTag("home_player_dna_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PLAYER DNA RADAR",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricVolt
                            )
                            Text(
                                text = "Strengths & Shooting Profile",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Visualize Shot Lab strengths & weaknesses across 6 court axes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            color = ElectricVolt,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "OPEN",
                                style = MaterialTheme.typography.labelMedium,
                                color = ObsidianBg,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Road to 7th Grade Timeline
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianBorder, MaterialTheme.shapes.large)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = ElectricVolt
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ROAD TO 7TH GRADE TRYOUTS",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        BasketballCurriculum.roadmapPhases.forEach { phase ->
                            val isActive = profile.currentLevel in phase.levelRange
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) ElectricVolt else ObsidianBorder)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${phase.window} · ${phase.title}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isActive) ElectricVolt else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = phase.focus,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentXpSummaryCard(
    currentXp: Int,
    shotLabXp: Int,
    currentLevel: Int,
    levelTitle: String,
    curLevelMinXp: Int,
    nextLevelTitle: String?,
    nextLevelMinXp: Int?,
    streakDays: Int,
    workoutsLogged: Int,
) {
    val rawProgress = if (nextLevelMinXp != null && nextLevelMinXp > curLevelMinXp) {
        ((currentXp - curLevelMinXp).toFloat() / (nextLevelMinXp - curLevelMinXp).toFloat())
            .coerceIn(0f, 1f)
    } else {
        1f
    }
    val animatedXpProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 550),
        label = "xp_progress"
    )
    val xpRemaining = if (nextLevelMinXp != null) (nextLevelMinXp - currentXp).coerceAtLeast(0) else 0

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ElectricVolt.copy(alpha = 0.45f), MaterialTheme.shapes.large)
            .testTag("dashboard_xp_summary")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CURRENT XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricVolt
                    )
                    Text(
                        text = "$currentXp XP",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Shot Lab XP: +$shotLabXp (+7/make · +2/miss)",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaCyan
                    )
                }
                Surface(
                    color = ObsidianSurfaceHigh,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = CourtOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${streakDays}d Streak",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { animatedXpProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = ElectricVolt,
                trackColor = ObsidianSurfaceHigh
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (nextLevelTitle != null && nextLevelMinXp != null) {
                        "$xpRemaining XP to Level ${currentLevel + 1} · $nextLevelTitle"
                    } else {
                        "Max Level Unlocked · $levelTitle"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (nextLevelMinXp != null) "$currentXp / $nextLevelMinXp" else "$workoutsLogged workouts",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaCyan
                )
            }
        }
    }
}

@Composable
private fun TrainingBadgesCard(
    badges: List<TrainingBadge>,
    unlockedCount: Int,
    onOpenShotLab: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MambaGold.copy(alpha = 0.45f), MaterialTheme.shapes.large)
            .testTag("dashboard_badges_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MambaGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "TRAINING BADGES",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "$unlockedCount / ${badges.size} UNLOCKED",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricVolt
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("badges_horizontal_list")
            ) {
                items(badges, key = { it.id }) { badge ->
                    val progress = (badge.progressCurrent.toFloat() / badge.progressTarget.coerceAtLeast(1).toFloat())
                        .coerceIn(0f, 1f)
                    Surface(
                        color = if (badge.unlocked) ObsidianSurfaceHigh else ObsidianBg,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .width(172.dp)
                            .border(
                                width = if (badge.unlocked) 1.5.dp else 1.dp,
                                color = if (badge.unlocked) MambaGold else ObsidianBorder,
                                shape = MaterialTheme.shapes.medium
                            )
                            .testTag("badge_card_${badge.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = badge.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (badge.unlocked) MambaGold else ArenaCyan
                                )
                                Icon(
                                    imageVector = if (badge.unlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                    contentDescription = if (badge.unlocked) "Unlocked" else "Locked",
                                    tint = if (badge.unlocked) MambaGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = badge.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (badge.unlocked) ElectricVolt else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = badge.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                minLines = 2,
                                maxLines = 2
                            )
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (badge.unlocked) EmeraldSuccess else ArenaCyan,
                                trackColor = ObsidianSurface
                            )
                            Text(
                                text = if (badge.unlocked) "UNLOCKED" else "${badge.progressCurrent} / ${badge.progressTarget}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (badge.unlocked) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (unlockedCount < badges.size) {
                OutlinedButton(
                    onClick = onOpenShotLab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("badges_log_shots_button")
                ) {
                    Text("Record Shots in Shot Lab to Unlock More Badges")
                }
            }
        }
    }
}

@Composable
private fun StatMiniPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = ObsidianSurface,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.border(1.dp, ObsidianBorder, MaterialTheme.shapes.medium)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AttributeRow(
    name: String,
    value: Int,
    accent: Color,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (value / 99f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "attr_$name"
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = accent
            )
        }
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = accent,
            trackColor = ObsidianSurfaceHigh
        )
    }
}

@Composable
private fun ScheduleDayCard(
    day: ScheduleDay,
    isToday: Boolean,
    onToggleComplete: () -> Unit,
) {
    val load = SchoolLoad.safeValueOf(day.schoolLoad)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) ObsidianSurfaceHigh else ObsidianSurface
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .width(172.dp)
            .border(
                width = if (isToday) 1.5.dp else 1.dp,
                color = if (isToday) ElectricVolt else ObsidianBorder,
                shape = MaterialTheme.shapes.medium
            )
            .clickable { onToggleComplete() }
            .testTag("schedule_day_${day.dayIndex}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isToday) "${day.dayLabel.uppercase()} · TODAY" else day.dayLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) ElectricVolt else CourtOrange
                )
                Icon(
                    imageVector = if (day.completedThisWeek) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (day.completedThisWeek) "Completed" else "Pending",
                    tint = if (day.completedThisWeek) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = day.focusTitle,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                minLines = 2,
                maxLines = 2
            )
            Text(
                text = if (day.durationMinutes > 0) "${day.durationMinutes} min · ${load.label}" else "Active Rest · ${load.label}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun rememberTodayMondayIndex(): Int {
    val cal = Calendar.getInstance()
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        else -> 6
    }
}

private fun rememberRecommendedWorkout(dayIndex: Int, fatigueRaw: String): WorkoutProgram {
    val fatigue = FatigueLevel.safeValueOf(fatigueRaw)
    if (fatigue == FatigueLevel.TIRED) {
        return BasketballCurriculum.allWorkouts.first { it.dayKey == "WED" }
    }
    return when (dayIndex) {
        0 -> BasketballCurriculum.day01OwnTheBall
        1 -> BasketballCurriculum.allWorkouts.first { it.dayKey == "TUE" }
        2 -> BasketballCurriculum.allWorkouts.first { it.dayKey == "WED" }
        3 -> BasketballCurriculum.allWorkouts.first { it.dayKey == "THU" }
        4 -> BasketballCurriculum.allWorkouts.first { it.dayKey == "FRI" }
        else -> BasketballCurriculum.day01OwnTheBall
    }
}
