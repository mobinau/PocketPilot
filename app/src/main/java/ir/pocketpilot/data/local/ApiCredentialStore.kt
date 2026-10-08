package ir.pocketpilot.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Credentials stay encrypted on this device, and are never included in prompts. */
class ApiCredentialStore(context: Context) {
    private val prefs = context.getSharedPreferences("encrypted_provider", Context.MODE_PRIVATE)
    private val alias = "pocketpilot_provider_key"
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return (store.getKey(alias, null) as? SecretKey) ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    suspend fun save(value: String) = withContext(Dispatchers.IO) {
        if (value.isBlank()) { check(prefs.edit().clear().commit()); return@withContext }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        check(prefs.edit().putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP)).putString("value", Base64.encodeToString(cipher.doFinal(value.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)).commit())
    }
    fun configured() = prefs.contains("value")
    suspend fun read(): String? = withContext(Dispatchers.IO) {
        val value = prefs.getString("value", null) ?: return@withContext null
        val iv = Base64.decode(prefs.getString("iv", null), Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv)) }
        String(cipher.doFinal(Base64.decode(value, Base64.NO_WRAP)), Charsets.UTF_8)
    }
}
