package com.example.homecaretracker

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.semantics.SemanticsProperties
import com.example.homecaretracker.data.database.HomeCareDatabase
import com.example.homecaretracker.data.repository.ApplianceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaintenanceManagementTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()
    private var cleanupApplianceName: String? = null

    @After
    fun removeTestAppliance() {
        runBlocking {
            val name = cleanupApplianceName ?: return@runBlocking
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val repository = ApplianceRepository(
                HomeCareDatabase.getDatabase(context).applianceDao()
            )
            repository.observeAll().first()
                .firstOrNull { appliance -> appliance.name == name }
                ?.let { appliance -> repository.delete(appliance.id) }
        }
    }

    @Test
    fun maintenanceTaskCanBeCreatedCompletedEditedAndDeletedWhileHistoryRemains() {
        val suffix = System.currentTimeMillis()
        val applianceName = "Тестовый прибор $suffix"
        val taskTitle = "Очистка фильтра $suffix"
        val editedTaskTitle = "Проверка фильтра $suffix"
        cleanupApplianceName = applianceName

        composeRule.onNode(hasText("Моя техника") and hasClickAction()).performClick()
        composeRule.onNodeWithText("Добавить устройство").performClick()
        composeRule.onNodeWithText("Название *").performTextInput(applianceName)
        composeRule.onNodeWithText("Выбрать категорию *").performClick()
        composeRule.onNodeWithText("Другое").performClick()
        composeRule.onNodeWithText("Сохранить").performScrollTo().performClick()
        waitForText(applianceName)

        composeRule.onNode(hasText("Обслуживание") and hasClickAction()).performClick()
        composeRule.onNodeWithText("Добавить").performClick()
        waitForText("Новая задача")
        composeRule.onNodeWithText("Выберите устройство *").performClick()
        composeRule.onNodeWithText(applianceName).performClick()
        composeRule.onNodeWithText("Название обслуживания *").performTextInput(taskTitle)
        composeRule.onNodeWithText("Описание").performTextInput("Промыть фильтр")
        composeRule.onNodeWithText("Периодичность, дней *").performTextInput("30")
        composeRule.onNodeWithText("Сохранить").performScrollTo().performClick()
        waitForText(taskTitle)

        composeRule.onNodeWithText(taskTitle).performClick()
        waitForText("Выполнено")
        composeRule.onNode(isToggleable()).assertIsOn().performClick()
        waitForText("Неактивна")
        composeRule.onNode(isToggleable()).assertIsOff().performClick()
        waitForText("Активна")
        composeRule.onNodeWithText("Выполнено").performClick()
        waitForText("Обслуживание записано в историю.")
        composeRule.onNodeWithText("Следующее обслуживание:", substring = true).assertExists()

        composeRule.activityRule.scenario.recreate()
        composeRule.onNode(hasText("Обслуживание") and hasClickAction()).performClick()
        waitForText(taskTitle)
        composeRule.onNode(hasText("История") and hasClickAction()).performClick()
        waitForText(taskTitle)
        composeRule.onNode(hasText("Обслуживание") and hasClickAction()).performClick()
        composeRule.onNodeWithText(taskTitle).performClick()
        waitForText("Редактировать")

        composeRule.onNodeWithText("Редактировать").performClick()
        composeRule.onNodeWithText("Название обслуживания *").performTextClearance()
        composeRule.onNodeWithText("Название обслуживания *").performTextInput(editedTaskTitle)
        composeRule.onNodeWithText("Сохранить").performScrollTo().performClick()
        waitForText(editedTaskTitle)

        composeRule.onNode(hasText("История") and hasClickAction()).performClick()
        waitForText(taskTitle)
        composeRule.onNodeWithText("Все устройства").performClick()
        composeRule.onAllNodesWithText(applianceName).get(1).performClick()
        composeRule.onNodeWithText("30 дней").performClick()
        composeRule.onNodeWithText(taskTitle).assertExists()
        composeRule.onAllNodesWithText(applianceName).assertCountEquals(2)
        composeRule.onNodeWithText(editedTaskTitle).assertDoesNotExist()
        composeRule.onNodeWithText(taskTitle).performClick()
        composeRule.onNodeWithText("Дата выполнения:", substring = true).assertExists()
        composeRule.onAllNodesWithText(applianceName).assertCountEquals(2)
        composeRule.onNodeWithText("Закрыть").performClick()

        composeRule.onNode(hasText("Главная") and hasClickAction()).performClick()
        composeRule.onNodeWithText("Устройств").assertExists()
        composeRule.onNodeWithText("Активных задач").assertExists()
        composeRule.onNodeWithText("Просрочено").assertExists()
        composeRule.onNodeWithText("Выполнено").assertExists()
        composeRule.onNodeWithText("Ближайшее обслуживание").assertExists()

        composeRule.onNode(hasText("Обслуживание") and hasClickAction()).performClick()
        composeRule.onNodeWithText(editedTaskTitle).performClick()
        composeRule.onNodeWithText("Удалить задачу").performScrollTo().performClick()
        composeRule.onNodeWithText("Удалить задачу?").assertExists()
        composeRule.onNodeWithText("Подтвердить удаление").performClick()
        waitForText("Добавить")
        composeRule.onNodeWithText(editedTaskTitle).assertDoesNotExist()

        composeRule.onNode(hasText("История") and hasClickAction()).performClick()
        composeRule.onNodeWithText(taskTitle).assertExists()
        composeRule.onNodeWithText(applianceName).assertExists()

        composeRule.onNode(hasText("Моя техника") and hasClickAction()).performClick()
        composeRule.onNodeWithText(applianceName).performClick()
        composeRule.onNodeWithText("Удалить устройство").performScrollTo().performClick()
        composeRule.onNodeWithText("Подтвердить удаление").performClick()
        waitForText("Добавить устройство")
    }

    private fun waitForText(text: String) {
        val deadline = System.currentTimeMillis() + 10_000
        while (System.currentTimeMillis() < deadline) {
            if (composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()) return
            Thread.sleep(100)
        }
        val screenText = composeRule
            .onAllNodesWithText("", substring = true, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .flatMap { node ->
                if (node.config.contains(SemanticsProperties.Text)) {
                    node.config[SemanticsProperties.Text].map { it.text }
                } else {
                    emptyList()
                }
            }
            .distinct()
            .joinToString()
        throw AssertionError("Timed out waiting for '$text'. Current screen: $screenText")
    }
}
