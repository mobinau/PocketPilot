package ir.pocketpilot.data.repository

import ir.pocketpilot.domain.model.*
import ir.pocketpilot.domain.repository.AiAssistantRepository
import ir.pocketpilot.domain.usecase.*
import java.time.LocalDate
import kotlin.math.abs

class DemoAiRepository(private val calendar: FinancialCalendar, private val analysis: FinanceAnalysis = FinanceAnalysis(calendar), private val today: () -> LocalDate = { LocalDate.now() }) : AiAssistantRepository {
    override suspend fun answer(question: String, data: FinanceData, currency: Currency): String {
        val report = analysis.report(data, calendar.monthOf(today()), currency)
        val name = data.categories.firstOrNull { it.id == report.topCategory }?.name
        if (data.transactions.none { it.currency == currency.storage }) return "هنوز تراکنشی با این واحد پول ثبت نشده است. ابتدا درآمد و هزینه‌های خود را ثبت کنید تا بتوانم آن‌ها را تحلیل کنم."
        return when {
            question.contains("پس‌انداز") || question.contains("پس انداز") -> "این ماه ${money(report.totals.savings, currency)} پس‌انداز خالص دارید. درآمد ${money(report.totals.income, currency)} و هزینه ${money(report.totals.expense, currency)} است. نرخ پس‌انداز شما ${report.totals.savingsRate.toInt().toString().persianDigits()}٪ است."
            question.contains("مقایسه") || question.contains("چرا") || question.contains("زیاد") -> report.expenseChange?.let { "هزینه این ماه ${money(report.totals.expense, currency)} و ماه گذشته ${money(report.previous.expense, currency)} بوده است؛ ${abs(it).toInt().toString().persianDigits()}٪ ${if (it >= 0) "افزایش" else "کاهش"}. ${name?.let { n -> "بیشترین سهم هزینه مربوط به $n است." } ?: "این ماه هزینه‌ای ثبت نشده است."} برای بررسی دقیق‌تر، گزارش دسته‌بندی‌ها را ببینید." } ?: "ماه گذشته هزینه‌ای با این واحد پول ثبت نشده است؛ درصد مقایسه قابل محاسبه نیست. هزینه این ماه ${money(report.totals.expense, currency)} است."
            question.contains("کمتر") || question.contains("کاهش") -> if (name == null) "این ماه هنوز هزینه‌ای ثبت نکرده‌اید. برای شروع، هزینه‌ها را مرتب ثبت کنید و برای هر دسته سقف بودجه تعیین کنید." else "بیشترین سهم هزینه شما مربوط به $name است (${money(report.categories[report.topCategory] ?: 0, currency)}). هزینه‌های قابل‌کاهش این دسته را بررسی کنید و سقف ماهانه‌ای واقع‌بینانه تعیین کنید. ${if (report.budgets.any { it.percentage >= 80 }) "بودجه‌های نزدیک سقف را در صفحه بودجه بررسی کنید." else "ثبت منظم خریدهای کوچک، تصویر روشن‌تری از مخارج به شما می‌دهد."}"
            question.contains("بیشترین") || question.contains("چه چیزی") || question.contains("کدام") -> name?.let { "بیشترین هزینه این ماه مربوط به $it با مبلغ ${money(report.categories[report.topCategory] ?: 0, currency)} است. کل هزینه این ماه ${money(report.totals.expense, currency)} بوده است." } ?: "این ماه هزینه‌ای با این واحد پول ثبت نشده است."
            question.contains("بودجه") -> report.budgets.takeIf { it.isNotEmpty() }?.joinToString("\n") { "${data.categories.firstOrNull { c -> c.id == it.budget.categoryId }?.name}: ${it.percentage.toInt().toString().persianDigits()}٪ مصرف شده؛ ${money(it.remaining, currency)} باقی‌مانده." } ?: "برای این ماه بودجه‌ای ندارید. در صفحه بودجه‌بندی سقف هر دسته را تعیین کنید."
            else -> "می‌توانم درباره بیشترین هزینه، پس‌انداز، بودجه و مقایسه ماه‌ها پاسخ بدهم.\n${analysis.insights(data, currency, today()).take(2).joinToString("\n")}"
        }
    }
}
/** A transport is injected by a future provider integration. No simulated network request. */
fun interface AiProvider { suspend fun respond(question: String, financialContext: String): String }
class ApiAiRepository(private val provider: AiProvider, private val calendar: FinancialCalendar) : AiAssistantRepository {
    override suspend fun answer(question: String, data: FinanceData, currency: Currency): String = provider.respond(question, FinanceAnalysis(calendar).insights(data, currency).joinToString("\n"))
}
