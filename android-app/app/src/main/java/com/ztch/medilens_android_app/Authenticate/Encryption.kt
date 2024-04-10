package com.ztch.medilens_android_app.Authenticate
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.ztch.medilens_android_app.ApiUtils.TokenAuth
import java.security.MessageDigest
import java.util.*
import android.util.Log
import com.google.gson.Gson
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.util.Base64.getDecoder
import java.util.Base64.getEncoder
import javax.crypto.spec.IvParameterSpec
import java.util.Base64

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
    Log.d("Encryption", "Hashed password: $p")
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

fun decodeBase64Url(base64UrlString: String): String {
    // Replace URL-specific chars with regular Base64 chars
    val base64String = base64UrlString.replace("-", "+").replace("_", "/")
    // Decode the Base64Url string
    val decodedBytes = Base64.getDecoder().decode(base64String)
    // Convert the decoded bytes to a string
    return String(decodedBytes)
}

fun decodeJwt(jwt: String): Pair<String, String> {
    // Split the JWT into its components
    val parts = jwt.split(".")
    if (parts.size < 2) {
        throw IllegalArgumentException("Invalid JWT: Parts are missing")
    }
    // Decode the header and payload
    val header = decodeBase64Url(parts[0])
    val payload = decodeBase64Url(parts[1])
    return header to payload
}

data class JwtPayload(
    val sub: String,
    val exp: Long
)

data class SubPayload(
    val email: String,
    @SerializedName("id") val userId: String
)

fun getEmailFromJwt(jwt: String): String {
    val (_, payload) = decodeJwt(jwt)
    val gson = Gson()
    val payloadObj = gson.fromJson(payload, JwtPayload::class.java)
    val subPayload = gson.fromJson(payloadObj.sub, SubPayload::class.java)
    return subPayload.email
}

fun getUserIdFromJwt(jwt: String): String {
    val (_, payload) = decodeJwt(jwt)
    val gson = Gson()
    val payloadObj = gson.fromJson(payload, JwtPayload::class.java)
    val subPayload = gson.fromJson(payloadObj.sub, SubPayload::class.java)
    return subPayload.userId
}

fun encryptData(data: String, key: String, init: String): String {
    val iv = IvParameterSpec(Base64.getDecoder().decode(init))
    val keyBytes = key.toByteArray(Charsets.UTF_8)
    val skeySpec = SecretKeySpec(keyBytes, "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
    val encrypted = cipher.doFinal(data.toByteArray())
    return Base64.getEncoder().encodeToString(encrypted)
}

fun decryptData(data: String, key: String, init: String): String {
    val iv = IvParameterSpec(Base64.getDecoder().decode(init))
    val keyBytes = key.toByteArray(Charsets.UTF_8)
    val skeySpec = SecretKeySpec(keyBytes, "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
    val decrypted = cipher.doFinal(Base64.getDecoder().decode(data))
    return String(decrypted)
}

fun createRandomIV(): String {
    val random = Random()
    val iv = ByteArray(16)
    random.nextBytes(iv)
    return Base64.getEncoder().encodeToString(iv)
}
