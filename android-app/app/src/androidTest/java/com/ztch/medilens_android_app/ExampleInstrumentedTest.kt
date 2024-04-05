package com.ztch.medilens_android_app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import com.ztch.medilens_android_app.Authenticate.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */


@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.ztch.medilens_android_app", appContext.packageName)
    }

    @Test
    fun testHasRequiredPermissions() {
        val mainActivity = MainActivity()
        assertTrue(mainActivity.hasRequiredPermissions())
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLogin() {
        // Define a flag to track if the navigation action is invoked
        var navigateToHomePageCalled = false
        var navigateToSignUpCalled = false

        // Define a mock navigation action
        val mockNavigateToHomePage: () -> Unit = {
            navigateToHomePageCalled = true
        }

        val mockNavigateToSignUp: () -> Unit = {
            navigateToSignUpCalled = true
        }

        // Set the content with the mock navigation action
        composeTestRule.setContent {
            Login(
                onNavigateToHomePage = mockNavigateToHomePage,
                onNavigateToSignUp = { mockNavigateToSignUp }
            )
        }

        // Perform actions that trigger navigation, like clicking a button
        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()

        // Verify that the navigation action was invoked
        assertTrue(navigateToHomePageCalled)
        assertTrue(navigateToSignUpCalled)
    }




}