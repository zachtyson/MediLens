package com.ztch.medilens_android_app.Medicard

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.ztch.medilens_android_app.ApiUtils.ApiService
import com.ztch.medilens_android_app.ApiUtils.EmailRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback
import androidx.compose.ui.Modifier
import com.ztch.medilens_android_app.ApiUtils.EmailResponse
import com.ztch.medilens_android_app.ApiUtils.RetrofitClient
import retrofit2.http.Body
import retrofit2.http.POST


/*** andriod Skeleton code that matches the CEN email project***/

@Composable
fun MediCardEmailScreen() {
    var to by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    /* TODO body to medicard  and fetch users doctor email  */


    val token = "your_authentication_token"

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        TextField(
            value = to,
            onValueChange = { to = it },
            label = { Text("To:") }
        )
        TextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject:") }
        )
        TextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Body:") }
        )
        Button(
            onClick = {
                val emailRequest = EmailRequest(to, subject, body)
                RetrofitClient.apiService.sendEmail(token, emailRequest).enqueue(object : Callback<EmailResponse> {
                    override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                        if (response.isSuccessful) {
                            val emailResponse = response.body()
                            // Handle the email response
                            emailResponse?.let {
                                Log.d("medicard email suc", "Email sent successfully. Message: ${it.message}")
                            }
                        } else {
                            // Error sending email
                            Log.e("medicard email error:" ,"Failed to send email: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                        // Network error
                        Log.e("medicard email error:", "Failed to send email: ${t.message}")
                    }
                })
            }
        ) {
            Text("Send Email")
        }
    }
}