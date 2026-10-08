package ir.pocketpilot.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.pocketpilot.domain.model.*
import ir.pocketpilot.domain.usecase.*
import ir.pocketpilot.ui.*
import ir.pocketpilot.ui.components.*

@Composable fun TransactionsScreen(vm: FinanceViewModel, state: AppState, onAdd: () -> Unit, onDetail: (Long) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf<TransactionType?>(null) }
    var period by rememberSaveable { mutableStateOf<Period?>(null) }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val rows = remember(state, query, type, category, period) { vm.transactions(query, type, category, period) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { ScreenHeading("تراکنش‌ها", "${rows.size.toString().persianDigits()} تراکنش", action = { FilledTonalIconButton(onClick = onAdd) { Icon(Icons.Rounded.Add, "افزودن تراکنش") } }) }
        item { OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("جست‌وجوی تراکنش‌ها...") }, leadingIcon = { Icon(Icons.Rounded.Search, "جست‌وجو") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(selected = type == null, onClick = { type = null }, label = { Text("همه") }); TransactionType.entries.forEach { t -> FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t.label) }) } } }
        item { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { item { FilterChip(selected = period == null, onClick = { period = null }, label = { Text("همه زمان‌ها") }) }; items(Period.entries) { p -> FilterChip(selected = period == p, onClick = { period = p }, label = { Text(p.label) }) } } }
        item { Box { OutlinedButton(onClick = { expanded = true }) { Icon(Icons.Rounded.FilterList, null); Spacer(Modifier.width(8.dp)); Text(state.data.categories.firstOrNull { it.id == category }?.name ?: "همه دسته‌بندی‌ها") }; DropdownMenu(expanded, { expanded = false }) { DropdownMenuItem(text = { Text("همه دسته‌بندی‌ها") }, onClick = { category = null; expanded = false }); state.data.categories.forEach { c -> DropdownMenuItem(text = { Text(c.name) }, onClick = { category = c.id; expanded = false }) } } } }
        item { Text("جمع هزینه: ${money(vm.totals(rows).expense, state.preferences.currency)}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (rows.isEmpty()) item { EmptyState(if (state.data.transactions.isEmpty()) "هنوز تراکنشی ثبت نکرده‌اید." else "تراکنشی پیدا نشد.", "یک تراکنش اضافه کنید یا فیلترها را تغییر دهید.", "افزودن اولین تراکنش", onAdd) }
        items(rows, key = { it.id }) { t -> TransactionRow(t, state.data.categories.firstOrNull { it.id == t.categoryId }, state.preferences.currency) { onDetail(t.id) }; HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
    }
}
