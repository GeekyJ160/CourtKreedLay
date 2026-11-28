package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.CoachChatMessage
import com.example.model.AuthorRole
import com.example.model.FatigueLevel
import com.example.model.MindsetMission
import com.example.model.ScheduleDay
import com.example.model.SchoolLoad
import com.example.viewmodel.AiStreamState
import com.example.viewmodel.DashboardBundle
import com.example.viewmodel.FocusPlanState

enum class CoachSubTab(val label: String) {
    AI_COACH("Coach IQ"),
    MINDSET("Mindset"),
    PARENT_MODE("Parent Mode"),
}

@Composable
fun CoachHubScreen(
    selectedSubTab: CoachSubTab,
    onSelectSubTab: (CoachSubTab) -> Unit,
    bundle: DashboardBundle,
    userEmail: String?,
    chatMessages: List<CoachChatMessage>,
    aiStreamState: AiStreamState,
    focusPlanState: FocusPlanState,
    onIncrementMission: (MindsetMission) -> Unit,
    onToggleMission: (MindsetMission) -> Unit,
    onAddCustomMission: (String, String, Int) -> Unit,
    onResetWeeklyMissions: () -> Unit,
    onAddObservation: (AuthorRole, String, String) -> Unit,
    onDeleteObservation: (String) -> Unit,
    onAskCoachIq: (String) -> Unit,
    onGenerateFocusPlan: () -> Unit,
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
    onUpdateScheduleDay: (ScheduleDay, String, Int, SchoolLoad, Boolean) -> Unit,
    onSignOut: () -> Unit,
    onRequestSignIn: (() -> Unit)? = null,
    cloudStorageAvailable: Boolean = false,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CoachSubTab.entries.forEach { tab ->
                FilterChip(
                    selected = selectedSubTab == tab,
                    onClick = { onSelectSubTab(tab) },
                    label = { Text(tab.label, maxLines = 1) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("coach_subtab_${tab.name.lowercase()}")
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedSubTab) {
                CoachSubTab.AI_COACH -> {
                    CoachIqOnlyContent(
                        bundle = bundle,
                        chatMessages = chatMessages,
                        aiStreamState = aiStreamState,
                        focusPlanState = focusPlanState,
                        onAskCoachIq = onAskCoachIq,
                        onGenerateFocusPlan = onGenerateFocusPlan
                    )
                }
                CoachSubTab.MINDSET -> {
                    MambaMindsetOnlyContent(
                        bundle = bundle,
                        onIncrementMission = onIncrementMission,
                        onToggleMission = onToggleMission,
                        onAddCustomMission = onAddCustomMission,
                        onResetWeeklyMissions = onResetWeeklyMissions,
                        onAddCourageLog = onAddObservation
                    )
                }
                CoachSubTab.PARENT_MODE -> {
                    ParentCoachScreen(
                        bundle = bundle,
                        userEmail = userEmail,
                        onSaveCalibration = onSaveCalibration,
                        onAddObservation = onAddObservation,
                        onDeleteObservation = onDeleteObservation,
                        onUpdateScheduleDay = onUpdateScheduleDay,
                        onSignOut = onSignOut,
                        onRequestSignIn = onRequestSignIn,
                        cloudStorageAvailable = cloudStorageAvailable
                    )
                }
            }
        }
    }
}
