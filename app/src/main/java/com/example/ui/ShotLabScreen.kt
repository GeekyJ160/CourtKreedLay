package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BasketballCurriculum
import com.example.data.CourtZoneDef
import com.example.data.ProgressionSystem
import com.example.model.ShotZoneStat
import com.example.model.careerAccuracyPercent
import com.example.model.sessionAccuracyPercent
import com.example.ui.theme.ArenaCyan
import com.example.ui.theme.CourtOrange
import com.example.ui.theme.CrimsonMiss
import com.example.ui.theme.ElectricVolt
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MambaGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.viewmodel.DashboardBundle
import kotlin.math.hypot

@Composable
fun ShotLabScreen(
    bundle: DashboardBundle,
    selectedZoneId: String,
    onSelectZone: (String) -> Unit,
    onRecordShot: (CourtZoneDef, Boolean) -> Unit,
    onRecordBatch: (CourtZoneDef, Int, Int) -> Unit,
    onResetSession: () -> Unit,
    onOpenPlayerDna: () -> Unit = {},
) {
    var showSessionOnly by remember { mutableStateOf(true) }
    var showHeatmap by remember { mutableStateOf(true) }
    val zones = BasketballCurriculum.courtZones
    val statsByZone = remember(bundle.shotZones) { bundle.shotZones.associateBy { it.id } }
    val selectedZone = zones.firstOrNull { it.id == selectedZoneId } ?: zones.first()
    val selectedStat = statsByZone[selectedZone.id]
    val loggedZonesCount = remember(statsByZone, showSessionOnly) {
        zones.count { z ->
            val stat = statsByZone[z.id]
            val attempts = if (showSessionOnly) (stat?.sessionAttempts ?: 0) else (stat?.attempts ?: 0)
            attempts > 0
        }
    }

    val activeMakes = if (showSessionOnly) (selectedStat?.sessionMakes ?: 0) else (selectedStat?.makes ?: 0)
    val activeAttempts = if (showSessionOnly) (selectedStat?.sessionAttempts ?: 0) else (selectedStat?.attempts ?: 0)
    val activePct = if (showSessionOnly) (selectedStat?.sessionAccuracyPercent() ?: 0) else (selectedStat?.careerAccuracyPercent() ?: 0)

    val weakestZone = remember(bundle.shotZones) {
        val attempted = bundle.shotZones.filter { it.attempts >= 5 }
        val weakestStat = attempted.minByOrNull { it.careerAccuracyPercent() }
        if (weakestStat != null) {
            zones.firstOrNull { it.id == weakestStat.id } to weakestStat.careerAccuracyPercent()
        } else {
            zones.first() to null
        }
    }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "SHOT LAB",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArenaCyan
                            )
                            Surface(
                                color = ObsidianSurfaceHigh,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = "Saved locally",
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "SAVED LOCALLY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        }
                        Text(
                            text = "FIND YOUR SWEET SPOT",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = showSessionOnly,
                            onClick = { showSessionOnly = true },
                            label = { Text("Session") }
                        )
                        FilterChip(
                            selected = !showSessionOnly,
                            onClick = { showSessionOnly = false },
                            label = { Text("All-Time") }
                        )
                    }
                }

                // Quick Court Position Selector Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("shot_position_chips")
                ) {
                    items(zones, key = { it.id }) { zone ->
                        val isSelected = zone.id == selectedZone.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectZone(zone.id) },
                            label = { Text(zone.name) },
                            modifier = Modifier.testTag("shot_chip_${zone.id}")
                        )
                    }
                }

                // Interactive Half-Court Map + Visual Accuracy Heatmap
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianBorder, MaterialTheme.shapes.large)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (showHeatmap) "COURT ACCURACY HEATMAP" else "HALF-COURT SHOT MAP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (showHeatmap) ElectricVolt else ArenaCyan
                                )
                                Text(
                                    text = "Tap any zone on the court to track makes & misses",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            FilterChip(
                                selected = showHeatmap,
                                onClick = { showHeatmap = !showHeatmap },
                                label = { Text("Heatmap") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Toggle court heatmap overlay",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CourtOrange.copy(alpha = 0.22f),
                                    selectedLabelColor = CourtOrange,
                                    selectedLeadingIconColor = CourtOrange
                                ),
                                modifier = Modifier.testTag("shot_heatmap_toggle")
                            )
                        }
                        InteractiveHalfCourtCanvas(
                            zones = zones,
                            statsByZone = statsByZone,
                            selectedZoneId = selectedZone.id,
                            showSessionOnly = showSessionOnly,
                            showHeatmap = showHeatmap,
                            onZoneSelected = onSelectZone,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.22f)
                                .clip(MaterialTheme.shapes.medium)
                                .background(ObsidianBg)
                                .testTag("interactive_shot_court")
                        )
                        ShotHeatmapLegend(
                            loggedZonesCount = loggedZonesCount,
                            totalZonesCount = zones.size,
                            showHeatmap = showHeatmap,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("shot_heatmap_legend")
                        )
                    }
                }

                // Selected Zone Logging Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CourtOrange.copy(alpha = 0.55f), MaterialTheme.shapes.large)
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
                            Column {
                                Text(
                                    text = if (selectedZone.isCloseRange) "FOUNDATION ZONE" else "RANGE ZONE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedZone.isCloseRange) ElectricVolt else ArenaCyan
                                )
                                Text(
                                    text = selectedZone.name,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val shotLabXp = ProgressionSystem.computeShotLabXp(bundle.shotZones)
                                Text(
                                    text = "+7 XP/make · +2 XP/miss · Shot Lab XP: +$shotLabXp",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElectricVolt
                                )
                            }
                            Surface(
                                color = ObsidianSurfaceHigh,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "$activePct%",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = when {
                                        activeAttempts == 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                                        activePct >= 55 -> ElectricVolt
                                        activePct >= 38 -> MambaGold
                                        else -> CourtOrange
                                    },
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ShotCounterPill("MAKES", activeMakes.toString(), EmeraldSuccess, Modifier.weight(1f))
                            ShotCounterPill("ATTEMPTS", activeAttempts.toString(), ArenaCyan, Modifier.weight(1f))
                            ShotCounterPill("ACCURACY", "$activePct%", MambaGold, Modifier.weight(1f))
                        }

                        // Primary Made It / Missed Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onRecordShot(selectedZone, true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("shot_made_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldSuccess,
                                    contentColor = ObsidianBg
                                )
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Made It", style = MaterialTheme.typography.titleMedium)
                            }
                            Button(
                                onClick = { onRecordShot(selectedZone, false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("shot_missed_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CrimsonMiss,
                                    contentColor = MaterialTheme.colorScheme.onBackground
                                )
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Missed", style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        // Quick 10-shot drill logging
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onRecordBatch(selectedZone, 8, 10) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+8/10 Set")
                            }
                            OutlinedButton(
                                onClick = { onRecordBatch(selectedZone, 5, 10) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+5/10 Set")
                            }
                            OutlinedButton(
                                onClick = onResetSession,
                                modifier = Modifier.testTag("reset_shot_session_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset session", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Reset")
                            }
                        }
                    }
                }

                // Recommended Drill Card based on Weakest Zone / Foundation Guidance
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceHigh),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MambaGold.copy(alpha = 0.4f), MaterialTheme.shapes.large)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = MambaGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val targetZone = weakestZone.first ?: selectedZone
                            val weakPct = weakestZone.second
                            Text(
                                text = if (weakPct != null) "WEAK-SPOT FOCUS: ${targetZone.name.uppercase()} ($weakPct%)" else "SHOOTING MECHANICS TIP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MambaGold
                            )
                            Text(
                                text = "Recommended drill: ${targetZone.recommendedDrill}. Keep most early reps close to the basket and grow your range as balance and form stay automatic.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // All Court Positions Breakdown (Local Shot Log)
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianBorder, MaterialTheme.shapes.large)
                        .testTag("shot_positions_breakdown_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (showSessionOnly) "COURT POSITIONS · THIS SESSION" else "COURT POSITIONS · ALL-TIME",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        zones.forEach { zone ->
                            val stat = statsByZone[zone.id]
                            val makes = if (showSessionOnly) (stat?.sessionMakes ?: 0) else (stat?.makes ?: 0)
                            val attempts = if (showSessionOnly) (stat?.sessionAttempts ?: 0) else (stat?.attempts ?: 0)
                            val pct = if (attempts == 0) 0 else (makes * 100) / attempts
                            val isSelected = zone.id == selectedZone.id

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ObsidianSurfaceHigh else Color.Transparent)
                                    .clickable { onSelectZone(zone.id) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = zone.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isSelected) ElectricVolt else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (attempts == 0) "0 / 0" else "$makes / $attempts ($pct%)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (attempts == 0) MaterialTheme.colorScheme.onSurfaceVariant else ArenaCyan
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { if (attempts == 0) 0f else (pct / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = when {
                                        attempts == 0 -> ObsidianBorder
                                        pct >= 55 -> EmeraldSuccess
                                        pct >= 38 -> MambaGold
                                        else -> CourtOrange
                                    },
                                    trackColor = ObsidianBg
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = onOpenPlayerDna,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("shot_lab_open_player_dna_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricVolt,
                                contentColor = ObsidianBg
                            )
                        ) {
                            Text("View Player DNA Radar Chart")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShotCounterPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = ObsidianSurfaceHigh,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = accent)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShotHeatmapLegend(
    loggedZonesCount: Int,
    totalZonesCount: Int,
    showHeatmap: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LegendSwatch(label = "Hot 55%+", color = EmeraldSuccess)
                LegendSwatch(label = "Warm 38–54%", color = MambaGold)
                LegendSwatch(label = "Cold <38%", color = CourtOrange)
                LegendSwatch(label = "None", color = Color(0xFF1E293B), borderColor = Color(0xFF475569))
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                color = ObsidianSurfaceHigh,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "$loggedZonesCount/$totalZonesCount active",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (loggedZonesCount > 0) ArenaCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        if (loggedZonesCount == 0 && showHeatmap) {
            Text(
                text = "Log shots below to illuminate your shooting zones on the court.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegendSwatch(
    label: String,
    color: Color,
    borderColor: Color? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (borderColor != null) Modifier.border(1.dp, borderColor, CircleShape)
                    else Modifier
                )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InteractiveHalfCourtCanvas(
    zones: List<CourtZoneDef>,
    statsByZone: Map<String, ShotZoneStat>,
    selectedZoneId: String,
    showSessionOnly: Boolean,
    showHeatmap: Boolean,
    onZoneSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer: TextMeasurer = rememberTextMeasurer()
    val heatmapAlpha by animateFloatAsState(
        targetValue = if (showHeatmap) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "court_heatmap_alpha"
    )

    Canvas(
        modifier = modifier.pointerInput(zones) {
            detectTapGestures { tap ->
                val w = size.width.toFloat().coerceAtLeast(1f)
                val h = size.height.toFloat().coerceAtLeast(1f)
                val nearest = zones.minByOrNull { z ->
                    val zx = z.normX * w
                    val zy = z.normY * h
                    hypot((tap.x - zx).toDouble(), (tap.y - zy).toDouble())
                }
                if (nearest != null) {
                    onZoneSelected(nearest.id)
                }
            }
        }
    ) {
        val w = size.width
        val h = size.height
        val minDim = minOf(w, h)
        val courtLeft = w * 0.04f
        val courtTop = h * 0.04f
        val courtWidth = w * 0.92f
        val courtHeight = h * 0.92f
        val courtLine = Color(0xFF475569)

        // Subtle court floor & paint tint
        drawRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(courtLeft, courtTop),
            size = Size(courtWidth, courtHeight)
        )
        drawRect(
            color = Color(0xFF1E293B).copy(alpha = 0.28f),
            topLeft = Offset(w * 0.35f, courtTop),
            size = Size(w * 0.30f, h * 0.42f)
        )

        // Visual Court Accuracy Heatmap Overlay (radial thermal blooms clipped to court bounds)
        if (heatmapAlpha > 0.01f) {
            clipRect(
                left = courtLeft,
                top = courtTop,
                right = courtLeft + courtWidth,
                bottom = courtTop + courtHeight
            ) {
                // Pass 1: Wide ambient thermal blooms that blend across neighboring court zones
                zones.forEach { z ->
                    val stat = statsByZone[z.id]
                    val attempts = if (showSessionOnly) (stat?.sessionAttempts ?: 0) else (stat?.attempts ?: 0)
                    if (attempts > 0) {
                        val pct = if (showSessionOnly) (stat?.sessionAccuracyPercent() ?: 0) else (stat?.careerAccuracyPercent() ?: 0)
                        val heatColor = when {
                            pct >= 55 -> EmeraldSuccess
                            pct >= 38 -> MambaGold
                            else -> CourtOrange
                        }
                        val volumeWeight = 0.45f + (attempts.coerceAtMost(20) / 20f) * 0.55f
                        val outerRadius = minDim * (0.21f + (attempts.coerceAtMost(20) / 20f) * 0.07f)
                        val center = Offset(z.normX * w, z.normY * h)

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    heatColor.copy(alpha = 0.44f * volumeWeight * heatmapAlpha),
                                    heatColor.copy(alpha = 0.16f * volumeWeight * heatmapAlpha),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = outerRadius
                            ),
                            radius = outerRadius,
                            center = center
                        )
                    }
                }

                // Pass 2: Focused thermal hotspots directly beneath high-accuracy or cold shooting spots
                zones.forEach { z ->
                    val stat = statsByZone[z.id]
                    val attempts = if (showSessionOnly) (stat?.sessionAttempts ?: 0) else (stat?.attempts ?: 0)
                    if (attempts > 0) {
                        val pct = if (showSessionOnly) (stat?.sessionAccuracyPercent() ?: 0) else (stat?.careerAccuracyPercent() ?: 0)
                        val coreColor = when {
                            pct >= 55 -> ElectricVolt
                            pct >= 38 -> MambaGold
                            else -> CrimsonMiss
                        }
                        val volumeWeight = 0.45f + (attempts.coerceAtMost(20) / 20f) * 0.55f
                        val innerRadius = minDim * 0.115f
                        val center = Offset(z.normX * w, z.normY * h)

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    coreColor.copy(alpha = 0.46f * volumeWeight * heatmapAlpha),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = innerRadius
                            ),
                            radius = innerRadius,
                            center = center
                        )
                    }
                }
            }
        }

        // Outer boundary
        drawRect(
            color = courtLine,
            topLeft = Offset(courtLeft, courtTop),
            size = Size(courtWidth, courtHeight),
            style = Stroke(width = 3f)
        )

        // Paint / Key
        drawRect(
            color = courtLine,
            topLeft = Offset(w * 0.35f, courtTop),
            size = Size(w * 0.30f, h * 0.42f),
            style = Stroke(width = 2.5f)
        )

        // Free-throw circle
        drawCircle(
            color = courtLine,
            radius = w * 0.15f,
            center = Offset(w * 0.50f, h * 0.46f),
            style = Stroke(width = 2.5f)
        )

        // Restricted area arc
        drawArc(
            color = courtLine.copy(alpha = 0.8f),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.42f, h * 0.04f),
            size = Size(w * 0.16f, h * 0.15f),
            style = Stroke(width = 2f)
        )

        // 3-point arc
        drawArc(
            color = courtLine,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.10f, h * -0.20f),
            size = Size(w * 0.80f, h * 0.98f),
            style = Stroke(width = 3f)
        )

        // Backboard & Rim
        drawLine(
            color = Color(0xFF94A3B8),
            start = Offset(w * 0.42f, h * 0.08f),
            end = Offset(w * 0.58f, h * 0.08f),
            strokeWidth = 4f
        )
        drawCircle(
            color = CourtOrange,
            radius = 12f,
            center = Offset(w * 0.50f, h * 0.125f),
            style = Stroke(width = 4f)
        )

        // Draw tappable zone badges
        zones.forEach { z ->
            val stat = statsByZone[z.id]
            val attempts = if (showSessionOnly) (stat?.sessionAttempts ?: 0) else (stat?.attempts ?: 0)
            val pct = if (showSessionOnly) (stat?.sessionAccuracyPercent() ?: 0) else (stat?.careerAccuracyPercent() ?: 0)
            val isSelected = z.id == selectedZoneId

            val fillColor = when {
                attempts == 0 -> Color(0xFF1E293B).copy(alpha = 0.92f)
                pct >= 55 -> EmeraldSuccess.copy(alpha = 0.90f)
                pct >= 38 -> MambaGold.copy(alpha = 0.90f)
                else -> CourtOrange.copy(alpha = 0.90f)
            }
            val center = Offset(z.normX * w, z.normY * h)
            val radius = if (isSelected) 34f else 27f

            if (isSelected) {
                drawCircle(
                    color = ElectricVolt,
                    radius = radius + 6f,
                    center = center,
                    style = Stroke(width = 3.5f)
                )
            }

            drawCircle(
                color = fillColor,
                radius = radius,
                center = center
            )
            drawCircle(
                color = if (isSelected) ElectricVolt else Color(0xFF475569),
                radius = radius,
                center = center,
                style = Stroke(width = 2f)
            )

            val labelText = if (attempts > 0) "$pct%" else z.shortLabel
            val style = TextStyle(
                color = if (attempts > 0 && pct >= 38) ObsidianBg else Color.White,
                fontSize = if (attempts > 0) 11.sp else 8.sp,
                fontWeight = FontWeight.Bold
            )
            val measured = textMeasurer.measure(labelText, style)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    center.x - measured.size.width / 2f,
                    center.y - measured.size.height / 2f
                )
            )
        }
    }
}
