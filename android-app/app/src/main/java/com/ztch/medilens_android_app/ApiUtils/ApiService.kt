package com.ztch.medilens_android_app.ApiUtils
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
interface ApiService {
    @FormUrlEncoded
    @POST("login/e/")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginTokenResponse> // TokenResponse is a data class you'll define to match the JSON response
    @POST("users/")
    fun createUser(@Body credentials: UserRegistrationCredentials): Call<RegisterResponse>

}

data class UserRegistrationCredentials(
    val email: String,
    val password: String,
)
