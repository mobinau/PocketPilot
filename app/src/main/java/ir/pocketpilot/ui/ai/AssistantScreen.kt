package ir.pocketpilot.ui.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.pocketpilot.ui.FinanceViewModel
import ir.pocketpilot.ui.components.ScreenHeading

@Composable fun AssistantScreen(vm: FinanceViewModel, onBack: () -> Unit) {
    val chat by vm.chat.collectAsStateWithLifecycle()
    var input by rememberSaveable { mutableStateOf("") }
    val list = rememberLazyListState()
    LaunchedEffect(chat.messages.size, chat.busy) { list.animateScrollToItem((chat.messages.size - 1).coerceAtLeast(0)) }
    Column(Modifier.fillMaxSize().imePadding().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Spacer(Modifier.height(8.dp))
        ScreenHeading("دستیار مالی هوشمند", "حالت آزمایشی", onBack)
        Text("پاسخ آزمایشی بر اساس داده‌های ذخیره‌شده در دستگاه", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyColumn(state = list, modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
            items(chat.messages) { message ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.user) Arrangement.Start else Arrangement.End) {
                    Surface(modifier = Modifier.widthIn(max = 320.dp), shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = if (message.user) 4.dp else 20.dp, bottomEnd = if (message.user) 20.dp else 4.dp), color = if (message.user) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface) {
                        Column(Modifier.padding(16.dp)) { Text(if (message.user) "شما" else "پول‌یار", style = MaterialTheme.typography.labelSmall); Spacer(Modifier.height(6.dp)); Text(message.text, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
            if (chat.busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(listOf("بیشترین هزینه من چیست؟", "این ماه چقدر پس‌انداز کردم؟", "چطور کمتر خرج کنم؟")) { question -> SuggestionChip(onClick = { vm.ask(question) }, label = { Text(question) }, enabled = !chat.busy) } }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
            OutlinedTextField(input, { input = it }, placeholder = { Text("سؤال مالی خود را بنویسید...") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), maxLines = 4, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send), keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSend = { if (input.isNotBlank() && !chat.busy) { vm.ask(input); input = "" } }))
            FilledIconButton(enabled = input.isNotBlank() && !chat.busy, onClick = { vm.ask(input); input = "" }, modifier = Modifier.size(52.dp)) { Icon(Icons.AutoMirrored.Rounded.Send, "ارسال") }
        }
    }
}
