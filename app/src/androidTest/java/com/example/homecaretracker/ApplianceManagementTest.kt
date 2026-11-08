package com.example.homecaretracker

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApplianceManagementTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun applianceCanBeCreatedViewedEditedPersistedAndDeletedWithConfirmation() {
        val applianceName = "Тестовый прибор ${System.currentTimeMillis()}"
        val updatedName = "$applianceName — обновлён"

        composeRule.onNode(
            hasText("Моя техника") and hasClickAction()
        ).performClick()
        composeRule.onNodeWithText("Добавить устройство").performClick()

        composeRule.onNodeWithText("Сохранить").performClick()
        composeRule.onNodeWithText("Новое устройство").assertExists()

        composeRule.onNodeWithText("Название *").performTextInput(applianceName)
        composeRule.onNodeWithText("Выбрать категорию *").performClick()
        composeRule.onNodeWithText("Холодильник").performClick()
        composeRule.onAllNodesWithText("Холодильник").assertCountEquals(1)
        composeRule.onNodeWithText("Производитель")
            .performScrollTo()
            .performClick()
            .performTextInput("Bosch")
        composeRule.onNodeWithText("Модель")
            .performScrollTo()
            .performClick()
            .performTextInput("WQG-123")
        composeRule.onNodeWithText(applianceName).assertExists()
        composeRule.onNodeWithText("Bosch").assertExists()
        composeRule.onNodeWithText("WQG-123").assertExists()
        composeRule.onNodeWithText("Сохранить").performScrollTo().performClick()
        waitForText("Добавить устройство")
        composeRule.onNodeWithText("Новое устройство").assertDoesNotExist()
        composeRule.onNodeWithText("Добавить устройство").assertExists()
        composeRule.onNodeWithText(applianceName).assertExists()
        composeRule.onNodeWithText("Bosch · WQG-123").assertExists()

        composeRule.onNodeWithText(applianceName).performClick()
        waitForText("Редактировать")
        composeRule.onNodeWithText("Производитель").assertExists()
        composeRule.onNodeWithText("Bosch").assertExists()
        composeRule.onNodeWithText("WQG-123").assertExists()

        composeRule.onNodeWithText("Редактировать").performScrollTo().performClick()
        composeRule.onNodeWithText("Название *").performTextClearance()
        composeRule.onNodeWithText("Название *").performTextInput("Несохранённое изменение")
        composeRule.onNodeWithText("Отмена").performScrollTo().performClick()
        waitForText("Редактировать")
        composeRule.onNodeWithText(applianceName).assertExists()

        composeRule.onNodeWithText("Редактировать").performScrollTo().performClick()
        composeRule.onNodeWithText("Название *").performTextClearance()
        composeRule.onNodeWithText("Название *").performTextInput(updatedName)
        composeRule.onNodeWithText("Сохранить").performScrollTo().performClick()
        waitForText("Редактировать")
        composeRule.onNodeWithText(updatedName).assertExists()

        composeRule.activityRule.scenario.recreate()
        composeRule.onNode(
            hasText("Моя техника") and hasClickAction()
        ).performClick()
        composeRule.onNodeWithText(updatedName).assertExists()

        composeRule.onNode(
            hasText("Обслуживание") and hasClickAction()
        ).performClick()
        composeRule.onAllNodesWithText("Обслуживание").assertCountEquals(2)
        composeRule.onNode(
            hasText("История") and hasClickAction()
        ).performClick()
        composeRule.onAllNodesWithText("История").assertCountEquals(2)
        composeRule.onNode(
            hasText("Моя техника") and hasClickAction()
        ).performClick()
        composeRule.onNodeWithText(updatedName).assertExists()

        composeRule.onNodeWithText(updatedName).performClick()
        waitForText("Удалить устройство")
        composeRule.onNodeWithText("Удалить устройство").performScrollTo().performClick()
        composeRule.onNodeWithText("Удалить устройство?").assertExists()
        composeRule.onNodeWithText("Отмена").performClick()
        composeRule.onNodeWithText(updatedName).assertExists()

        composeRule.onNodeWithText("Удалить устройство").performScrollTo().performClick()
        composeRule.onNodeWithText("Подтвердить удаление").performClick()
        waitForText("Добавить устройство")
        composeRule.onNodeWithText(updatedName).assertDoesNotExist()
    }

    private fun waitForText(text: String) {
        val deadline = System.currentTimeMillis() + 10_000
        while (System.currentTimeMillis() < deadline) {
            if (composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()) {
                return
            }
            Thread.sleep(100)
        }
        val currentTexts = composeRule
            .onAllNodes(hasText("", substring = true), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .flatMap { node ->
                if (node.config.contains(SemanticsProperties.Text)) {
                    node.config[SemanticsProperties.Text].map { it.text }
                } else {
                    emptyList()
                } + if (node.config.contains(SemanticsProperties.EditableText)) {
                    listOf(node.config[SemanticsProperties.EditableText].text)
                } else {
                    emptyList()
                }
            }
            .distinct()
            .joinToString()
        throw AssertionError(
            "Timed out waiting for '$text'. Current screen text: $currentTexts"
        )
    }
}
