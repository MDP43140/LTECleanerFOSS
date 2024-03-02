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
import theredspy15.ltecleanerfoss.App
import theredspy15.ltecleanerfoss.Constants
class BootReceiver: BroadcastReceiver() {
	override fun onReceive(ctx: Context, i: Intent) {
		val constraints = Constraints.Builder()
			.setRequiresBatteryNotLow(true)
			.setRequiresDeviceIdle(true)
			.build()

		// Schedule the work hourly
		ScheduledWorker.enqueueWork(ctx)

		// Schedule the work at boot completed
		if (App.prefs!!.getBoolean("bootedcleanup",false)){
			val myWork = OneTimeWorkRequestBuilder<ScheduledWorker>()
				.addTag(Constants.BGCLEAN_WORK_TAG)
				.setConstraints(constraints)
				.setInitialDelay(1, TimeUnit.MINUTES)
				.build()
			WorkManager.getInstance(ctx).enqueueUniqueWork(
				Constants.BGCLEAN_WORK_NAME,
				ExistingWorkPolicy.REPLACE,
				myWork
			)
		}
	}
}