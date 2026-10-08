package ir.pocketpilot.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import ir.pocketpilot.BuildConfig
import ir.pocketpilot.data.local.ApiCredentialStore
import ir.pocketpilot.domain.model.*
import ir.pocketpilot.ui.*
import ir.pocketpilot.ui.components.*
import kotlinx.coroutines.launch

@Composable fun SettingsScreen(vm: FinanceViewModel, state: AppState) {
    var clear by remember { mutableStateOf(false) }
    var demo by remember { mutableStateOf(false) }
    var ai by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val credentials = remember { ApiCredentialStore(context.applicationContext) }
    var configured by remember { mutableStateOf(credentials.configured()) }
    val scope = rememberCoroutineScope()
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { ScreenHeading("تنظیمات", "پول‌یار، به سلیقه شما") }
        item { SectionHeading("ظاهر") }
        item { Panel { ThemeMode.entries.forEach { mode -> SettingChoice(mode.label, state.preferences.theme == mode) { vm.theme(mode) } } } }
        item { SectionHeading("واحد پول") }
        item { Panel { Currency.entries.forEach { currency -> SettingChoice(currency.label, state.preferences.currency == currency) { vm.currency(currency) } }; Text("ریال و تومان دو نمایش از یک دفتر مالی هستند. دلار و یورو دفترهای جدا دارند؛ تبدیل نرخ ارز انجام نمی‌شود.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        item { SectionHeading("زبان") }
        item { Panel { Metric("زبان برنامه", "فارسی"); Text("تاریخ‌ها و دوره‌های مالی ماهانه و سالانه بر اساس تقویم شمسی هستند.", style = MaterialTheme.typography.bodySmall) } }
        item { SectionHeading("دستیار هوشمند") }
        item { Panel {
            Metric("وضعیت دستیار", "حالت آزمایشی")
            Text("تحلیل محلی فعال است و به اینترنت یا کلید API نیاز ندارد.")
            OutlinedButton(onClick = { ai = true }, modifier = Modifier.fillMaxWidth()) { Text("تنظیمات دستیار هوشمند") }
            Text(if (configured) "کلید API به‌صورت رمزگذاری‌شده ذخیره شده است." else "کلید API ثبت نشده است.", style = MaterialTheme.typography.bodySmall)
        } }
        item { SectionHeading("داده‌ها") }
        item { Panel {
            TextButton(onClick = { demo = true }) { Icon(Icons.Rounded.Refresh, null); Spacer(Modifier.width(8.dp)); Text("بارگذاری دوباره داده‌های نمونه") }
            TextButton(onClick = { clear = true }) { Icon(Icons.Rounded.DeleteOutline, null, tint = MaterialTheme.colorScheme.error); Spacer(Modifier.width(8.dp)); Text("حذف تمام داده‌ها", color = MaterialTheme.colorScheme.error) }
        } }
        item { SectionHeading("درباره پول‌یار") }
        item { Panel { Text("پول‌یار همراه شما برای ثبت تراکنش‌ها، بودجه‌بندی و شناخت الگوهای مالی است. اطلاعات مالی روی دستگاه شما می‌ماند."); Metric("نسخه برنامه", BuildConfig.VERSION_NAME); Text("PocketPilot", style = MaterialTheme.typography.labelMedium) } }
    }
    if (clear) ConfirmDialog("حذف تمام اطلاعات", "تمام اطلاعات مالی ذخیره‌شده حذف خواهند شد. آیا مطمئن هستید؟", { clear = false }, { vm.clear(); clear = false })
    if (demo) ConfirmDialog("بارگذاری داده‌های نمونه", "تراکنش‌ها و بودجه‌های فعلی با داده‌های نمونه جایگزین می‌شوند. آیا مطمئن هستید؟", { demo = false }, { vm.reloadDemo(); demo = false }, "بارگذاری")
    if (ai) {
        var provider by remember { mutableStateOf(state.preferences.provider) }
        var key by remember { mutableStateOf("") }
        var remove by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var saving by remember { mutableStateOf(false) }
        AlertDialog(onDismissRequest = { if (!saving) ai = false }, title = { Text("تنظیمات دستیار هوشمند") }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("حالت آزمایشی فعال است. اتصال واقعی به سرویس، پس از اضافه کردن پیاده‌سازی ارائه‌دهنده در کد فعال می‌شود.")
            OutlinedTextField(provider, { provider = it }, label = { Text("نام ارائه‌دهنده API") }, singleLine = true)
            OutlinedTextField(key, { key = it }, label = { Text("کلید API") }, placeholder = { Text(if (configured) "برای جایگزینی، کلید جدید وارد کنید" else "کلید را وارد کنید") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            if (configured) Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(remove, { remove = it }); Text("حذف کلید ذخیره‌شده") }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        } }, confirmButton = { TextButton(enabled = !saving, onClick = { saving = true; scope.launch { try { if (remove || key.isNotBlank()) credentials.save(if (remove) "" else key.trim()); configured = credentials.configured(); vm.provider(provider); ai = false } catch (_: Exception) { error = "ذخیره امن کلید انجام نشد. دوباره تلاش کنید." }; saving = false } }) { Text("ذخیره") } }, dismissButton = { TextButton(enabled = !saving, onClick = { ai = false }) { Text("لغو") } })
    }
}
@Composable private fun SettingChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) { Text(label, modifier = Modifier.weight(1f)); RadioButton(selected, onClick) }
}
