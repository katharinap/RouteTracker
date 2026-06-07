package com.katharina.routetracker.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.katharina.routetracker.domain.TrackingSession
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SessionListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun sessionList_displaysSessions() {
        val sessions = listOf(
            TrackingSession(id = 1, startedAt = 1000L),
            TrackingSession(id = 2, startedAt = 2000L)
        )

        composeTestRule.setContent {
            SessionList(
                sessions = sessions,
                onCreateSession = {},
                onSelectSession = {}
            )
        }

        composeTestRule.onNodeWithText("Session #1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Session #2").assertIsDisplayed()
    }

    @Test
    fun sessionList_callsOnSelectSession_whenItemClicked() {
        var selectedId: Long? = null
        val sessions = listOf(TrackingSession(id = 42))

        composeTestRule.setContent {
            SessionList(
                sessions = sessions,
                onCreateSession = {},
                onSelectSession = { selectedId = it }
            )
        }

        composeTestRule.onNodeWithText("Session #42").performClick()
        assertEquals(42L, selectedId)
    }

    @Test
    fun sessionList_callsOnCreateSession_whenButtonClicked() {
        var createCalled = false
        composeTestRule.setContent {
            SessionList(
                sessions = emptyList(),
                onCreateSession = { createCalled = true },
                onSelectSession = {}
            )
        }

        composeTestRule.onNodeWithText("New Session").performClick()
        assert(createCalled)
    }
}
