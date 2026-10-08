package ir.pocketpilot.ui.components

import android.icu.text.DateFormat
import android.icu.util.ULocale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.pocketpilot.domain.model.*
import ir.pocketpilot.domain.usecase.*
import java.time.LocalDate
import ir.pocketpilot.data.local.AndroidFinancialCalendar
import java.util.Date

fun persianDate(date: LocalDate): String = DateFormat.getDateInstance(DateFormat.MEDIUM, ULocale("fa_IR@calendar=persian")).apply { timeZone = android.icu.util.TimeZone.getTimeZone("UTC") }.format(Date(date.toEpochDay() * 86_400_000L))
fun monthLabel(date: LocalDate): String = monthLabel(AndroidFinancialCalendar.monthOf(date))
fun monthLabel(month: FinancialMonth): String = "${listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")[month.month - 1]} ${month.year.toString().persianDigits()}"
fun categoryIcon(key: String): ImageVector = when (key) {
    "food" -> Icons.Rounded.Restaurant; "transport" -> Icons.Rounded.DirectionsBus
    "shopping" -> Icons.Rounded.ShoppingBag; "bills" -> Icons.AutoMirrored.Rounded.ReceiptLong
    "fun" -> Icons.Rounded.Movie; "health" -> Icons.Rounded.FavoriteBorder
    "education" -> Icons.Rounded.School; "travel" -> Icons.Rounded.Flight
    "housing" -> Icons.Rounded.Home; "clothing" -> Icons.Rounded.Checkroom
    "car" -> Icons.Rounded.DirectionsCar; "salary" -> Icons.Rounded.AccountBalanceWallet
    "work" -> Icons.Rounded.WorkOutline; "investment" -> Icons.AutoMirrored.Rounded.TrendingUp
    "gift" -> Icons.Rounded.CardGiftcard; else -> Icons.Rounded.Payments
}
@Composable fun ScreenHeading(title: String, subtitle: String? = null, back: (() -> Unit)? = null, action: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) IconButton(onClick = back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "بازگشت") }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        action?.invoke()
    }
}
@Composable fun SectionHeading(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (action != null) TextButton(onClick = onAction) { Text(action) }
    }
}
@Composable fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
@Composable fun EmptyState(title: String, description: String, button: String? = null, onClick: () -> Unit = {}, icon: ImageVector = Icons.Rounded.AccountBalanceWallet) {
    Column(Modifier.fillMaxWidth().padding(vertical = 36.dp, horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Icon(icon, null, Modifier.padding(24.dp).size(40.dp), tint = MaterialTheme.colorScheme.primary) }
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        button?.let { Button(onClick = onClick) { Text(it) } }
    }
}
@Composable fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
@Composable fun TransactionRow(transaction: Transaction, category: Category?, currency: Currency, onClick: () -> Unit) {
    val income = transaction.type == TransactionType.INCOME
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 12.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = RoundedCornerShape(16.dp), color = if (income) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
            Icon(categoryIcon(category?.icon.orEmpty()), null, Modifier.padding(12.dp).size(22.dp), tint = if (income) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(Modifier.weight(1f)) {
            Text(transaction.description.ifBlank { category?.name ?: transaction.type.label }, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text("${category?.name.orEmpty()} · ${persianDate(transaction.date)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (transaction.recurring || transaction.sourceId != null) Text("تکرار ماهانه", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(money(transaction.amount, currency), style = MaterialTheme.typography.labelLarge, color = if (income) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Text(transaction.type.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable fun ConfirmDialog(title: String, message: String, onDismiss: () -> Unit, onConfirm: () -> Unit, confirm: String = "حذف") {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(message) }, confirmButton = { TextButton(onClick = onConfirm) { Text(confirm, color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } })
}
