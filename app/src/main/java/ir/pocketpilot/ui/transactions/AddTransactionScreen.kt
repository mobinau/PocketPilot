@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package ir.pocketpilot.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.pocketpilot.domain.model.*
import ir.pocketpilot.domain.usecase.*
import ir.pocketpilot.ui.*
import ir.pocketpilot.ui.components.*
import java.math.BigDecimal
import java.time.*

@Composable fun CategorySelector(categories: List<Category>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Text(categories.firstOrNull { it.id == selected }?.name ?: "انتخاب دسته‌بندی", modifier = Modifier.weight(1f)); Icon(Icons.Rounded.ExpandMore, "انتخاب دسته‌بندی")
        }
        DropdownMenu(expanded, { expanded = false }, modifier = Modifier.heightIn(max = 360.dp)) {
            categories.forEach { c -> DropdownMenuItem(text = { Text(c.name) }, leadingIcon = { Icon(categoryIcon(c.icon), null) }, onClick = { onSelect(c.id); expanded = false }) }
        }
    }
}
@Composable fun AddTransactionScreen(vm: FinanceViewModel, state: AppState, id: Long, initialType: String, onBack: () -> Unit) {
    val existing = state.data.transactions.firstOrNull { it.id == id }
    val currency = if (existing == null || existing.currency == state.preferences.currency.storage) state.preferences.currency else Currency.entries.first { it.storage == existing.currency }
    var type by rememberSaveable(id) { mutableStateOf(existing?.type ?: TransactionType.valueOf(initialType)) }
    var amount by rememberSaveable(id) { mutableStateOf(existing?.let { BigDecimal.valueOf(it.amount).divide(BigDecimal.valueOf(currency.factor)).toPlainString().persianDigits() } ?: "") }
    var category by rememberSaveable(id) { mutableStateOf(existing?.categoryId ?: "") }
    var dateEpoch by rememberSaveable(id) { mutableLongStateOf((existing?.date ?: LocalDate.now()).toEpochDay()) }
    var description by rememberSaveable(id) { mutableStateOf(existing?.description ?: "") }
    var recurring by rememberSaveable(id) { mutableStateOf(existing?.recurring ?: false) }
    var dateDialog by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val saving by vm.savingTransaction.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.imePadding()) {
        item { ScreenHeading(if (id == 0L) "افزودن تراکنش" else "ویرایش تراکنش", back = onBack) }
        item { SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) { TransactionType.entries.forEachIndexed { index, t -> SegmentedButton(selected = type == t, onClick = { type = t; category = "" }, shape = SegmentedButtonDefaults.itemShape(index, 2)) { Text(t.label) } } } }
        item { Text("مبلغ · ${currency.label}", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(8.dp)); OutlinedTextField(amount, { amount = it; error = null }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("۰") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), textStyle = MaterialTheme.typography.headlineLarge, singleLine = true, isError = error != null, supportingText = { Text("${currency.unit} · مبلغ بیشتر از صفر") }) }
        item { Text("دسته‌بندی", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(8.dp)); CategorySelector(state.data.categories.filter { it.type == type }, category) { category = it; error = null } }
        item { Text("تاریخ", style = MaterialTheme.typography.titleMedium); OutlinedButton(onClick = { dateDialog = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Icon(Icons.Rounded.CalendarMonth, null); Spacer(Modifier.width(8.dp)); Text(persianDate(LocalDate.ofEpochDay(dateEpoch))) } }
        item { OutlinedTextField(description, { description = it }, label = { Text("توضیحات") }, placeholder = { Text("مثلاً خرید مواد غذایی") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4) }
        item { Panel { Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("تراکنش تکرارشونده", style = MaterialTheme.typography.titleMedium); Text("ثبت خودکار ماهانه هنگام باز کردن برنامه", style = MaterialTheme.typography.bodySmall) }; Switch(checked = recurring, onCheckedChange = { recurring = it }, enabled = existing?.sourceId == null) } } }
        error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        item { Button(enabled = !saving, onClick = {
            error = vm.validateTransaction(amount, category, type, LocalDate.ofEpochDay(dateEpoch), currency)
            if (error == null) { focus.clearFocus(); vm.saveTransaction(id, amount, category, type, LocalDate.ofEpochDay(dateEpoch), description, recurring, currency, onBack) }
        }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("ذخیره تراکنش") }; TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("لغو") } }
    }
    if (dateDialog) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = LocalDate.ofEpochDay(dateEpoch).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), selectableDates = object : SelectableDates { override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() })
        DatePickerDialog(onDismissRequest = { dateDialog = false }, confirmButton = { TextButton(onClick = { picker.selectedDateMillis?.let { dateEpoch = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay() }; dateDialog = false }) { Text("تأیید") } }, dismissButton = { TextButton(onClick = { dateDialog = false }) { Text("لغو") } }) {
            DatePicker(state = picker, title = { Text("انتخاب تاریخ · تقویم میلادی", Modifier.padding(20.dp)) }, headline = { Text(picker.selectedDateMillis?.let { persianDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) } ?: "تاریخ را انتخاب کنید", Modifier.padding(20.dp)) }, showModeToggle = false)
        }
    }
}
