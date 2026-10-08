package ir.pocketpilot.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.pocketpilot.ui.AppState
import ir.pocketpilot.ui.components.*
import ir.pocketpilot.domain.usecase.money
import java.time.LocalDate

@Composable fun DashboardScreen(state: AppState, onAdd: (String) -> Unit, onAll: () -> Unit, onDetail: (Long) -> Unit, onAi: () -> Unit) {
    val currency = state.preferences.currency
    val rows = state.data.transactions.filter { it.currency == currency.storage }.take(5)
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { ScreenHeading("سلام 👋", "امروز وضعیت مالی شما چطور است؟", action = { Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer) { Icon(Icons.Rounded.AccountBalanceWallet, "پول‌یار", Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.primary) } }) }
        item { Text(persianDate(LocalDate.now()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primary) {
                Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text("موجودی فعلی", modifier = Modifier.weight(1f)); Icon(Icons.Rounded.AccountBalanceWallet, null) }
                    Text(money(state.all.balance, currency), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .25f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f)) { Text("کل درآمد", style = MaterialTheme.typography.bodySmall); Text(money(state.all.income, currency), style = MaterialTheme.typography.labelLarge) }
                        Column(Modifier.weight(1f)) { Text("کل هزینه", style = MaterialTheme.typography.bodySmall); Text(money(state.all.expense, currency), style = MaterialTheme.typography.labelLarge) }
                    }
                    Text("بر اساس تمام تراکنش‌های ${currency.label}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = { onAdd("INCOME") }, modifier = Modifier.weight(1f).heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Rounded.Add, null, Modifier.size(20.dp)); Spacer(Modifier.width(6.dp)); Text("افزودن درآمد") }
                Button(onClick = { onAdd("EXPENSE") }, modifier = Modifier.weight(1f).heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Rounded.Add, null, Modifier.size(20.dp)); Spacer(Modifier.width(6.dp)); Text("افزودن هزینه") }
            }
        }
        item { SectionHeading("نگاه این ماه", monthLabel(LocalDate.now())) }
        item { Panel {
            val totals = state.report?.totals
            Metric("درآمد این ماه", money(totals?.income ?: 0, currency))
            HorizontalDivider()
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Metric("هزینه این ماه", money(totals?.expense ?: 0, currency), Modifier.weight(1f))
                Metric("پس‌انداز این ماه", money(totals?.savings ?: 0, currency), Modifier.weight(1f))
            }
        } }
        item { Surface(onClick = onAi, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, null)
                Column(Modifier.weight(1f)) { Text("دستیار مالی هوشمند", style = MaterialTheme.typography.titleMedium); Text(state.insights.firstOrNull() ?: "الگوی هزینه‌هایتان را بهتر بشناسید.", style = MaterialTheme.typography.bodyMedium) }
            }
        } }
        item { SectionHeading("آخرین تراکنش‌ها", "مشاهده همه", onAll) }
        if (rows.isEmpty()) item { EmptyState("هنوز تراکنشی ثبت نکرده‌اید.", "اولین قدم برای مدیریت پول، ثبت آن است.", "افزودن اولین تراکنش", { onAdd("EXPENSE") }) }
        else item { Panel { rows.forEachIndexed { index, t -> TransactionRow(t, state.data.categories.firstOrNull { it.id == t.categoryId }, currency) { onDetail(t.id) }; if (index < rows.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) } } }
    }
}
