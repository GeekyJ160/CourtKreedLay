package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BasketballCurriculum
import com.example.data.PlayerDnaModel
import com.example.model.PlayerProfile
import com.example.model.ShotZoneStat
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class DnaRadarAxis(
    val id: String,
    val label: String,
    val shortLabel: String,
    val makes: Int,
    val attempts: Int,
    val percentage: Int,
    val zoneIds: List<String>,
    val drillTip: String,
)

data class PlayerDnaAnalytics(
    val axes: List<DnaRadarAxis>,
    val totalMakes: Int,
    val totalAttempts: Int,
    val overallPct: Int,
    val strengths: List<DnaRadarAxis>,
    val weaknesses: List<DnaRadarAxis>,
    val matchedPro: PlayerDnaModel,
    val matchReason: String,
)

fun aggregatePlayerDnaFromShotLab(shotZones: List<ShotZoneStat>): PlayerDnaAnalytics {
    val byId = shotZones.associateBy { it.id }

    fun buildAxis(
        id: String,
        label: String,
        shortLabel: String,
        zoneIds: List<String>,
        drillTip: String,
    ): DnaRadarAxis {
        val makes = zoneIds.sumOf { byId[it]?.makes ?: 0 }
        val attempts = zoneIds.sumOf { byId[it]?.attempts ?: 0 }
        val pct = if (attempts > 0) ((makes.toDouble() / attempts.toDouble()) * 100.0).roundToInt().coerceIn(0, 100) else 0
        return DnaRadarAxis(
            id = id,
            label = label,
            shortLabel = shortLabel,
            makes = makes,
            attempts = attempts,
            percentage = pct,
            zoneIds = zoneIds,
            drillTip = drillTip,
        )
    }

    val rimAxis = buildAxis(
        id = "rim",
        label = "Rim & Paint",
        shortLabel = "RIM",
        zoneIds = listOf("paint", "left_short", "right_short"),
        drillTip = "Both-Hand Mikan Finish & Contact Layups"
    )
    val midAxis = buildAxis(
        id = "midrange",
        label = "Midrange",
        shortLabel = "MID",
        zoneIds = listOf("left_elbow", "right_elbow", "ft_top"),
        drillTip = "Paige 1-Dribble Elbow Pull-Up"
    )
    val wingTop3Axis = buildAxis(
        id = "perimeter_3",
        label = "3PT Wing/Top",
        shortLabel = "3PT ARC",
        zoneIds = listOf("left_wing_3", "right_wing_3", "top_key_3"),
        drillTip = "Caitlin Step-Into-Range Catch & Shoot"
    )
    val corner3Axis = buildAxis(
        id = "corner_3",
        label = "Corner 3s",
        shortLabel = "CRN 3",
        zoneIds = listOf("left_corner_3", "right_corner_3"),
        drillTip = "Curry Corner Relocation Catch & Shoot"
    )
    val leftSideAxis = buildAxis(
        id = "left_side",
        label = "Left Floor",
        shortLabel = "LEFT",
        zoneIds = listOf("left_short", "left_elbow", "left_corner_3", "left_wing_3"),
        drillTip = "Left-Side Pull-Ups & Weak-Hand Drives"
    )
    val rightSideAxis = buildAxis(
        id = "right_side",
        label = "Right Floor",
        shortLabel = "RIGHT",
        zoneIds = listOf("right_short", "right_elbow", "right_corner_3", "right_wing_3"),
        drillTip = "Right-Side Footwork & Quick Release"
    )

    val axes = listOf(rimAxis, midAxis, wingTop3Axis, corner3Axis, leftSideAxis, rightSideAxis)
    val totalMakes = shotZones.sumOf { it.makes }
    val totalAttempts = shotZones.sumOf { it.attempts }
    val overallPct = if (totalAttempts > 0) ((totalMakes.toDouble() / totalAttempts.toDouble()) * 100.0).roundToInt() else 0

    val attemptedAxes = axes.filter { it.attempts > 0 }
    val strengths = if (attemptedAxes.isNotEmpty()) {
        attemptedAxes.sortedWith(compareByDescending<DnaRadarAxis> { it.percentage }.thenByDescending { it.makes }).take(2)
    } else {
        emptyList()
    }
    val weaknesses = if (attemptedAxes.size >= 2) {
        attemptedAxes.sortedWith(compareBy<DnaRadarAxis> { it.percentage }.thenBy { it.attempts }).take(2)
    } else {
        emptyList()
    }

    val models = BasketballCurriculum.playerDnaModels
    val (matchedPro, matchReason) = when {
        totalAttempts == 0 -> {
            models.first { it.id == "bueckers" } to "Log shots in Shot Lab to unlock your live DNA archetype match."
        }
        wingTop3Axis.percentage >= midAxis.percentage && wingTop3Axis.percentage >= rimAxis.percentage && wingTop3Axis.attempts >= 3 -> {
            models.first { it.id == "clark" } to "Matched by your deep-range confidence (${wingTop3Axis.percentage}% from 3PT Wing/Top)."
        }
        corner3Axis.percentage >= 45 && corner3Axis.attempts >= 3 -> {
            models.first { it.id == "curry" } to "Matched by your perimeter relocation accuracy (${corner3Axis.percentage}% Corner 3s)."
        }
        rimAxis.percentage >= midAxis.percentage && rimAxis.attempts >= 3 -> {
            models.first { it.id == "watkins" } to "Matched by your downhill rim pressure (${rimAxis.percentage}% Paint & Short Corner)."
        }
        else -> {
            models.first { it.id == "bueckers" } to "Matched by your balanced midrange touch (${midAxis.percentage}% Elbow & Free Throw)."
        }
    }

    return PlayerDnaAnalytics(
        axes = axes,
        totalMakes = totalMakes,
        totalAttempts = totalAttempts,
        overallPct = overallPct,
        strengths = strengths,
        weaknesses = weaknesses,
        matchedPro = matchedPro,
        matchReason = matchReason,
    )
}

@Composable
fun PlayerDnaProfileScreen(
    bundle: DashboardBundle,
    onBack: (() -> Unit)? = null,
    onOpenShotLab: () -> Unit,
    onStartRecommendedDrill: () -> Unit,
) {
    val profile = bundle.profile ?: PlayerProfile()
    val analytics = remember(bundle.shotZones) {
        aggregatePlayerDnaFromShotLab(bundle.shotZones)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("player_dna_profile_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 640.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with optional Back button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (onBack != null) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("player_dna_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                        }
                        Column {
                            Text(
                                text = "PLAYER DNA PROFILE",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArenaCyan
                            )
                            Text(
                                text = profile.playerName,
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Surface(
                        color = ObsidianSurfaceHigh,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.border(1.dp, ElectricVolt.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "SHOT LAB FG",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricVolt
                            )
                            Text(
                                text = if (analytics.totalAttempts > 0) "${analytics.overallPct}%" else "--",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Radar Chart Card
                PlayerDnaRadarSection(
                    analytics = analytics,
                    onOpenShotLab = onOpenShotLab,
                )

                // Matched Guard Archetype Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CourtOrange.copy(alpha = 0.5f), MaterialTheme.shapes.large)
                        .testTag("player_dna_archetype_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE DNA ARCHETYPE MATCH",
                                style = MaterialTheme.typography.labelSmall,
                                color = CourtOrange
                            )
                            Text(
                                text = "${analytics.totalMakes}/${analytics.totalAttempts} SHOTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArenaCyan
                            )
                        }
                        Text(
                            text = "${analytics.matchedPro.name} · ${analytics.matchedPro.archetype}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = analytics.matchReason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onStartRecommendedDrill,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("dna_train_archetype_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricVolt,
                                    contentColor = ObsidianBg
                                )
                            ) {
                                Text("Train Next Workout")
                            }
                            OutlinedButton(
                                onClick = onOpenShotLab,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("dna_log_more_shots_button")
                            ) {
                                Icon(Icons.Default.SportsBasketball, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Shot Lab")
                            }
                        }
                    }
                }

                // Strengths & Weaknesses Breakdown
                StrengthsAndWeaknessesSection(analytics = analytics)
            }
        }
    }
}

@Composable
fun PlayerDnaRadarSection(
    analytics: PlayerDnaAnalytics,
    onOpenShotLab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedAxisIdx by remember { mutableIntStateOf(0) }
    val selectedAxis = analytics.axes.getOrElse(selectedAxisIdx) { analytics.axes.first() }

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianBorder, MaterialTheme.shapes.large)
            .testTag("player_dna_radar_card")
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = null,
                        tint = ElectricVolt,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "SHOOTING DNA RADAR",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = if (analytics.totalAttempts > 0) {
                        "${analytics.totalMakes}/${analytics.totalAttempts} FG"
                    } else {
                        "NO SHOTS YET"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaCyan
                )
            }

            Text(
                text = "Aggregated live from your local Shot Lab logs across all 11 court positions.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Radar Chart Canvas
            DnaRadarChartCanvas(
                axes = analytics.axes,
                selectedAxisIndex = selectedAxisIdx,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.08f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(ObsidianBg)
                    .padding(8.dp)
                    .testTag("player_dna_radar_canvas")
            )

            // Axis Selector Pills
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                analytics.axes.chunked(3).forEachIndexed { rowIdx, rowAxes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowAxes.forEachIndexed { colIdx, axis ->
                            val actualIdx = rowIdx * 3 + colIdx
                            val isSelected = actualIdx == selectedAxisIdx
                            Surface(
                                color = if (isSelected) ObsidianSurfaceHigh else ObsidianBg,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) ElectricVolt else ObsidianBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedAxisIdx = actualIdx }
                                    .testTag("radar_axis_pill_${axis.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = axis.shortLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) ElectricVolt else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (axis.attempts > 0) "${axis.percentage}%" else "--",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = when {
                                            axis.attempts == 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                                            axis.percentage >= 55 -> EmeraldSuccess
                                            axis.percentage >= 38 -> MambaGold
                                            else -> CourtOrange
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Selected Axis Detail Strip
            Surface(
                color = ObsidianSurfaceHigh,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedAxis.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = ArenaCyan
                        )
                        Text(
                            text = if (selectedAxis.attempts > 0) {
                                "${selectedAxis.makes} / ${selectedAxis.attempts} made (${selectedAxis.percentage}%)"
                            } else {
                                "0 shots recorded in this area yet"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Drill focus: ${selectedAxis.drillTip}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (analytics.totalAttempts == 0) {
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = onOpenShotLab) {
                            Text("Log")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DnaRadarChartCanvas(
    axes: List<DnaRadarAxis>,
    selectedAxisIndex: Int,
    modifier: Modifier = Modifier,
) {
    val textMeasurer: TextMeasurer = rememberTextMeasurer()
    val animatedFractions = axes.mapIndexed { idx, axis ->
        val target = if (axis.attempts > 0) {
            (axis.percentage / 100f).coerceIn(0.08f, 1f)
        } else {
            0.05f
        }
        animateFloatAsState(
            targetValue = target,
            animationSpec = tween(durationMillis = 500),
            label = "radar_axis_$idx"
        ).value
    }

    Canvas(modifier = modifier) {
        if (axes.isEmpty()) return@Canvas

        val count = axes.size
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (minOf(size.width, size.height) * 0.34f).coerceAtLeast(10f)
        val angleStep = (2.0 * PI / count).toFloat()
        val startAngle = (-PI / 2.0).toFloat() // Top vertex straight up

        fun pointAt(index: Int, fraction: Float): Offset {
            val angle = startAngle + index * angleStep
            return Offset(
                x = center.x + cos(angle) * radius * fraction,
                y = center.y + sin(angle) * radius * fraction
            )
        }

        // 1. Concentric hexagonal grid rings (25%, 50%, 75%, 100%)
        val ringLevels = listOf(0.25f, 0.50f, 0.75f, 1.0f)
        ringLevels.forEachIndexed { ringIdx, level ->
            val ringPath = Path()
            for (i in 0 until count) {
                val p = pointAt(i, level)
                if (i == 0) ringPath.moveTo(p.x, p.y) else ringPath.lineTo(p.x, p.y)
            }
            ringPath.close()
            drawPath(
                path = ringPath,
                color = if (ringIdx == ringLevels.lastIndex) Color(0xFF475569) else Color(0xFF273244),
                style = Stroke(width = if (ringIdx == ringLevels.lastIndex) 2.5f else 1.5f)
            )
        }

        // 2. Radial axis spokes
        for (i in 0 until count) {
            val outer = pointAt(i, 1f)
            drawLine(
                color = if (i == selectedAxisIndex) ElectricVolt.copy(alpha = 0.65f) else Color(0xFF334155),
                start = center,
                end = outer,
                strokeWidth = if (i == selectedAxisIndex) 3f else 1.5f
            )
        }

        // 3. Ring percentage labels along top spoke
        listOf(0.25f to "25%", 0.50f to "50%", 0.75f to "75%").forEach { (lvl, text) ->
            val p = pointAt(0, lvl)
            val measured = textMeasurer.measure(
                text = text,
                style = TextStyle(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Medium)
            )
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(p.x + 6f, p.y - measured.size.height / 2f)
            )
        }

        // 4. Player DNA filled polygon
        val dataPath = Path()
        val dataPoints = mutableListOf<Offset>()
        for (i in 0 until count) {
            val pt = pointAt(i, animatedFractions[i])
            dataPoints.add(pt)
            if (i == 0) dataPath.moveTo(pt.x, pt.y) else dataPath.lineTo(pt.x, pt.y)
        }
        dataPath.close()

        drawPath(
            path = dataPath,
            color = ElectricVolt.copy(alpha = 0.24f)
        )
        drawPath(
            path = dataPath,
            color = ElectricVolt,
            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 5. Vertex dots & outer axis labels
        for (i in 0 until count) {
            val axis = axes[i]
            val pt = dataPoints[i]
            val dotColor = when {
                axis.attempts == 0 -> Color(0xFF64748B)
                axis.percentage >= 55 -> EmeraldSuccess
                axis.percentage >= 38 -> MambaGold
                else -> CourtOrange
            }

            if (i == selectedAxisIndex) {
                drawCircle(
                    color = ElectricVolt,
                    radius = 12f,
                    center = pt,
                    style = Stroke(width = 2.5f)
                )
            }
            drawCircle(
                color = dotColor,
                radius = 7f,
                center = pt
            )

            // Outer vertex label
            val angle = startAngle + i * angleStep
            val labelRadius = radius * 1.24f
            val lx = center.x + cos(angle) * labelRadius
            val ly = center.y + sin(angle) * labelRadius

            val labelText = if (axis.attempts > 0) "${axis.shortLabel}\n${axis.percentage}%" else axis.shortLabel
            val measured = textMeasurer.measure(
                text = labelText,
                style = TextStyle(
                    color = if (i == selectedAxisIndex) ElectricVolt else Color(0xFFE2E8F0),
                    fontSize = 10.sp,
                    fontWeight = if (i == selectedAxisIndex) FontWeight.Bold else FontWeight.SemiBold,
                    lineHeight = 12.sp
                )
            )
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = (lx - measured.size.width / 2f).coerceIn(2f, size.width - measured.size.width - 2f),
                    y = (ly - measured.size.height / 2f).coerceIn(2f, size.height - measured.size.height - 2f)
                )
            )
        }
    }
}

@Composable
private fun StrengthsAndWeaknessesSection(
    analytics: PlayerDnaAnalytics,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Strengths Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EmeraldSuccess.copy(alpha = 0.45f), MaterialTheme.shapes.large)
                .testTag("player_dna_strengths_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "TOP STRENGTHS",
                        style = MaterialTheme.typography.headlineMedium,
                        color = EmeraldSuccess
                    )
                }

                if (analytics.strengths.isEmpty()) {
                    Text(
                        text = "Record shots across Shot Lab court positions to surface her highest-percentage spots.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    analytics.strengths.forEach { axis ->
                        AxisStrengthRow(axis = axis, accent = EmeraldSuccess)
                    }
                }
            }
        }

        // Weaknesses / Growth Areas Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CourtOrange.copy(alpha = 0.45f), MaterialTheme.shapes.large)
                .testTag("player_dna_weaknesses_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = CourtOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "GROWTH OPPORTUNITIES",
                        style = MaterialTheme.typography.headlineMedium,
                        color = CourtOrange
                    )
                }

                if (analytics.weaknesses.isEmpty()) {
                    Text(
                        text = "Log shots from at least two court areas in Shot Lab to pinpoint where to focus reps next.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    analytics.weaknesses.forEach { axis ->
                        AxisStrengthRow(axis = axis, accent = CourtOrange, showDrillTip = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun AxisStrengthRow(
    axis: DnaRadarAxis,
    accent: Color,
    showDrillTip: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = axis.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${axis.makes}/${axis.attempts} · ${axis.percentage}%",
                style = MaterialTheme.typography.labelMedium,
                color = accent
            )
        }
        LinearProgressIndicator(
            progress = { (axis.percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = accent,
            trackColor = ObsidianSurfaceHigh
        )
        if (showDrillTip) {
            Text(
                text = "Recommended fix: ${axis.drillTip}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
