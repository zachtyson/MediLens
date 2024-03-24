package com.ztch.medilens_android_app.ApiUtils

import android.content.Context
object LoginAuth {
    fun isLoggedIn(context: Context): Boolean {
        return hasToken(context, "access_token")
    }

    fun logIn(context: Context, token: String): Boolean {
        if (token == "") {
            return false
        }
        saveToken(context, "access_token", token)
        return true;
    }

    fun saveToken(context: Context, token_name: String, token: String) {
        val sharedPref = context.getSharedPreferences("medilens", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(token_name, token)
            apply()
        }
    }

    fun hasToken(context: Context, token: String): Boolean {
        // check if token exists
        val sharedPref = context.getSharedPreferences("medilens", Context.MODE_PRIVATE)
        return sharedPref.contains(token)
    }

    fun getToken(context: Context, token: String): String {
        val sharedPref = context.getSharedPreferences("medilens", Context.MODE_PRIVATE)
        return sharedPref.getString(token, "").toString()
    }
}
