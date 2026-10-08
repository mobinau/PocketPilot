package ir.pocketpilot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.pocketpilot.data.local.PreferenceStore
import ir.pocketpilot.domain.model.*
import ir.pocketpilot.domain.repository.*
import ir.pocketpilot.domain.usecase.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import ir.pocketpilot.domain.model.FinancialMonth

data class AppState(val data: FinanceData = FinanceData(), val preferences: Preferences = Preferences(), val loading: Boolean = true, val error: String? = null, val all: Totals = Totals(0,0), val report: MonthlyReport? = null, val insights: List<String> = emptyList())
data class ChatMessage(val text: String, val user: Boolean)
data class ChatState(val messages: List<ChatMessage> = listOf(ChatMessage("سلام! من دستیار پول‌یار هستم. درباره درآمد، هزینه، بودجه یا پس‌اندازتان بپرسید.", false)), val busy: Boolean = false)
class FinanceViewModel(private val repository: FinanceRepository, private val preferences: PreferenceStore, private val assistant: AiAssistantRepository, val calendar: FinancialCalendar) : ViewModel() {
    private val analysis = FinanceAnalysis(calendar)
    private val initialized = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val events = Channel<String>(Channel.BUFFERED)
    val messages = events.receiveAsFlow()
    val state = combine(repository.data, preferences.state, initialized, error) { data, prefs, ready, err ->
        AppState(data, prefs, !ready || !prefs.ready, err, analysis.totals(data.transactions.filter { it.currency == prefs.currency.storage }), analysis.report(data, calendar.currentMonth(), prefs.currency), analysis.insights(data, prefs.currency))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppState())
    private val _chat = MutableStateFlow(ChatState())
    val chat = _chat.asStateFlow()
    private val _savingTransaction = MutableStateFlow(false)
    val savingTransaction = _savingTransaction.asStateFlow()
    init { initialize() }
    fun initialize() = viewModelScope.launch { try { repository.initialize(); initialized.value = true; error.value = null } catch (_: Exception) { error.value = "بارگذاری اطلاعات انجام نشد. دوباره تلاش کنید." } }
    private fun action(success: String? = null, block: suspend () -> Unit) = viewModelScope.launch {
        try { block(); success?.let { events.send(it) } } catch (_: Exception) { events.send("ذخیره تغییرات انجام نشد. دوباره تلاش کنید.") }
    }
    fun completeOnboarding() = action { preferences.finishOnboarding() }
    fun theme(mode: ThemeMode) = action { preferences.theme(mode) }
    fun currency(currency: Currency) = action { preferences.currency(currency) }
    fun provider(provider: String) = action("نام سرویس ذخیره شد؛ دستیار همچنان در حالت آزمایشی است.") { preferences.provider(provider) }
    fun reloadDemo() = action("داده‌های نمونه بارگذاری شد.") { repository.reloadDemo() }
    fun clear() = action("تمام اطلاعات مالی حذف شد.") { repository.clear(); _chat.value = ChatState() }
    fun deleteTransaction(id: Long) = action("تراکنش حذف شد.") { repository.deleteTransaction(id) }
    fun deleteBudget(id: Long) = action("بودجه حذف شد.") { repository.deleteBudget(id) }
    fun saveTransaction(id: Long, amountText: String, categoryId: String, type: TransactionType, date: LocalDate, description: String, recurring: Boolean, currency: Currency, onSaved: () -> Unit) {
        if (_savingTransaction.value) return
        val validation = validateTransaction(amountText, categoryId, type, date, currency)
        if (validation != null) { viewModelScope.launch { events.send(validation) }; return }
        _savingTransaction.value = true
        action("تراکنش با موفقیت ذخیره شد.") {
            try {
                val existing = state.value.data.transactions.firstOrNull { it.id == id }
                repository.saveTransaction(Transaction(id, parseAmount(amountText, currency)!!, categoryId, type, date, description.trim(), recurring, currency.storage, existing?.sourceId))
                onSaved()
            } finally { _savingTransaction.value = false }
        }
    }
    fun validateTransaction(amountText: String, categoryId: String, type: TransactionType, date: LocalDate, currency: Currency): String? = when {
        amountText.isBlank() -> "لطفاً مبلغ را وارد کنید."
        parseAmount(amountText, currency) == null -> "مبلغ نامعتبر است؛ مبلغ باید بیشتر از صفر باشد."
        state.value.data.categories.none { it.id == categoryId && it.type == type } -> "لطفاً یک دسته‌بندی انتخاب کنید."
        date.isAfter(LocalDate.now()) -> "تاریخ تراکنش نمی‌تواند در آینده باشد."
        else -> null
    }
    fun saveBudget(id: Long, amount: String, category: String, month: FinancialMonth, onSaved: () -> Unit) {
        val currency = state.value.preferences.currency
        val parsed = parseAmount(amount, currency)
        if (parsed == null || state.value.data.categories.none { it.id == category && it.type == TransactionType.EXPENSE }) {
            viewModelScope.launch { events.send(if (parsed == null) "سقف بودجه باید مبلغی معتبر و بیشتر از صفر باشد." else "لطفاً دسته‌بندی را انتخاب کنید.") }; return
        }
        action("بودجه با موفقیت ذخیره شد.") { repository.saveBudget(Budget(id, category, parsed, month, currency.storage)); onSaved() }
    }
    fun transactions(query: String, type: TransactionType?, category: String?, period: Period?): List<Transaction> {
        val s = state.value
        val names = s.data.categories.associate { it.id to it.name }
        var rows = s.data.transactions.filter { it.currency == s.preferences.currency.storage && (type == null || it.type == type) && (category == null || it.categoryId == category) && (query.isBlank() || it.description.contains(query, true) || names[it.categoryId].orEmpty().contains(query, true)) }
        if (period != null) rows = analysis.period(rows, period, LocalDate.now())
        return rows
    }
    fun filtered(period: Period) = analysis.period(state.value.data.transactions.filter { it.currency == state.value.preferences.currency.storage }, period, LocalDate.now())
    fun totals(rows: List<Transaction>) = analysis.totals(rows)
    fun spending(rows: List<Transaction>) = analysis.spending(rows)
    fun report(month: FinancialMonth) = analysis.report(state.value.data, month, state.value.preferences.currency)
    fun dailyTrend(rows: List<Transaction>, income: Boolean = false): List<Pair<LocalDate, Long>> = rows.groupBy { it.date }.toSortedMap().map { (date, items) -> date to if (income) analysis.totals(items).savings else analysis.totals(items).expense }
    fun ask(question: String) {
        if (question.isBlank() || _chat.value.busy) return
        _chat.update { it.copy(messages = it.messages + ChatMessage(question.trim(), true), busy = true) }
        val snapshot = state.value
        viewModelScope.launch {
            val response = try { assistant.answer(question, snapshot.data, snapshot.preferences.currency) } catch (_: Exception) { "پاسخ آماده نشد. لطفاً دوباره تلاش کنید." }
            _chat.update { it.copy(messages = it.messages + ChatMessage(response, false), busy = false) }
        }
    }
}

