package com.example.homecaretracker

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemePreferenceTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun selectedThemeIsRestoredAfterActivityRecreation() {
        composeRule.onNodeWithContentDescription("Настройки темы").performClick()
        composeRule.onNodeWithTag("theme_DARK").performClick()

        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithContentDescription("Настройки темы").performClick()
        composeRule.onNodeWithTag("theme_DARK").assertIsSelected()

        composeRule.onNodeWithTag("theme_SYSTEM").performClick()
    }
}
