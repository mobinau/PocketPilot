package ir.pocketpilot

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun onboardingAndBottomNavigationWorkInPersian() {
        compose.waitUntil(15000) { compose.onAllNodesWithText("رد کردن").fetchSemanticsNodes().isNotEmpty() || compose.onAllNodesWithText("خانه").fetchSemanticsNodes().isNotEmpty() }
        if (compose.onAllNodesWithText("رد کردن").fetchSemanticsNodes().isNotEmpty()) compose.onNodeWithText("رد کردن").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("خانه").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("خانه").assertIsDisplayed()
        compose.onNodeWithText("تراکنش‌ها").performClick()
        compose.onNodeWithText("جست‌وجوی تراکنش‌ها...").assertIsDisplayed()
        compose.onNodeWithText("بودجه").performClick()
        compose.onNodeWithText("بودجه‌بندی").assertIsDisplayed()
        compose.onNodeWithText("گزارش‌ها").performClick()
        compose.onNodeWithText("گزارش مالی ماهانه").assertIsDisplayed()
        compose.onNodeWithText("تنظیمات").performClick()
        compose.onNodeWithText("ظاهر").assertIsDisplayed()
    }
}
