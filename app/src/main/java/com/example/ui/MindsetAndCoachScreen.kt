package com.example.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CoachChatMessage
import com.example.model.AuthorRole
import com.example.model.MindsetMission
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
import com.example.viewmodel.AiStreamState
import com.example.viewmodel.DashboardBundle
import com.example.viewmodel.FocusPlanState

@Composable
fun MambaMindsetOnlyContent(
    bundle: DashboardBundle,
    onIncrementMission: (MindsetMission) -> Unit,
    onToggleMission: (MindsetMission) -> Unit,
    onAddCustomMission: (String, String, Int) -> Unit,
    onResetWeeklyMissions: () -> Unit,
    onAddCourageLog: (AuthorRole, String, String) -> Unit,
) {
    var newChallengeText by remember { mutableStateOf("") }
    var courageNote by remember { mutableStateOf("") }

    val profile = bundle.profile ?: PlayerProfile()
    val completedCount = bundle.missions.count { it.completed }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 640.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .clip(MaterialTheme.shapes.large)
                        .border(1.dp, MambaGold.copy(alpha = 0.5f), MaterialTheme.shapes.large)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_mindset_badge),
                        contentDescription = "Mamba Mindset crown emblem",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(ObsidianBg.copy(alpha = 0.35f), ObsidianBg.copy(alpha = 0.92f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "FEARLESS PLAYER CHALLENGE · $completedCount/${bundle.missions.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MambaGold
                        )
                        Text(
                            text = "CONTROLLED AGGRESSION",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "\"${profile.dailyAffirmation}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricVolt
                        )
                    }
                }

                val progress = if (bundle.missions.isEmpty()) 0f else completedCount.toFloat() / bundle.missions.size
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MambaGold,
                    trackColor = ObsidianSurfaceHigh
                )

                bundle.missions.forEach { mission ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (mission.completed) ObsidianSurfaceHigh else ObsidianSurface
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (mission.completed) MambaGold else ObsidianBorder,
                                MaterialTheme.shapes.medium
                            )
                            .testTag("mission_card_${mission.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onToggleMission(mission) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (mission.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Toggle mission",
                                    tint = if (mission.completed) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${mission.category} · +${mission.xpReward} XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MambaGold
                                )
                                Text(
                                    text = mission.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (mission.targetCount > 1) {
                                    Text(
                                        text = "${mission.currentCount} / ${mission.targetCount} reps this week",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArenaCyan
                                    )
                                }
                            }
                            if (mission.targetCount > 1 && !mission.completed) {
                                OutlinedButton(
                                    onClick = { onIncrementMission(mission) },
                                    modifier = Modifier.testTag("mission_plus_one_${mission.id}")
                                ) {
                                    Text("+1")
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newChallengeText,
                        onValueChange = { if (it.length <= 120) newChallengeText = it },
                        label = { Text("Add weekly confidence challenge") },
                        placeholder = { Text("Take 10 shots off the dribble...") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newChallengeText.isNotBlank()) {
                                onAddCustomMission(newChallengeText, "CONFIDENCE", 3)
                                newChallengeText = ""
                            }
                        },
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("add_custom_mission_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MambaGold,
                            contentColor = ObsidianBg
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add challenge")
                    }
                }

                OutlinedButton(
                    onClick = onResetWeeklyMissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Start Fresh Week (Reset Checklist)")
                }

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
                            text = "COURAGE & DECISION LOG",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Log a moment when you made the RIGHT aggressive read — even if the shot missed!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = courageNote,
                            onValueChange = { if (it.length <= 350) courageNote = it },
                            label = { Text("What brave play did you make today?") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (courageNote.isNotBlank()) {
                                    onAddCourageLog(AuthorRole.PLAYER, "Brave Decision", courageNote)
                                    courageNote = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_courage_log_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CourtOrange,
                                contentColor = ObsidianBg
                            )
                        ) {
                            Text("Save Courage Moment")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CoachIqOnlyContent(
    bundle: DashboardBundle,
    chatMessages: List<CoachChatMessage>,
    aiStreamState: AiStreamState,
    focusPlanState: FocusPlanState,
    onAskCoachIq: (String) -> Unit,
    onGenerateFocusPlan: () -> Unit,
) {
    var chatInput by remember { mutableStateOf("") }
    val profile = bundle.profile ?: PlayerProfile()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 640.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ArenaCyan.copy(alpha = 0.45f), MaterialTheme.shapes.large)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ArenaCyan)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "COACH IQ · ${profile.playerName.uppercase()}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Personalized to your Shot Lab, ratings & parent observations",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArenaCyan
                                )
                            }
                        }

                        Button(
                            onClick = onGenerateFocusPlan,
                            enabled = focusPlanState !is FocusPlanState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("generate_focus_plan_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricVolt,
                                contentColor = ObsidianBg
                            )
                        ) {
                            if (focusPlanState is FocusPlanState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = ObsidianBg)
                                Spacer(Modifier.width(8.dp))
                                Text("Building Custom Plan...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Generate Today's Personalized Focus Plan")
                            }
                        }

                        when (focusPlanState) {
                            is FocusPlanState.Success -> {
                                val plan = focusPlanState.plan
                                Surface(
                                    color = ObsidianSurfaceHigh,
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, ElectricVolt.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = plan.headline,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = ElectricVolt
                                        )
                                        Text(
                                            text = plan.confidenceCue,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        plan.drills.forEachIndexed { idx, d ->
                                            Text(
                                                text = "${idx + 1}. $d",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = "Dad / Coach Tip: ${plan.parentNote}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MambaGold
                                        )
                                    }
                                }
                            }
                            is FocusPlanState.Error -> {
                                Text(
                                    text = focusPlanState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            else -> Unit
                        }
                    }
                }

                val quickPrompts = listOf(
                    "I missed 4 shots in a row — how do I stay aggressive?",
                    "Analyze my Shot Lab zones & weak hand",
                    "Give me a 2-min pre-game confidence talk",
                    "How do I play like Paige & Caitlin in 7th grade?"
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickPrompts) { prompt ->
                        Surface(
                            color = ObsidianSurfaceHigh,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .border(1.dp, ObsidianBorder, RoundedCornerShape(20.dp))
                                .clickable(enabled = aiStreamState !is AiStreamState.Loading && aiStreamState !is AiStreamState.Streaming) {
                                    onAskCoachIq(prompt)
                                }
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.bodySmall,
                                color = ArenaCyan,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                chatMessages.forEach { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.fromCoach) Arrangement.Start else Arrangement.End
                    ) {
                        Surface(
                            color = if (msg.fromCoach) ObsidianSurface else CourtOrange.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .widthIn(max = 520.dp)
                                .border(
                                    1.dp,
                                    if (msg.fromCoach) ObsidianBorder else CourtOrange.copy(alpha = 0.5f),
                                    RoundedCornerShape(16.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (msg.fromCoach) "COACH IQ" else "YOU",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (msg.fromCoach) ArenaCyan else CourtOrange
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                when (aiStreamState) {
                    is AiStreamState.Loading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = ArenaCyan)
                            Spacer(Modifier.width(8.dp))
                            Text("Coach IQ is analyzing your game...", style = MaterialTheme.typography.bodySmall, color = ArenaCyan)
                        }
                    }
                    is AiStreamState.Streaming -> {
                        Surface(
                            color = ObsidianSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ArenaCyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("COACH IQ", style = MaterialTheme.typography.labelSmall, color = ArenaCyan)
                                Spacer(Modifier.height(4.dp))
                                Text(aiStreamState.text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    is AiStreamState.Error -> {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = aiStreamState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    else -> Unit
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { if (it.length <= 400) chatInput = it },
                        label = { Text("Ask Coach IQ anything...") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                onAskCoachIq(chatInput)
                                chatInput = ""
                            }
                        },
                        enabled = aiStreamState !is AiStreamState.Loading && aiStreamState !is AiStreamState.Streaming,
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("send_coach_iq_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArenaCyan,
                            contentColor = ObsidianBg
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send to Coach IQ")
                    }
                }
            }
        }
    }
}
