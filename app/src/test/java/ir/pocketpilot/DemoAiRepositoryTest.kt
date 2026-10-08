package ir.pocketpilot

import ir.pocketpilot.data.repository.*
import ir.pocketpilot.domain.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DemoAiRepositoryTest {
    private val today = LocalDate.of(2026,10,5)
    private val ai = DemoAiRepository(calendar = IcuFinancialCalendar, today = { today })
    private val data = FinanceData(listOf(
        Transaction(amount = 1000, categoryId = "salary", type = TransactionType.INCOME, date = today, description = "حقوق"),
        Transaction(amount = 700, categoryId = "food", type = TransactionType.EXPENSE, date = today, description = "خوراک"),
        Transaction(amount = 500, categoryId = "food", type = TransactionType.EXPENSE, date = IcuFinancialCalendar.atDay(IcuFinancialCalendar.monthOf(today).minusMonths(1), 5), description = "خوراک قبلی")
    ), listOf(Category("food", "خوراک و رستوران", TransactionType.EXPENSE, "food")))
    @Test fun savingsResponseUsesLocalTotals() = runBlocking { val response = ai.answer("این ماه چقدر پس‌انداز کردم؟", data, Currency.IRR); assertTrue(response.contains("۳۰۰ ریال")); assertTrue(response.contains("۳۰٪")) }
    @Test fun topCategoryUsesLocalData() = runBlocking { val response = ai.answer("بیشترین هزینه من چیست؟", data, Currency.IRR); assertTrue(response.contains("خوراک و رستوران")); assertTrue(response.contains("۷۰۰ ریال")) }
    @Test fun comparisonCalculatesChange() = runBlocking { assertTrue(ai.answer("هزینه این ماه را با ماه قبل مقایسه کن", data, Currency.IRR).contains("۴۰٪ افزایش")) }
    @Test fun reductionResponseIsSpecific() = runBlocking { assertTrue(ai.answer("چطور کمتر خرج کنم؟", data, Currency.IRR).contains("خوراک و رستوران")) }
    @Test fun emptyDataDoesNotInventAmounts() = runBlocking { assertTrue(ai.answer("پس‌انداز", FinanceData(), Currency.IRR).contains("هنوز تراکنشی")) }
    @Test fun currencyDoesNotMixLedgers() = runBlocking { assertTrue(ai.answer("بیشترین هزینه", data, Currency.USD).contains("هنوز تراکنشی")) }
    @Test fun apiAdapterUsesOnlyInjectedProvider() = runBlocking { var called = false; val api = ApiAiRepository(provider = AiProvider { q, context -> called = q == "سؤال" && context.isNotBlank(); "پاسخ" }, calendar = IcuFinancialCalendar); assertEquals("پاسخ", api.answer("سؤال", data, Currency.IRR)); assertTrue(called) }
}

