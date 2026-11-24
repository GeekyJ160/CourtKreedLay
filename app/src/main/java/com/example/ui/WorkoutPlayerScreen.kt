package com.example.ui

import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DrillAnimationType
import com.example.ui.theme.ArenaCyan
import com.example.ui.theme.CourtOrange
import com.example.ui.theme.ElectricVolt
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MambaGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.viewmodel.ActiveWorkoutState
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WorkoutPlayerScreen(
    state: ActiveWorkoutState,
    onBack: () -> Unit,
    onToggleRunning: () -> Unit,
    onReset: () -> Unit,
    onToggleVoice: () -> Unit,
    onToggleDrillChecked: (String) -> Unit,
    onSelectDrill: (Int) -> Unit,
    onNextDrill: () -> Unit,
    onFinishWorkout: (Int, String?) -> Unit,
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.US
                ttsReady = true
            }
        }
        engine
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val drills = state.program.drills
    val activeDrill = drills.getOrNull(state.currentDrillIndex) ?: drills.first()

    LaunchedEffect(state.currentDrillIndex, state.isResting, state.voiceEnabled, ttsReady) {
        if (ttsReady && state.voiceEnabled && state.completedWorkoutSummary == null) {
            val spoken = if (state.isResting) {
                "Rest and catch your breath. Next up: ${drills.getOrNull(state.currentDrillIndex + 1)?.name ?: "Cooldown"}."
            } else {
                activeDrill.voiceCue
            }
            tts.speak(spoken, TextToSpeech.QUEUE_FLUSH, null, activeDrill.id)
        }
    }

    var rpeSlider by remember { mutableFloatStateOf(5f) }
    var notesInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier.widthIn(max = 640.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Top bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("workout_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to dashboard",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Column {
                                Text(
                                    text = state.program.levelTag.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CourtOrange
                                )
                                Text(
                                    text = state.program.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        IconButton(
                            onClick = onToggleVoice,
                            modifier = Modifier.testTag("workout_voice_toggle")
                        ) {
                            Icon(
                                imageVector = if (state.voiceEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = if (state.voiceEnabled) "Mute voice cues" else "Enable voice cues",
                                tint = if (state.voiceEnabled) ElectricVolt else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Progress indicator
                    val progress = state.completedDrillIds.size.toFloat() / drills.size.coerceAtLeast(1)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${state.completedDrillIds.size} of ${drills.size} drills completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "+${state.program.xpReward} XP",
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricVolt
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ElectricVolt,
                            trackColor = ObsidianSurfaceHigh
                        )
                    }

                    if (state.completedWorkoutSummary != null) {
                        // Completion celebration card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, ElectricVolt, MaterialTheme.shapes.large)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Workout complete",
                                    tint = ElectricVolt,
                                    modifier = Modifier.size(56.dp)
                                )
                                Text(
                                    text = "WORKOUT COMPLETE!",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = ElectricVolt
                                )
                                Text(
                                    text = state.completedWorkoutSummary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Button(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("workout_done_return_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricVolt,
                                        contentColor = ObsidianBg
                                    )
                                ) {
                                    Text("Return to My Player", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    } else {
                        // Active Drill Card + Animated Court Diagram + Timer
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (state.isResting) ArenaCyan else CourtOrange.copy(alpha = 0.6f),
                                    MaterialTheme.shapes.large
                                )
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (state.isResting) ArenaCyan else CourtOrange,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (state.isResting) "REST PERIOD" else "DRILL ${state.currentDrillIndex + 1} / ${drills.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ObsidianBg,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = activeDrill.primaryAttribute.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MambaGold
                                    )
                                }

                                // Animated Court Drill Diagram
                                AnimatedDrillCourtCanvas(
                                    animationType = activeDrill.animationType,
                                    isRunning = state.isRunning,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(ObsidianBg)
                                        .border(1.dp, ObsidianBorder, MaterialTheme.shapes.medium)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (state.isResting) "Recover & Breathe" else activeDrill.name,
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = activeDrill.repsTarget,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ElectricVolt
                                        )
                                    }
                                    // Countdown Digital Clock
                                    Surface(
                                        color = ObsidianSurfaceHigh,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Text(
                                            text = formatTimer(state.secondsRemaining),
                                            style = MaterialTheme.typography.displayMedium,
                                            color = if (state.isResting) ArenaCyan else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = activeDrill.coachingCue,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Controls
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = onToggleRunning,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("workout_play_pause_button"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (state.isRunning) CourtOrange else ElectricVolt,
                                            contentColor = ObsidianBg
                                        )
                                    ) {
                                        Icon(
                                            imageVector = if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (state.isRunning) "Pause" else "Resume")
                                    }
                                    OutlinedButton(
                                        onClick = onNextDrill,
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("workout_next_drill_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SkipNext,
                                            contentDescription = "Next drill"
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Check & Next")
                                    }
                                    IconButton(
                                        onClick = onReset,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("workout_reset_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Reset workout",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Drill Checklist
            itemsIndexed(drills, key = { _, d -> d.id }) { index, drill ->
                val checked = drill.id in state.completedDrillIds
                val isCurrent = index == state.currentDrillIndex
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) ObsidianSurfaceHigh else ObsidianSurface
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isCurrent) CourtOrange else ObsidianBorder,
                            MaterialTheme.shapes.medium
                        )
                        .clickable { onSelectDrill(index) }
                        .testTag("workout_drill_row_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onToggleDrillChecked(drill.id) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (checked) "Drill checked" else "Mark drill complete",
                                tint = if (checked) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = drill.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${drill.durationSeconds / 60} min · ${drill.repsTarget}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Finish & Log Card
            if (state.completedWorkoutSummary == null) {
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
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "FINISH & LOG WORKOUT",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "How tough did this session feel? (Fatigue 1–10: ${rpeSlider.toInt()})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = rpeSlider,
                                onValueChange = { rpeSlider = it },
                                valueRange = 1f..10f,
                                steps = 8
                            )
                            OutlinedTextField(
                                value = notesInput,
                                onValueChange = { if (it.length <= 280) notesInput = it },
                                label = { Text("Quick reflection (optional)") },
                                placeholder = { Text("e.g., Left-hand pounds felt faster today!") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = { onFinishWorkout(rpeSlider.toInt(), notesInput) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("complete_and_save_workout_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricVolt,
                                    contentColor = ObsidianBg
                                )
                            ) {
                                Text(
                                    text = "Complete & Earn +${state.program.xpReward} XP",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedDrillCourtCanvas(
    animationType: DrillAnimationType,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "drill_motion")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drill_phase"
    )
    val t = if (isRunning) phase else 0.25f

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val lineColor = Color(0xFF2B354C)

        // Minimal half-court floor lines
        drawRect(
            color = lineColor,
            topLeft = Offset(w * 0.06f, h * 0.08f),
            size = Size(w * 0.88f, h * 0.84f),
            style = Stroke(width = 2f)
        )
        // Key / paint box
        drawRect(
            color = lineColor,
            topLeft = Offset(w * 0.36f, h * 0.08f),
            size = Size(w * 0.28f, h * 0.45f),
            style = Stroke(width = 2f)
        )
        // Rim
        drawCircle(
            color = CourtOrange,
            radius = 9f,
            center = Offset(w * 0.50f, h * 0.16f),
            style = Stroke(width = 3.5f)
        )

        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        val angle = t * 2f * PI.toFloat()

        when (animationType) {
            DrillAnimationType.POUND_DRIBBLE -> {
                val playerCenter = Offset(w * 0.5f, h * 0.65f)
                val bounceOffset = kotlin.math.abs(sin(angle * 2f)) * 22f
                val ballCenter = Offset(w * 0.62f, h * 0.65f + bounceOffset)
                drawCircle(color = ArenaCyan, radius = 16f, center = playerCenter)
                drawLine(
                    color = CourtOrange.copy(alpha = 0.7f),
                    start = Offset(w * 0.62f, h * 0.58f),
                    end = Offset(w * 0.62f, h * 0.82f),
                    strokeWidth = 3f,
                    pathEffect = dashEffect
                )
                drawCircle(color = CourtOrange, radius = 11f, center = ballCenter)
            }

            DrillAnimationType.CROSSOVER_WEAVE -> {
                val x = w * (0.28f + 0.44f * (0.5f - 0.5f * cos(angle)))
                val y = h * (0.76f - 0.36f * sin(angle * 0.5f))
                drawLine(
                    color = ElectricVolt.copy(alpha = 0.5f),
                    start = Offset(w * 0.28f, h * 0.76f),
                    end = Offset(w * 0.72f, h * 0.52f),
                    strokeWidth = 3f,
                    pathEffect = dashEffect
                )
                drawCircle(color = ArenaCyan, radius = 15f, center = Offset(x, y))
                drawCircle(color = CourtOrange, radius = 10f, center = Offset(x + 18f, y + 6f))
            }

            DrillAnimationType.FORM_SHOOT -> {
                val shooter = Offset(w * 0.50f, h * 0.66f)
                val rim = Offset(w * 0.50f, h * 0.16f)
                drawLine(
                    color = MambaGold.copy(alpha = 0.5f),
                    start = shooter,
                    end = rim,
                    strokeWidth = 3f,
                    pathEffect = dashEffect
                )
                val ballY = shooter.y + (rim.y - shooter.y) * t
                val arcX = shooter.x + sin(t * PI.toFloat()) * 14f
                drawCircle(color = ArenaCyan, radius = 15f, center = shooter)
                drawCircle(color = CourtOrange, radius = 10f, center = Offset(arcX, ballY))
            }

            DrillAnimationType.LAYUP_DRIVE -> {
                val start = Offset(w * 0.22f, h * 0.76f)
                val end = Offset(w * 0.48f, h * 0.22f)
                drawLine(
                    color = ElectricVolt.copy(alpha = 0.6f),
                    start = start,
                    end = end,
                    strokeWidth = 4f,
                    pathEffect = dashEffect
                )
                val px = start.x + (end.x - start.x) * t
                val py = start.y + (end.y - start.y) * t
                drawCircle(color = ArenaCyan, radius = 15f, center = Offset(px, py))
                drawCircle(color = CourtOrange, radius = 10f, center = Offset(px + 12f, py - 10f))
            }

            DrillAnimationType.DEFENSIVE_SLIDE -> {
                val x = w * (0.28f + 0.44f * (0.5f + 0.5f * sin(angle)))
                val y = h * 0.60f
                drawLine(
                    color = ArenaCyan.copy(alpha = 0.5f),
                    start = Offset(w * 0.26f, y),
                    end = Offset(w * 0.74f, y),
                    strokeWidth = 4f,
                    pathEffect = dashEffect
                )
                drawCircle(color = ElectricVolt, radius = 16f, center = Offset(x, y))
            }

            DrillAnimationType.FLOOR_VISION_PASS -> {
                val pg = Offset(w * 0.50f, h * 0.74f)
                val wing = Offset(w * 0.82f, h * 0.40f)
                val corner = Offset(w * 0.18f, h * 0.24f)
                drawLine(
                    color = ArenaCyan.copy(alpha = 0.65f),
                    start = pg,
                    end = wing,
                    strokeWidth = 3f,
                    pathEffect = dashEffect
                )
                drawLine(
                    color = MambaGold.copy(alpha = 0.45f),
                    start = pg,
                    end = corner,
                    strokeWidth = 2.5f,
                    pathEffect = dashEffect
                )
                drawCircle(color = ArenaCyan, radius = 15f, center = pg)
                drawCircle(color = ElectricVolt, radius = 12f, center = wing)
                drawCircle(color = ElectricVolt, radius = 12f, center = corner)
                val bx = pg.x + (wing.x - pg.x) * t
                val by = pg.y + (wing.y - pg.y) * t
                drawCircle(color = CourtOrange, radius = 9.5f, center = Offset(bx, by), style = Stroke(width = 0f).let { androidx.compose.ui.graphics.drawscope.Fill })
            }
        }
    }
}

private fun formatTimer(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val mins = safe / 60
    val secs = safe % 60
    return "%02d:%02d".format(mins, secs)
}
