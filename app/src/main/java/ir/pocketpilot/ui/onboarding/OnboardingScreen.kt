package ir.pocketpilot.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ir.pocketpilot.domain.usecase.persianDigits

@Composable fun OnboardingScreen(onComplete: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf("به پول‌یار خوش آمدید", "هزینه‌هایتان را بشناسید", "برای آینده برنامه‌ریزی کنید", "دستیار مالی هوشمند")
    val descriptions = listOf("مدیریت درآمد، هزینه و بودجه‌تان را ساده و هوشمند کنید.", "تراکنش‌ها را ثبت کنید و ببینید پولتان بیشتر در چه بخش‌هایی خرج می‌شود.", "برای دسته‌های مختلف بودجه تعیین کنید و میزان پیشرفت خود را ببینید.", "با کمک تحلیل‌های هوشمند، الگوی هزینه‌هایتان را بهتر درک کنید.")
    val icons = listOf(Icons.Rounded.AccountBalanceWallet, Icons.Rounded.PieChart, Icons.Rounded.Savings, Icons.Rounded.AutoAwesome)
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding().padding(28.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("پول‌یار", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = onComplete) { Text("رد کردن") }
        }
        Spacer(Modifier.weight(1f))
        AnimatedContent(page, label = "معرفی") { index ->
            Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
                Box(Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(40.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface) { Icon(icons[index], null, Modifier.padding(32.dp).size(80.dp), tint = MaterialTheme.colorScheme.primary) }
                    Text("همراه مالی شما", modifier = Modifier.align(Alignment.BottomCenter).padding(22.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Text(titles[index], style = MaterialTheme.typography.headlineLarge)
                Text(descriptions[index], style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(4) { Box(Modifier.size(if (page == it) 24.dp else 8.dp, 8.dp).clip(CircleShape).background(if (page == it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)) }
            Spacer(Modifier.weight(1f)); Text("${(page + 1).toString().persianDigits()} از ۴", style = MaterialTheme.typography.labelMedium)
        }
        Button(onClick = { if (page == 3) onComplete() else page++ }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp)) { Text(if (page == 3) "شروع کنیم" else "بعدی") }
        Text("اطلاعات مالی شما در دستگاه ذخیره می‌شود.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
