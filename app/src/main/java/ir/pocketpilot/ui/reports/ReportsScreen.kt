package ir.pocketpilot.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.pocketpilot.domain.usecase.*
import ir.pocketpilot.ui.*
import ir.pocketpilot.ui.components.*
import ir.pocketpilot.ui.budgets.MonthSelector
import ir.pocketpilot.domain.model.FinancialMonth
import kotlin.math.abs

@Composable fun ReportsScreen(vm: FinanceViewModel, state: AppState, onAnalytics: () -> Unit, onAi: () -> Unit) {
    var month by remember { mutableStateOf(vm.calendar.currentMonth()) }
    val report = remember(state, month) { vm.report(month) }
    val currency = state.preferences.currency
    val names = state.data.categories.associate { it.id to it.name }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { ScreenHeading("گزارش مالی ماهانه", "پیشرفتتان را دنبال کنید") }
        item { MonthSelector(month) { month = it } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { FilledTonalButton(onClick = onAnalytics, modifier = Modifier.weight(1f)) { Icon(Icons.Rounded.BarChart, null); Spacer(Modifier.width(6.dp)); Text("تحلیل مالی") }; OutlinedButton(onClick = onAi, modifier = Modifier.weight(1f)) { Text("دستیار هوشمند") } } }
        if (state.data.transactions.none { vm.calendar.contains(month, it.date) && it.currency == currency.storage }) item { EmptyState("این ماه تراکنشی ثبت نشده است.", "ماه دیگری را انتخاب کنید یا تراکنشی ثبت کنید.") }
        else {
            item { Panel { Metric("کل درآمد", money(report.totals.income, currency)); Metric("کل هزینه", money(report.totals.expense, currency)); HorizontalDivider(); Metric("میزان پس‌انداز", money(report.totals.savings, currency)); Text("نرخ پس‌انداز: ${report.totals.savingsRate.toInt().toString().persianDigits()}٪", color = MaterialTheme.colorScheme.primary) } }
            item { SectionHeading("خلاصه این ماه") }
            item { Panel {
                Text(report.expenseChange?.let { "هزینه‌های شما در این ماه نسبت به ماه گذشته ${abs(it).toInt().toString().persianDigits()}٪ ${if (it >= 0) "افزایش" else "کاهش"} داشته است." } ?: "ماه گذشته هزینه‌ای ثبت نشده است؛ درصد مقایسه قابل محاسبه نیست.")
                report.topCategory?.let { Text("بیشترین هزینه شما مربوط به ${names[it]} بوده است.") }
                Text("نرخ پس‌انداز شما در این ماه ${report.totals.savingsRate.toInt().toString().persianDigits()}٪ بوده است.")
            } }
            item { Panel {
                Metric("بیشترین دسته هزینه", report.topCategory?.let { names[it] }.orEmpty().ifBlank { "هزینه‌ای ثبت نشده است" })
                Metric("بزرگ‌ترین تراکنش هزینه", report.largest?.let { "${it.description.ifBlank { names[it.categoryId].orEmpty() }} · ${money(it.amount, currency)}" } ?: "هزینه‌ای ثبت نشده است")
            } }
            item { SectionHeading("عملکرد بودجه") }
            item { Panel { if (report.budgets.isEmpty()) Text("در این ماه بودجه‌ای تعیین نکرده‌اید.") else report.budgets.forEach { Metric(names[it.budget.categoryId].orEmpty(), "${it.percentage.toInt().toString().persianDigits()}٪ مصرف شده"); LinearProgressIndicator(progress = { (it.percentage.toFloat() / 100).coerceIn(0f,1f) }, modifier = Modifier.fillMaxWidth(), color = if (it.percentage >= 100) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) } } }
            item { SectionHeading("مقایسه با ماه گذشته") }
            item { Panel { Metric("هزینه ماه گذشته", money(report.previous.expense, currency)); Metric("هزینه این ماه", money(report.totals.expense, currency)); Metric("تغییر پس‌انداز", money(report.totals.savings - report.previous.savings, currency)) } }
        }
        item { SectionHeading("بینش‌های هوشمند · این ماه") }
        item { Panel { state.insights.forEach { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary); Text(it, style = MaterialTheme.typography.bodyMedium) } } } }
    }
}

