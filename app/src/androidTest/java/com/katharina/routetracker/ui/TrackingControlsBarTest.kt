package com.katharina.routetracker.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.katharina.routetracker.domain.TrackingState
import com.katharina.routetracker.domain.toControlsState
import org.junit.Rule
import org.junit.Test

class TrackingControlsBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun idleState_showsOnlyStartButton() {
        composeTestRule.setContent {
            TrackingControlsBar(
                controls = TrackingState.IDLE.toControlsState(),
                onStart = {}, onPause = {}, onResume = {}, onStop = {}
            )
        }

        composeTestRule.onNodeWithText("Start Tracking").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pause").assertDoesNotExist()
        composeTestRule.onNodeWithText("Resume").assertDoesNotExist()
        composeTestRule.onNodeWithText("Stop").assertDoesNotExist()
    }

    @Test
    fun trackingState_showsPauseAndStopButtons() {
        composeTestRule.setContent {
            TrackingControlsBar(
                controls = TrackingState.TRACKING.toControlsState(),
                onStart = {}, onPause = {}, onResume = {}, onStop = {}
            )
        }

        composeTestRule.onNodeWithText("Start Tracking").assertDoesNotExist()
        composeTestRule.onNodeWithText("Pause").assertIsDisplayed()
        composeTestRule.onNodeWithText("Resume").assertDoesNotExist()
        composeTestRule.onNodeWithText("Stop").assertIsDisplayed()
    }

    @Test
    fun pausedState_showsResumeAndStopButtons() {
        composeTestRule.setContent {
            TrackingControlsBar(
                controls = TrackingState.PAUSED.toControlsState(),
                onStart = {}, onPause = {}, onResume = {}, onStop = {}
            )
        }

        composeTestRule.onNodeWithText("Start Tracking").assertDoesNotExist()
        composeTestRule.onNodeWithText("Pause").assertDoesNotExist()
        composeTestRule.onNodeWithText("Resume").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stop").assertIsDisplayed()
    }
}
