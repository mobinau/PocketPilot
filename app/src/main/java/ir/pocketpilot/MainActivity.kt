package ir.pocketpilot

import android.os.Bundle
import android.content.Context
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.pocketpilot.ui.FinanceViewModel
import ir.pocketpilot.ui.navigation.PocketApp
import ir.pocketpilot.ui.theme.PocketTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val locale = Locale.forLanguageTag("fa-IR")
        val configuration = Configuration(newBase.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Locale.setDefault(Locale.forLanguageTag("fa-IR"))
        enableEdgeToEdge()
        val app = application as PocketPilotApplication
        setContent {
            val vm: FinanceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return FinanceViewModel(app.repository, app.preferences, app.assistant, app.calendar) as T
                }
            })
            val state by vm.state.collectAsStateWithLifecycle()
            PocketTheme(state.preferences.theme) {
                val light = MaterialTheme.colorScheme.background.luminance() > .5f
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = light
                        isAppearanceLightNavigationBars = light
                    }
                }
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { PocketApp(vm, state) }
            }
        }
    }
}
