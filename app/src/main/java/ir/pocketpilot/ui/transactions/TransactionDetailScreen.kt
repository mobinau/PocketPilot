package ir.pocketpilot.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.pocketpilot.domain.usecase.money
import ir.pocketpilot.ui.*
import ir.pocketpilot.ui.components.*

@Composable fun TransactionDetailScreen(vm: FinanceViewModel, state: AppState, id: Long, onBack: () -> Unit, onEdit: (Long, String) -> Unit) {
    val transaction = state.data.transactions.firstOrNull { it.id == id }
    var confirm by remember { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { ScreenHeading("جزئیات تراکنش", back = onBack) }
        if (transaction == null) item { EmptyState("تراکنش در دسترس نیست.", "این تراکنش حذف شده است.", "بازگشت", onBack) }
        else {
            item { Panel { Text(transaction.type.label, color = MaterialTheme.colorScheme.primary); Text(money(transaction.amount, state.preferences.currency), style = MaterialTheme.typography.headlineLarge); Metric("دسته‌بندی", state.data.categories.firstOrNull { it.id == transaction.categoryId }?.name.orEmpty()); Metric("تاریخ", persianDate(transaction.date)); Metric("توضیحات", transaction.description.ifBlank { "بدون توضیحات" }); Metric("تکرار", if (transaction.recurring) "ماهانه" else if (transaction.sourceId != null) "ثبت‌شده از تکرار ماهانه" else "بدون تکرار") } }
            item { Button(onClick = { onEdit(id, transaction.type.name) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("ویرایش") }; TextButton(onClick = { confirm = true }, modifier = Modifier.fillMaxWidth()) { Text("حذف", color = MaterialTheme.colorScheme.error) } }
        }
    }
    if (confirm) ConfirmDialog("حذف تراکنش", "آیا از حذف این تراکنش مطمئن هستید؟${if (transaction?.recurring == true) " تکرارهای ثبت‌شده این تراکنش نیز حذف می‌شوند." else ""}", { confirm = false }, { vm.deleteTransaction(id); confirm = false; onBack() })
}
