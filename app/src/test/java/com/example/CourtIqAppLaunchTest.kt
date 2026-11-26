package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CourtIqAppLaunchTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchesLocallyAndNavigatesAcrossCoreTrainingFeatures() {
        composeRule.onNodeWithTag("dashboard_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("start_todays_workout_button")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("workout_play_pause_button")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("workout_back_button")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("main_screen_scaffold").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_workouts").performClick()
        composeRule.onNodeWithTag("train_section_0").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_shot_lab").performClick()
        composeRule.onNodeWithTag("interactive_shot_court").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("nav_coach").performClick()
        composeRule.onNodeWithTag("coach_subtab_parent_mode").performClick()
        composeRule.onNodeWithText("PARENT / COACH MODE").assertIsDisplayed()
    }
}
