package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.LabCategory
import com.example.data.PlayerDnaModel
import com.example.data.WorkoutProgram
import com.example.model.PlayerProfile
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrainScreen(
    bundle: DashboardBundle,
    onStartWorkout: (WorkoutProgram) -> Unit,
) {
    val profile = bundle.profile ?: PlayerProfile()
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Player DNA, 1 = Evolution, 2 = Drill Labs
    var selectedPlayerId by remember { mutableStateOf(BasketballCurriculum.playerDnaModels.first().id) }
    val scenarioAnswers = remember { mutableStateMapOf<String, Int>() }
    var selectedLab by remember { mutableStateOf<LabCategory?>(null) }

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
                Text(
                    text = "TRAINING & FILM LAB",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Sub-tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Player DNA", "Evolution (1–6)", "Drill Labs").forEachIndexed { idx, label ->
                        FilterChip(
                            selected = selectedSection == idx,
                            onClick = { selectedSection = idx },
                            label = { Text(label) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("train_section_$idx")
                        )
                    }
                }

                when (selectedSection) {
                    0 -> {
                        Text(
                            text = "Study specific combo-guard habits, test your basketball IQ read, then immediately train her signature moves.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Player DNA horizontal selector
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(BasketballCurriculum.playerDnaModels, key = { it.id }) { model ->
                                val active = model.id == selectedPlayerId
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (active) ObsidianSurfaceHigh else ObsidianSurface
                                    ),
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier
                                        .width(164.dp)
                                        .border(
                                            width = if (active) 1.5.dp else 1.dp,
                                            color = if (active) CourtOrange else ObsidianBorder,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .clickable { selectedPlayerId = model.id }
                                        .testTag("dna_player_${model.id}")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = model.archetype.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (active) ElectricVolt else CourtOrange
                                        )
                                        Text(
                                            text = model.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = model.positionTag,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        val activeModel = BasketballCurriculum.playerDnaModels
                            .firstOrNull { it.id == selectedPlayerId }
                            ?: BasketballCurriculum.playerDnaModels.first()

                        PlayerDnaDetailCard(
                            model = activeModel,
                            selectedOption = scenarioAnswers[activeModel.id],
                            onSelectOption = { idx -> scenarioAnswers[activeModel.id] = idx },
                            onPracticeClick = {
                                val targetWorkout = when (activeModel.id) {
                                    "clark", "gray" -> BasketballCurriculum.allWorkouts.first { it.dayKey == "FRI" }
                                    "bueckers" -> BasketballCurriculum.allWorkouts.first { it.dayKey == "THU" }
                                    "watkins" -> BasketballCurriculum.allWorkouts.first { it.dayKey == "MON" }
                                    else -> BasketballCurriculum.allWorkouts.first { it.dayKey == "TUE" }
                                }
                                onStartWorkout(targetWorkout)
                            }
                        )
                    }

                    1 -> {
                        Text(
                            text = "Levels unlock through demonstrated consistency, XP, and Coach/Parent mastery sign-off — not just tapping complete.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val workoutCount = bundle.workouts.size
                        BasketballCurriculum.evolutionLevels.forEach { level ->
                            val isCurrent = level.levelNumber == profile.currentLevel
                            val isUnlocked = level.levelNumber <= profile.currentLevel ||
                                (profile.xp >= level.minXp && workoutCount >= level.minWorkouts)
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent) ObsidianSurfaceHigh else ObsidianSurface
                                ),
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isCurrent) 1.5.dp else 1.dp,
                                        color = when {
                                            isCurrent -> ElectricVolt
                                            isUnlocked -> ArenaCyan.copy(alpha = 0.45f)
                                            else -> ObsidianBorder
                                        },
                                        shape = MaterialTheme.shapes.large
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = if (isCurrent) ElectricVolt else if (isUnlocked) ArenaCyan else ObsidianSurfaceHigh,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "LVL 0${level.levelNumber}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isCurrent || isUnlocked) ObsidianBg else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = level.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (isCurrent) "ACTIVE LEVEL" else level.subtitle,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isCurrent) ElectricVolt else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                            contentDescription = if (isUnlocked) "Unlocked" else "Locked",
                                            tint = if (isUnlocked) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = level.skillsSummary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Unlock path: ${level.unlockCriteria}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MambaGold
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        Text(
                            text = "Explore the 6 core skill labs or launch any guided 15–30 minute workout directly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Guided Workouts List
                        Text(
                            text = "GUIDED WORKOUT LIBRARY",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        BasketballCurriculum.allWorkouts.forEach { workout ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, ObsidianBorder, MaterialTheme.shapes.medium)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${workout.levelTag} · ${workout.durationMinutes} min",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CourtOrange
                                        )
                                        Text(
                                            text = workout.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${workout.drills.size} drills · +${workout.xpReward} XP",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Button(
                                        onClick = { onStartWorkout(workout) },
                                        modifier = Modifier.testTag("launch_workout_${workout.id}"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricVolt,
                                            contentColor = ObsidianBg
                                        )
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Start")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "6 VISUAL SKILL LABS",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        LabCategory.entries.forEach { lab ->
                            val expanded = selectedLab == lab
                            val labDrills = BasketballCurriculum.allWorkouts
                                .flatMap { it.drills }
                                .filter { it.labCategory == lab }
                                .distinctBy { it.name }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, ObsidianBorder, MaterialTheme.shapes.medium)
                                    .clickable { selectedLab = if (expanded) null else lab }
                                    .testTag("lab_card_${lab.name}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = lab.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = ArenaCyan
                                            )
                                            Text(
                                                text = lab.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = "Expand lab",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    AnimatedVisibility(visible = expanded) {
                                        Column(
                                            modifier = Modifier.padding(top = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            labDrills.forEach { d ->
                                                Surface(
                                                    color = ObsidianSurfaceHigh,
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp)) {
                                                        Text(
                                                            text = d.name,
                                                            style = MaterialTheme.typography.titleSmall,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = d.repsTarget,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = ElectricVolt
                                                        )
                                                        Text(
                                                            text = d.coachingCue,
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
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerDnaDetailCard(
    model: PlayerDnaModel,
    selectedOption: Int?,
    onSelectOption: (Int) -> Unit,
    onPracticeClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianBorder, MaterialTheme.shapes.large)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            ) {
                Image(
                    painter = painterResource(
                        id = if (model.usesPlaymakerArt) R.drawable.img_dna_playmaker else R.drawable.img_dna_scorer
                    ),
                    contentDescription = "${model.name} archetype illustration",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, ObsidianSurface)
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = model.archetype.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricVolt
                    )
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mission Banner
                Surface(
                    color = CourtOrange.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CourtOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ON-COURT MISSION",
                            style = MaterialTheme.typography.labelSmall,
                            color = CourtOrange
                        )
                        Text(
                            text = model.mission,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    model.traits.forEach { trait ->
                        Surface(
                            color = ObsidianSurfaceHigh,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = trait,
                                style = MaterialTheme.typography.bodySmall,
                                color = ArenaCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "FILM STUDY KEYS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MambaGold
                )
                model.filmStudyKeyPoints.forEach { point ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.SportsBasketball,
                            contentDescription = null,
                            tint = CourtOrange,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = point,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Interactive Basketball IQ Scenario
                Surface(
                    color = ObsidianSurfaceHigh,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ArenaCyan.copy(alpha = 0.35f), MaterialTheme.shapes.medium)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = ArenaCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FILM READ CHECK",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArenaCyan
                            )
                        }
                        Text(
                            text = model.iqScenario.question,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        model.iqScenario.options.forEachIndexed { idx, opt ->
                            val isChosen = selectedOption == idx
                            val isCorrect = idx == model.iqScenario.correctIndex
                            val bg = when {
                                selectedOption == null -> ObsidianSurface
                                isCorrect -> EmeraldSuccess.copy(alpha = 0.18f)
                                isChosen -> MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
                                else -> ObsidianSurface
                            }
                            Surface(
                                color = bg,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (isChosen) ArenaCyan else ObsidianBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectOption(idx) }
                                    .testTag("iq_option_${model.id}_$idx")
                            ) {
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        if (selectedOption != null) {
                            Text(
                                text = model.iqScenario.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = ElectricVolt
                            )
                        }
                    }
                }

                Button(
                    onClick = onPracticeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("practice_dna_${model.id}_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricVolt,
                        contentColor = ObsidianBg
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Practice ${model.name.substringBefore(" ")} Inspired Workout")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
