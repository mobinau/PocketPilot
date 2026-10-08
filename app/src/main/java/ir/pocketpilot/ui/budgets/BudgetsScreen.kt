package ir.pocketpilot.ui.budgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ir.pocketpilot.domain.model.*
import ir.pocketpilot.domain.usecase.*
import ir.pocketpilot.ui.*
import ir.pocketpilot.ui.components.*
import ir.pocketpilot.ui.transactions.CategorySelector
import java.math.BigDecimal
import ir.pocketpilot.domain.model.FinancialMonth

@Composable fun BudgetsScreen(vm: FinanceViewModel, state: AppState) {
    var month by remember { mutableStateOf(vm.calendar.currentMonth()) }
    var editor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Budget?>(null) }
    var delete by remember { mutableStateOf<Budget?>(null) }
    val statuses = remember(state, month) { vm.report(month).budgets }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { ScreenHeading("بودجه‌بندی", "برای پولتان برنامه داشته باشید", action = { FilledTonalIconButton(onClick = { editing = null; editor = true }) { Icon(Icons.Rounded.Add, "ایجاد بودجه") } }) }
        item { MonthSelector(month) { month = it } }
        item { Text("بودجه و هزینه‌های هر ماه بر اساس تقویم شمسی محاسبه می‌شوند.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (statuses.isEmpty()) item { EmptyState("هنوز بودجه‌ای تعیین نکرده‌اید.", "برای هر دسته، سقف هزینه ماهانه مشخص کنید.", "ایجاد بودجه", { editing = null; editor = true }, Icons.Rounded.Savings) }
        items(statuses, key = { it.budget.id }) { status ->
            val name = state.data.categories.firstOrNull { it.id == status.budget.categoryId }
            val progress by animateFloatAsState((status.percentage / 100).toFloat().coerceIn(0f, 1f), label = "پیشرفت بودجه")
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(categoryIcon(name?.icon.orEmpty()), null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(10.dp)); Text(name?.name.orEmpty(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f)); IconButton(onClick = { editing = status.budget; editor = true }) { Icon(Icons.Rounded.Edit, "ویرایش بودجه") }; IconButton(onClick = { delete = status.budget }) { Icon(Icons.Rounded.DeleteOutline, "حذف بودجه") } }
                Text("${status.percentage.toInt().toString().persianDigits()}٪ از بودجه مصرف شده", style = MaterialTheme.typography.labelLarge)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).semantics { contentDescription = "درصد مصرف بودجه ${status.percentage.toInt()}" }, color = if (status.percentage >= 100) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { Metric("هزینه‌شده", money(status.spent, state.preferences.currency), Modifier.weight(1f)); Metric(if (status.remaining < 0) "بیش از بودجه" else "باقی‌مانده", money(kotlin.math.abs(status.remaining), state.preferences.currency), Modifier.weight(1f)) }
                Text("سقف بودجه: ${money(status.budget.limit, state.preferences.currency)}", style = MaterialTheme.typography.bodySmall)
                status.warning?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = if (status.percentage >= 100) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
            }
        }
    }
    if (editor) {
        var category by remember(editing) { mutableStateOf(editing?.categoryId ?: "") }
        var amount by remember(editing) { mutableStateOf(editing?.let { BigDecimal.valueOf(it.limit).divide(BigDecimal.valueOf(state.preferences.currency.factor)).toPlainString().persianDigits() } ?: "") }
        AlertDialog(onDismissRequest = { editor = false }, title = { Text(if (editing == null) "ایجاد بودجه" else "ویرایش بودجه") }, text = { Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(monthLabel(month))
            Text("دسته‌بندی")
            CategorySelector(state.data.categories.filter { it.type == TransactionType.EXPENSE }, category) { category = it }
            OutlinedTextField(amount, { amount = it }, label = { Text("سقف بودجه ماهانه") }, suffix = { Text(state.preferences.currency.unit) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            Text("هر دسته در هر ماه یک بودجه دارد. ذخیره مجدد، سقف آن را تغییر می‌دهد.", style = MaterialTheme.typography.bodySmall)
        } }, confirmButton = { TextButton(onClick = { vm.saveBudget(editing?.id ?: 0, amount, category, month) { editor = false } }) { Text("ذخیره") } }, dismissButton = { TextButton(onClick = { editor = false }) { Text("لغو") } })
    }
    delete?.let { budget -> ConfirmDialog("حذف بودجه", "آیا از حذف این بودجه مطمئن هستید؟", { delete = null }, { vm.deleteBudget(budget.id); delete = null }) }
}
@Composable fun MonthSelector(month: FinancialMonth, onChange: (FinancialMonth) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        IconButton(onClick = { onChange(month.minusMonths(1)) }) { Icon(Icons.Rounded.ChevronRight, "ماه گذشته") }
        Text(monthLabel(month), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = { onChange(month.plusMonths(1)) }) { Icon(Icons.Rounded.ChevronLeft, "ماه بعد") }
    }
}

