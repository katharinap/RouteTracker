package com.katharina.routetracker.ui

import androidx.compose.ui.test.junit4.createComposeRule
import com.katharina.routetracker.domain.TrackPoint
import org.junit.Rule
import org.junit.Test

class OsmMapViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun osmMapView_rendersWithoutCrashing() {
        val dummyPoints = listOf(
            TrackPoint(52.5200, 13.4050, 0),
            TrackPoint(52.5210, 13.4060, 0)
        )

        composeTestRule.setContent {
            OsmMapView(points = dummyPoints)
        }

        // Smoke test: if we reached this point without a crash, the test passes
    }

    @Test
    fun osmMapView_rendersWithEmptyPointsWithoutCrashing() {
        composeTestRule.setContent {
            OsmMapView(points = emptyList())
        }
    }
}
