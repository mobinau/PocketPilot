package ir.pocketpilot.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import ir.pocketpilot.domain.model.Currency
import ir.pocketpilot.domain.usecase.*
import ir.pocketpilot.ui.components.*
import java.time.LocalDate
import kotlin.math.abs

@Composable fun CategoryChart(spending: Map<String, Long>, names: Map<String, String>, currency: Currency) {
    val colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF528CB5), Color(0xFFB58B42), Color(0xFFB56C8A), Color(0xFF6878AD), Color(0xFF82965C))
    val total = spending.values.sum()
    if (total == 0L) { Text("هنوز هزینه‌ای در این بازه ثبت نشده است."); return }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Canvas(Modifier.size(150.dp).semantics { contentDescription = "نمودار سهم دسته‌ها؛ جزئیات در فهرست زیر" }) {
            var angle = -90f
            spending.values.forEachIndexed { index, amount -> val sweep = amount.toFloat() / total * 360; drawArc(colors[index % colors.size], angle, (sweep - 2f).coerceAtLeast(.1f), false, style = Stroke(22.dp.toPx())); angle += sweep }
        }
    }
    Spacer(Modifier.height(12.dp))
    spending.entries.forEachIndexed { index, entry ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text("●", color = colors[index % colors.size]); Text(names[entry.key].orEmpty(), modifier = Modifier.weight(1f)); Text("${(entry.value.toDouble() / total * 100).toInt().toString().persianDigits()}٪", style = MaterialTheme.typography.labelLarge) }
        Text(money(entry.value, currency), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable fun TrendChart(values: List<Pair<LocalDate, Long>>, currency: Currency, title: String) {
    val line = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outline.copy(alpha = .2f)
    val max = (values.maxOfOrNull { abs(it.second) } ?: 0).coerceAtLeast(1).toFloat()
    if (values.isEmpty()) { Text("برای نمایش روند، تراکنشی ثبت کنید."); return }
    Text("بیشترین مقدار: ${money(values.maxOf { it.second }, currency)}", style = MaterialTheme.typography.bodySmall)
    Canvas(Modifier.fillMaxWidth().height(130.dp).semantics { contentDescription = "$title؛ ${values.joinToString { "${persianDate(it.first)}: ${money(it.second, currency)}" }}" }) {
        val zero = size.height / 2
        drawLine(grid, Offset(0f, zero), Offset(size.width, zero), 1.dp.toPx())
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = if (values.size == 1) size.width / 2 else i.toFloat() / (values.size - 1) * size.width
            val y = zero - v.second / max * (zero - 8.dp.toPx())
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(line, 4.dp.toPx(), Offset(x,y))
        }
        drawPath(path, line, style = Stroke(3.dp.toPx()))
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row { Text(persianDate(values.first().first), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f)); Text(persianDate(values.last().first), style = MaterialTheme.typography.labelSmall) }
    }
    Text("زمان در نمودار از چپ به راست پیش می‌رود؛ خط میانی صفر است.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
