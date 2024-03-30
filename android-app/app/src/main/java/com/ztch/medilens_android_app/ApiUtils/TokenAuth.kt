package com.ztch.medilens_android_app.ApiUtils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import android.util.Log
import com.ztch.medilens_android_app.Camera.ImageAndPrediction
import java.io.ByteArrayOutputStream

object TokenAuth {

    fun isLoggedIn(context: Context): Boolean {
        return hasToken(context, "access_token")
    }

    fun logIn(context: Context, token: String): Boolean {
        if (token == "") {
            return false
        }
        saveToken(context, "access_token", token)
        deleteToken(context, "images")
        return true;
    }

    fun getLogInToken(context: Context): String {
        return getToken(context, "access_token")
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

    fun deleteToken(context: Context, token: String) {
        val sharedPref = context.getSharedPreferences("medilens", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(token)
            apply()
        }
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    fun base64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    //  val images = remember { mutableStateListOf<ImageAndPrediction>() }
    fun saveImagesAndPredictions(context: Context, images: List<ImageAndPrediction>) {
        val sharedPref = context.getSharedPreferences("medilens", Context.MODE_PRIVATE)
        // clear existing images
        with (sharedPref.edit()) {
            remove("images")
            apply()
        }
        // convert the bitmaps into base64 strings
        for (image in images) {
            image.base64String = bitmapToBase64(image.bitmap!!)
        }
        // remove the bitmaps
        for (image in images) {
            image.bitmap = null
        }
        with(sharedPref.edit()) {
            putString("images", Gson().toJson(images))
            apply()
        }
    }

    fun getImagesAndPredictions(context: Context): List<ImageAndPrediction>? {
        val sharedPref = context.getSharedPreferences("medilens", Context.MODE_PRIVATE)
        val json = sharedPref.getString("images", null)
        if(json == null) {
            return null
        }
        Log.d("images", "JSON: $json")

        val type = object : TypeToken<List<ImageAndPrediction>>() {}.type
        val s : List<ImageAndPrediction> = Gson().fromJson(json, type)
        // convert base64 strings back to bitmaps and delete the base64 strings
        for (image in s) {
            if(image.base64String == null) {
                //delete the image
                s.drop(s.indexOf(image))
                continue
            }
            image.bitmap = base64ToBitmap(image.base64String!!)
            image.base64String = null
        }
        return s
    }
}
