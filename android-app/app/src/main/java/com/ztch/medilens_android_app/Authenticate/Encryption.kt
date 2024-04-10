package com.ztch.medilens_android_app.Authenticate
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.ztch.medilens_android_app.ApiUtils.TokenAuth
import java.security.MessageDigest

fun hashAndStorePassword(context: Context, password: String, userID: String) {
    if (password.isEmpty() || userID.isEmpty()) {
        return
    }

    // basic client-side encryption for user's medical data
    // this isn't very secure but it's better than storing medical data in plaintext
    // uses SHA-256 hashing with the user's password and the user's ID as the salt
    val saltedPassword = password + userID
    // the hash is then used to encrypt the user's medical data (the key is the hash)

    val p = hashWithSHA256(saltedPassword)
    storeKey(context, p)
}

fun hashWithSHA256(input: String): String {
    val bytes = input.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }
}

fun storeKey(context: Context, key: String) {
    TokenAuth.saveToken(context, "encryption_key", key)
}

fun getKey(context: Context): String {
    return TokenAuth.getToken(context, "encryption_key")
}
