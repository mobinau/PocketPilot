package ir.pocketpilot.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.pocketpilot.domain.model.*
import ir.pocketpilot.domain.usecase.*
import ir.pocketpilot.ui.*
import ir.pocketpilot.ui.components.*

@Composable fun AnalyticsScreen(vm: FinanceViewModel, state: AppState, onBack: () -> Unit) {
    var period by rememberSaveable { mutableStateOf(Period.MONTH) }
    val rows = remember(state, period) { vm.filtered(period) }
    val totals = remember(rows) { vm.totals(rows) }
    val spending = remember(rows) { vm.spending(rows) }
    val currency = state.preferences.currency
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { ScreenHeading("تحلیل مالی", "تصویر روشن‌تری از پولتان", onBack) }
        item { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(Period.entries) { p -> FilterChip(selected = period == p, onClick = { period = p }, label = { Text(p.label) }) } } }
        if (rows.isEmpty()) item { EmptyState("هنوز داده‌ای برای تحلیل ندارید.", "با ثبت تراکنش‌ها، نمودارها شکل می‌گیرند.") }
        else {
            item { SectionHeading("درآمد و هزینه") }
            item { Panel {
                Metric("درآمد", money(totals.income, currency))
                LinearProgressIndicator(progress = { (totals.income.toFloat() / maxOf(totals.income, totals.expense, 1)).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                Metric("هزینه", money(totals.expense, currency))
                LinearProgressIndicator(progress = { (totals.expense.toFloat() / maxOf(totals.income, totals.expense, 1)).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.error)
            } }
            item { SectionHeading("هزینه‌ها بر اساس دسته‌بندی") }
            item { Panel { CategoryChart(spending, state.data.categories.associate { it.id to it.name }, currency) } }
            item { SectionHeading("روند هزینه‌ها") }
            item { Panel { TrendChart(vm.dailyTrend(rows), currency, "روند هزینه‌ها") } }
            item { SectionHeading("روند پس‌انداز") }
            item { Panel { TrendChart(vm.dailyTrend(rows, true), currency, "خالص پس‌انداز روزانه"); Text("پس‌انداز روزانه = درآمد روز منهای هزینه روز", style = MaterialTheme.typography.bodySmall) } }
            item { SectionHeading("بیشترین هزینه‌ها") }
            item { Panel { rows.filter { it.type == TransactionType.EXPENSE }.sortedByDescending { it.amount }.take(5).forEach { Metric(it.description.ifBlank { state.data.categories.firstOrNull { c -> c.id == it.categoryId }?.name.orEmpty() }, money(it.amount, currency)) } } }
        }
    }
}
