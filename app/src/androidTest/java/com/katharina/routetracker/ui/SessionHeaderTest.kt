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
            SessionHeader(
                startedAt = startedAt,
                stoppedAt = null,
                distanceMeters = 0.0,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Started: ${startedAt.toDisplayTime()}").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 m").assertIsDisplayed()
    }

    @Test
    fun sessionHeader_showsStoppedTimeWhenProvided() {
        val startedAt = System.currentTimeMillis() - 1000
        val stoppedAt = System.currentTimeMillis()
        composeTestRule.setContent {
            SessionHeader(
                startedAt = startedAt,
                stoppedAt = stoppedAt,
                distanceMeters = 500.0,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Started: ${startedAt.toDisplayTime()}").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stopped: ${stoppedAt.toDisplayTime()}").assertIsDisplayed()
        composeTestRule.onNodeWithText("500 m").assertIsDisplayed()
    }

    @Test
    fun sessionHeader_showsFormattedDistanceInKm() {
        composeTestRule.setContent {
            SessionHeader(
                startedAt = System.currentTimeMillis(),
                stoppedAt = null,
                distanceMeters = 1500.0,
                onBack = {}
            )
        }

        // 1500m should show as 1.50 km
        composeTestRule.onNodeWithText("1.50 km").assertIsDisplayed()
    }

    @Test
    fun sessionHeader_showsDashForNullStartedTime() {
        composeTestRule.setContent {
            SessionHeader(
                startedAt = null,
                stoppedAt = null,
                distanceMeters = 0.0,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Started: —").assertIsDisplayed()
    }
}
