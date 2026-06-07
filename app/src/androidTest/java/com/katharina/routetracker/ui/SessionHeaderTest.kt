package com.katharina.routetracker.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class SessionHeaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun sessionHeader_showsStartedTime() {
        val startedAt = System.currentTimeMillis()
        composeTestRule.setContent {
            SessionHeader(startedAt = startedAt, stoppedAt = null)
        }

        composeTestRule.onNodeWithText("Started: ${startedAt.toDisplayTime()}").assertIsDisplayed()
    }

    @Test
    fun sessionHeader_showsStoppedTimeWhenProvided() {
        val startedAt = System.currentTimeMillis() - 1000
        val stoppedAt = System.currentTimeMillis()
        composeTestRule.setContent {
            SessionHeader(startedAt = startedAt, stoppedAt = stoppedAt)
        }

        composeTestRule.onNodeWithText("Started: ${startedAt.toDisplayTime()}").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stopped: ${stoppedAt.toDisplayTime()}").assertIsDisplayed()
    }

    @Test
    fun sessionHeader_showsDashForNullStartedTime() {
        composeTestRule.setContent {
            SessionHeader(startedAt = null, stoppedAt = null)
        }

        composeTestRule.onNodeWithText("Started: —").assertIsDisplayed()
    }
}
