package com.ztch.medilens_android_app

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ztch.medilens_android_app.ApiUtils.ApiService
import com.ztch.medilens_android_app.ApiUtils.UserRegistrationCredentials
import com.ztch.medilens_android_app.Authenticate.Login
import com.ztch.medilens_android_app.Authenticate.SignUp
import com.ztch.medilens_android_app.Homepage.CalendarDataSource
import com.ztch.medilens_android_app.Homepage.homepageHeader
import com.ztch.medilens_android_app.Notifications.AlarmBroadcaster
import com.ztch.medilens_android_app.Notifications.AlarmTimesScreen
import com.ztch.medilens_android_app.Notifications.PillInformationScreen
import com.ztch.medilens_android_app.Refill.AddMedication
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*



/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */


@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val mActivityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        // No need to explicitly launch the activity here as ActivityScenarioRule does it for us
    }

    @Test
    fun testHasRequiredPermissions() {
        // Wait for the activity to be resumed and check permissions
        mActivityRule.scenario.onActivity { activity ->
            assertTrue(!activity.hasRequiredPermissions())
        }
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.ztch.medilens_android_app", appContext.packageName)
    }



}

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginApiSuccess() {
        val email = "example@example.com"
        val password = "password"

        // Set the content with the Login composable
        composeTestRule.setContent {
            Login({},{})
        }

        composeTestRule.onNodeWithText("Email")
            .performTextInput(email)
        composeTestRule.onNodeWithText("Password")
            .performTextInput(password)

        composeTestRule.onNodeWithTag("LoginButton").performClick()
        composeTestRule.onNodeWithText(email)
        composeTestRule.onNodeWithText(password)


        // Start MockWebServer
        val mockWebServer = MockWebServer()
        mockWebServer.start()

        // Enqueue a mock response
        val response = MockResponse()
            .setResponseCode(200)
            .setBody("{\"access_token\": \"mock_token\"}")
        mockWebServer.enqueue(response)

        // Make a network request to the MockWebServer
        val baseUrl = mockWebServer.url("/").toString()
        val apiService = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        // Perform the login request
        val loginResponse = apiService.loginUser(email, password).execute()

        // Verify that the request was successful
        assertTrue(loginResponse.isSuccessful)
        assertEquals(200, loginResponse.code())

        val responseBody = loginResponse.body()
        assertEquals("mock_token", responseBody?.access_token)

        val request = mockWebServer.takeRequest()
        assertEquals("/login/e/", request.path)

        // Shut down the MockWebServer
        mockWebServer.shutdown()
    }

    @Test
    fun testLoginApifail() {
        val email = "example1@example.com"
        val password = "password1"

        composeTestRule.setContent {
            Login({},{})
        }

        composeTestRule.onNodeWithText("Email")
            .performTextInput(email)
        composeTestRule.onNodeWithText("Password")
            .performTextInput(password)

        composeTestRule.onNodeWithTag("LoginButton").performClick()
        composeTestRule.onNodeWithText(email)
        composeTestRule.onNodeWithText(password)

        val mockWebServer = MockWebServer()
        mockWebServer.start()

        val response = MockResponse()
            .setResponseCode(400) // or any other error code
            .setBody("{\"error\": \"invalid_credentials\"}")
        mockWebServer.enqueue(response)

        val baseUrl = mockWebServer.url("/").toString()
        val apiService = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        // Perform the login request
        val loginResponse = apiService.loginUser(email, password).execute()

        // Verify that the request was unsuccessful
        assertTrue(!loginResponse.isSuccessful)
        assertEquals(400, loginResponse.code())

        val responseBody = loginResponse.body()
        assertEquals(null, responseBody?.access_token)

        val request = mockWebServer.takeRequest()
        Log.d("test", "testLoginApifail: ${request.path}")
        assertEquals("/login/e/", request.path)

        mockWebServer.shutdown()
    }


    @Test
    fun signUpFailure() {
        // Launch the SignUp composable
        composeTestRule.setContent {
            SignUp({}, {})
        }

        // Input values into the text fields
        composeTestRule.onNodeWithText("Full Name").performTextInput("Zachuerta")
        composeTestRule.onNodeWithText("Email").performTextInput("Zachuerta@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("wrong_password")

        // Click on the Register button
        composeTestRule.onNodeWithText("Register").performClick()

        // Verify if error text is displayed
        composeTestRule.onNodeWithText("Passwords do not match").assertExists()
    }

    @Test
    fun signUpFailure1() {

        composeTestRule.setContent {
            SignUp({}, {})
        }
        composeTestRule.onNodeWithText("Register").performClick()
        // Verify if error text is displayed
        composeTestRule.onNodeWithText("Please fill all fields").assertExists()
    }

    @Test
    fun signUpSuccess() {

        val mockWebServer = MockWebServer()
        mockWebServer.start()

        // Enqueue a successful response from the mock server
        val responseBody = "{\"success\": true}"
        val response = MockResponse()
            .setResponseCode(200)
            .setBody(responseBody)
        mockWebServer.enqueue(response)

        val baseUrl = mockWebServer.url("/").toString()
        val apiService = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)


        composeTestRule.setContent {
            SignUp({}, {})
        }


        composeTestRule.onNodeWithText("Full Name").performTextInput("Zachuerta")
        composeTestRule.onNodeWithText("Email").performTextInput("Zachuerta@email.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("password")
        composeTestRule.onNodeWithText("Register").performClick()

        val userToCreate = UserRegistrationCredentials("Zachuerta@email.com", "password")
        val registerResponse = apiService.createUser(userToCreate).execute()
        assertTrue(registerResponse.isSuccessful)
    }


    @Test
    fun signUpFail2() {
        val mockWebServer = MockWebServer()
        mockWebServer.start()

        val responseBody = "{\"error\": \"Server error\"}"
        val response = MockResponse()
            .setResponseCode(500) // Set response code to indicate failure
            .setBody(responseBody)
        mockWebServer.enqueue(response)

        val baseUrl = mockWebServer.url("/").toString()
        val apiService = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        composeTestRule.setContent {
            SignUp({}, {})
        }

        composeTestRule.onNodeWithText("Full Name").performTextInput("Zachuerta")
        composeTestRule.onNodeWithText("Email").performTextInput("Zachuerta@email.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("password")
        composeTestRule.onNodeWithText("Register").performClick()

        val userToCreate = UserRegistrationCredentials("Zachuerta@email.com", "password")
        val registerResponse = apiService.createUser(userToCreate).execute()
        assertFalse(registerResponse.isSuccessful) // Expect the request to fail
    }

    @Test
    fun signUpNav() {

        composeTestRule.setContent {
            SignUp( {}, {})
        }
        composeTestRule.onNodeWithText("Log In").performClick()
        // CHECK if ICON is displayed
        composeTestRule.onNodeWithContentDescription("Back").assertExists()
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        composeTestRule.onNodeWithText("Register").assertExists().performClick()
    }

    @Test
    fun loginNav() {
        composeTestRule.setContent {
            Login({}, {})
        }
        composeTestRule.onNodeWithText("Sign Up").performClick()
        composeTestRule.onNodeWithTag("LoginButton").assertExists().performClick()

    }

}

@RunWith(AndroidJUnit4::class)
class HomescreenTest {

    //**** TEST WITH ALARM VIEWMODEL FAILS AS ITS FINAL CLASS AND CAN NOT BE MOCKED ****

    @get:Rule
    val composeTestRule = createComposeRule()


    @Test
    fun testHomeScreenNav() {
        // Set the content with the Home composable
        composeTestRule.setContent {
            appbarBottom({},{},{},{},{})
        }

        composeTestRule.onNodeWithContentDescription("settings").assertExists().performClick()
        composeTestRule.onNodeWithContentDescription("Cabinet").assertExists().performClick()
        composeTestRule.onNodeWithContentDescription("alerts").assertExists().performClick()
        composeTestRule.onNodeWithContentDescription("camera").assertExists().performClick()
        composeTestRule.onNodeWithContentDescription("medicard").assertExists().performClick()
    }

    @Test
    fun testHomeScreen1() {

        val dataSource = CalendarDataSource()
        val calendarUiModel = dataSource.getData(lastSelectedDate = dataSource.today)

        composeTestRule.setContent {
           homepageHeader(data = calendarUiModel,{})
        }

        composeTestRule.onNodeWithText("Today").assertExists()


        val date = Calendar.getInstance().time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        //click on the next date card  and see if the home page header change.
        val dayOfWeek = date.dayOfMonth + 1

        composeTestRule.onNodeWithText(dayOfWeek.toString()).assertExists().performClick()


        val dayHeader: String = date.format(DateTimeFormatter.ofPattern("E"))
        composeTestRule.onNodeWithText(dayHeader).assertExists()

    }
}


@RunWith(AndroidJUnit4::class)
class NotificationChannelTest {
    @Test
    fun testNotificationChannelCreation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Context of the app under test.
            val context = ApplicationProvider.getApplicationContext<Context>()

            val name = "Pill Reminder"
            val descriptionText = "Time to take your pills!"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("alarm_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            val createdChannel = notificationManager.getNotificationChannel("alarm_id")

            assertNotNull(createdChannel)
            assertEquals(name, createdChannel.name)
            assertEquals(descriptionText, createdChannel.description)
            assertEquals(importance, createdChannel.importance)
        }
    }

    @Test
    fun testNotification() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, AlarmBroadcaster::class.java)
        intent.putExtra("EXTRA_MESSAGE", "Pills")
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "alarm_id")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Pill Reminder")
            .setContentText("Time to take your pills!")
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1, notification)
    }

}


@RunWith(AndroidJUnit4::class)
class addAlarmsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

@Test
    fun AlarmTimesScreenNav() {
        // Set the content with the Home composable
        composeTestRule.setContent {
            PillInformationScreen({},{time, repetition, message, dosage, form -> })
        }

        composeTestRule.onNodeWithContentDescription("backAlert").assertExists().performClick()
    }

    @Test
    fun testAlarmTimesScreen() {
        // Set the content with the Home composable
        composeTestRule.setContent {
            PillInformationScreen({},{time, repetition, message, dosage, form -> })
        }

        composeTestRule.onNodeWithText("Add Reminder Details").assertExists()
        composeTestRule.onNodeWithText("Medication Name").assertExists()
        composeTestRule.onNodeWithText("Strength (mg)").assertExists()
        composeTestRule.onNodeWithText("RX Number").assertExists()
        composeTestRule.onNodeWithText("Dosage").assertExists()
        composeTestRule.onNodeWithText("Form").assertExists()
        composeTestRule.onNodeWithContentDescription("Dropdown").assertExists().performClick()
    }

    @Test
    fun testAlarmTimesScreen1() {

        composeTestRule.setContent {
            PillInformationScreen({},{time, repetition, message, dosage, form -> })
        }

        composeTestRule.onNodeWithText("Add Reminder Details").assertExists()
        composeTestRule.onNodeWithText("Medication Name").assertExists()
        composeTestRule.onNodeWithText("Strength (mg)").assertExists()
        composeTestRule.onNodeWithText("RX Number").assertExists()
        composeTestRule.onNodeWithText("Dosage").assertExists()
        composeTestRule.onNodeWithText("Form").assertExists()
        composeTestRule.onNodeWithContentDescription("Dropdown").assertExists().performClick()

        // fill in the nodes
        composeTestRule.onNodeWithText("Medication Name").performTextInput("Pills")
        composeTestRule.onNodeWithText("Strength (mg)").performTextInput("10")
        composeTestRule.onNodeWithText("RX Number").performTextInput("1234")
        composeTestRule.onNodeWithText("Dosage").performTextInput("1")
        composeTestRule.onNodeWithText("Form").performTextInput("Tablet")
        composeTestRule.onNodeWithText("Set Alarm Times").performClick()
    }

    @Test
    fun missingFields() {
        composeTestRule.setContent {
            PillInformationScreen({},{time, repetition, message, dosage, form -> })
        }

        composeTestRule.onNodeWithText("Set Alarm Times").performClick()
        composeTestRule.onNodeWithText("All text fields required!").assertExists()
    }

    @Test
    fun missingFields1() {
        // Set the content with the Home composable
        composeTestRule.setContent {
           AddMedication({},{},{})
        }

        composeTestRule.onNodeWithText("Set Alarm Times").performClick()
        composeTestRule.onNodeWithText("All text fields required!").assertExists()
    }

    @Test
    fun addMedicationNav() {
        // Set the content with the Home composable
        composeTestRule.setContent {
            AddMedication({},{}, {})
        }

        composeTestRule.onNodeWithContentDescription("Localized description").assertExists().performClick()

        // fill in text fields
        composeTestRule.onNodeWithText("Medication Name").performTextInput("Pills")
        composeTestRule.onNodeWithText("Dosage").performTextInput("10")
        composeTestRule.onNodeWithText("Imprint").performTextInput("555")
        composeTestRule.onNodeWithText("Shape").performTextInput("pill")
        composeTestRule.onNodeWithText("Color").performTextInput("blue")
        composeTestRule.onNodeWithText("Intake Method").performTextInput("eat")
        composeTestRule.onNodeWithText("Description").performTextInput("Tablet")

    }

}


@RunWith(Suite::class)
@Suite.SuiteClasses(
    ExampleInstrumentedTest::class,
    LoginScreenTest::class,
    HomescreenTest::class,
    NotificationChannelTest::class,
    addAlarmsTest::class
)
class AllTestsSuite

