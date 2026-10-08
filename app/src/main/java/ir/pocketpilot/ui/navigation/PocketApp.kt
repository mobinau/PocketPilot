package ir.pocketpilot.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import ir.pocketpilot.ui.*
import ir.pocketpilot.ui.onboarding.*
import ir.pocketpilot.ui.dashboard.*
import ir.pocketpilot.ui.transactions.*
import ir.pocketpilot.ui.budgets.*
import ir.pocketpilot.ui.analytics.*
import ir.pocketpilot.ui.reports.*
import ir.pocketpilot.ui.ai.*
import ir.pocketpilot.ui.settings.*

@Composable fun PocketApp(vm: FinanceViewModel, state: AppState) {
    if (state.error != null) { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(state.error); Button(onClick = { vm.initialize() }) { Text("تلاش دوباره") } } }; return }
    if (state.loading) { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
    if (!state.preferences.onboarding) { OnboardingScreen { vm.completeOnboarding() }; return }
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val bottomRoutes = listOf("home", "transactions", "budgets", "reports", "settings")
    val labels = listOf("خانه", "تراکنش‌ها", "بودجه", "گزارش‌ها", "تنظیمات")
    val icons = listOf(Icons.Rounded.Home, Icons.AutoMirrored.Rounded.ReceiptLong, Icons.Rounded.Savings, Icons.Rounded.BarChart, Icons.Rounded.Settings)
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(vm) { vm.messages.collect { snackbar.showSnackbar(it) } }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, bottomBar = {
        if (route in bottomRoutes) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            bottomRoutes.forEachIndexed { index, destination -> NavigationBarItem(selected = route == destination, onClick = { nav.navigate(destination) { popUpTo(nav.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } }, icon = { Icon(icons[index], labels[index]) }, label = { Text(labels[index], maxLines = 1) }) }
        }
    }) { padding ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(padding).fillMaxSize()) {
            composable("home") { DashboardScreen(state, onAdd = { nav.navigate("edit/0/$it") }, onAll = { nav.navigate("transactions") }, onDetail = { nav.navigate("detail/$it") }, onAi = { nav.navigate("ai") }) }
            composable("transactions") { TransactionsScreen(vm, state, { nav.navigate("edit/0/EXPENSE") }, { nav.navigate("detail/$it") }) }
            composable("edit/{id}/{type}", arguments = listOf(navArgument("id") { type = NavType.LongType }, navArgument("type") { type = NavType.StringType })) { e -> AddTransactionScreen(vm, state, e.arguments!!.getLong("id"), e.arguments!!.getString("type")!!, { nav.popBackStack() }) }
            composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e -> TransactionDetailScreen(vm, state, e.arguments!!.getLong("id"), { nav.popBackStack() }, { id, type -> nav.navigate("edit/$id/$type") }) }
            composable("budgets") { BudgetsScreen(vm, state) }
            composable("reports") { ReportsScreen(vm, state, { nav.navigate("analytics") }, { nav.navigate("ai") }) }
            composable("analytics") { AnalyticsScreen(vm, state) { nav.popBackStack() } }
            composable("ai") { AssistantScreen(vm) { nav.popBackStack() } }
            composable("settings") { SettingsScreen(vm, state) }
        }
    }
}
