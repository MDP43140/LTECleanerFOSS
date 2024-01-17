/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
class BootReceiver: BroadcastReceiver() {
	override fun onReceive(ctxt: Context, i: Intent) {
		runCleanup(ctxt)
	}
	fun runCleanup(context: Context){
		val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
		val constraints = Constraints.Builder()
			.setRequiresBatteryNotLow(true)
			.setRequiresDeviceIdle(true)
			.build()

		// Schedule the work hourly
		ScheduledWorker.enqueueWork(context)

		// Schedule the work at boot completed
		if (prefs.getBoolean("bootedcleanup",false)){
			val myWork = OneTimeWorkRequestBuilder<ScheduledWorker>()
				.addTag(ScheduledWorker.Companion.WORK_TAG)
				.setConstraints(constraints)
				.setInitialDelay(1, TimeUnit.MINUTES)
				.build()
			WorkManager.getInstance(context).enqueueUniqueWork(
				ScheduledWorker.Companion.UNIQUE_WORK_NAME,
				ExistingWorkPolicy.REPLACE,
				myWork
			)
		}
	}
}