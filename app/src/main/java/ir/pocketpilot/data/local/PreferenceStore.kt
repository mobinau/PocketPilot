package ir.pocketpilot.data.local
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import ir.pocketpilot.domain.model.*
import ir.pocketpilot.domain.model.Preferences
import kotlinx.coroutines.flow.map
private val Context.preferences by preferencesDataStore("pocketpilot_preferences")
class PreferenceStore(private val context: Context) {
    private val onboarding = booleanPreferencesKey("onboarding")
    private val theme = stringPreferencesKey("theme")
    private val currency = stringPreferencesKey("currency")
    private val provider = stringPreferencesKey("provider")
    val state = context.preferences.data.map { Preferences(it[onboarding] ?: false, runCatching { ThemeMode.valueOf(it[theme] ?: "SYSTEM") }.getOrDefault(ThemeMode.SYSTEM), runCatching { Currency.valueOf(it[currency] ?: "IRR") }.getOrDefault(Currency.IRR), it[provider] ?: "", true) }
    suspend fun finishOnboarding() { context.preferences.edit { it[onboarding] = true } }
    suspend fun theme(value: ThemeMode) { context.preferences.edit { it[theme] = value.name } }
    suspend fun currency(value: Currency) { context.preferences.edit { it[currency] = value.name } }
    suspend fun provider(value: String) { context.preferences.edit { it[provider] = value.trim() } }
}
