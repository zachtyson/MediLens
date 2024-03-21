package com.ztch.medilens_android_app.ApiUtils
import retrofit2.Call
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

    @FormUrlEncoded
    @POST("users/")
    fun createUser(
        @Field("email") email: String,
        @Field("password") password: String,
    ): Call<RegisterResponse> // TokenResponse is a data class you'll define to match the JSON response

}
