package ir.pocketpilot.data.local

import ir.pocketpilot.domain.model.*
import ir.pocketpilot.data.mapper.*
import java.time.LocalDate

object DemoData {
    val categories = listOf(
        Category("food", "خوراک و رستوران", TransactionType.EXPENSE, "food"),
        Category("transport", "حمل‌ونقل", TransactionType.EXPENSE, "transport"),
        Category("shopping", "خرید", TransactionType.EXPENSE, "shopping"),
        Category("bills", "قبوض", TransactionType.EXPENSE, "bills"),
        Category("fun", "تفریح", TransactionType.EXPENSE, "fun"),
        Category("health", "سلامت", TransactionType.EXPENSE, "health"),
        Category("education", "آموزش", TransactionType.EXPENSE, "education"),
        Category("travel", "سفر", TransactionType.EXPENSE, "travel"),
        Category("housing", "مسکن", TransactionType.EXPENSE, "housing"),
        Category("clothing", "پوشاک", TransactionType.EXPENSE, "clothing"),
        Category("car", "خودرو", TransactionType.EXPENSE, "car"),
        Category("otherExpense", "سایر هزینه‌ها", TransactionType.EXPENSE, "other"),
        Category("salary", "حقوق", TransactionType.INCOME, "salary"),
        Category("freelance", "فریلنسری", TransactionType.INCOME, "work"),
        Category("business", "کسب‌وکار", TransactionType.INCOME, "work"),
        Category("investment", "سرمایه‌گذاری", TransactionType.INCOME, "investment"),
        Category("gift", "هدیه", TransactionType.INCOME, "gift"),
        Category("otherIncome", "سایر درآمدها", TransactionType.INCOME, "other")
    )
    fun transactions(today: LocalDate = LocalDate.now(), calendar: FinancialCalendar = AndroidFinancialCalendar): List<Transaction> {
        val month = calendar.monthOf(today)
        fun row(amount: Long, category: String, title: String, day: Int, previous: Boolean = false): Transaction {
            val target = if (previous) month.minusMonths(1) else month
            val date = calendar.atDay(target, day.coerceAtMost(if (previous) calendar.lengthOfMonth(target) else calendar.dayOfMonth(today)))
            return Transaction(amount = amount, categoryId = category, type = categories.first { it.id == category }.type, date = date, description = title)
        }
        return listOf(
            row(280_000_000, "salary", "حقوق ماهانه", 1), row(45_000_000, "freelance", "دریافت درآمد فریلنسری", 3),
            row(110_000_000, "housing", "پرداخت اجاره", 1), row(12_500_000, "food", "خرید مواد غذایی", 2),
            row(2_400_000, "bills", "قبض اینترنت", 2), row(7_800_000, "food", "رستوران", 4),
            row(1_600_000, "transport", "هزینه تاکسی", 5), row(14_000_000, "clothing", "خرید لباس", 3),
            row(16_000_000, "shopping", "خرید لوازم خانه", 4), row(4_000_000, "health", "دارو و ویزیت", 2),
            row(270_000_000, "salary", "حقوق ماه گذشته", 1, true), row(100_000_000, "housing", "اجاره ماه گذشته", 2, true),
            row(17_000_000, "food", "مواد غذایی ماه گذشته", 8, true), row(3_800_000, "transport", "رفت‌وآمد ماه گذشته", 12, true),
            row(12_000_000, "shopping", "خرید ماه گذشته", 18, true), row(6_000_000, "fun", "سینما و تفریح", 24, true)
        )
    }
    fun budgets(today: LocalDate = LocalDate.now(), calendar: FinancialCalendar = AndroidFinancialCalendar) = listOf(Budget(categoryId = "food", limit = 25_000_000, month = calendar.monthOf(today)), Budget(categoryId = "shopping", limit = 20_000_000, month = calendar.monthOf(today)), Budget(categoryId = "transport", limit = 8_000_000, month = calendar.monthOf(today)))
}
