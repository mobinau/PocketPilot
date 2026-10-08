package ir.pocketpilot
import android.app.Application
import androidx.room.Room
import ir.pocketpilot.data.local.*
import ir.pocketpilot.data.repository.*
class PocketPilotApplication : Application() {
    val calendar = AndroidFinancialCalendar
    val database by lazy { Room.databaseBuilder(this, PocketDatabase::class.java, "pocketpilot.db").addMigrations(CalendarMigrations.MIGRATION_1_2).build() }
    val repository by lazy { RoomFinanceRepository(database, calendar) }
    val preferences by lazy { PreferenceStore(this) }
    val assistant by lazy { DemoAiRepository(calendar = calendar) }
}
