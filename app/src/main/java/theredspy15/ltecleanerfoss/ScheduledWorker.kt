/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import theredspy15.ltecleanerfoss.CommonFunctions.makeStatusNotification
import theredspy15.ltecleanerfoss.CommonFunctions.sendNotification
import theredspy15.ltecleanerfoss.CommonFunctions.convertSize
import theredspy15.ltecleanerfoss.Constants
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit
class ScheduledWorker(appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {
	override fun doWork(): Result {
		try {
			var notification = makeStatusNotification(
				applicationContext.getString(R.string.status_running),
				applicationContext
			)
			sendNotification(applicationContext, Constants.NOTIFICATION_ID_SERVICE, notification);
			val path = Environment.getExternalStorageDirectory()
			val prefs = PreferenceManager.getDefaultSharedPreferences(
				applicationContext
			)

			// scanner setup
			val fs = FileScanner(path, applicationContext)
				.setEmptyFile(prefs.getBoolean("emptyFile", false))
				.setEmptyDir(prefs.getBoolean("emptyFolder", false))
				.setAutoWhite(prefs.getBoolean("auto_white", true))
				.setDelete(true)
				.setCorpse(prefs.getBoolean("corpse", false))
				.setUpdateProgress(::updatePercentage)
				.setUpFilters(
					prefs.getBoolean("generic", true),
					prefs.getBoolean("apk", false)
				)

			// kilobytes found/freed text
			val kilobytesTotal = fs.startScan()
			val title =
				applicationContext.getString(R.string.clean_notification) + " " + convertSize(
					kilobytesTotal
				)

			notification = makeStatusNotification(title, applicationContext)
			sendNotification(applicationContext, Constants.NOTIFICATION_ID_SERVICE, notification);
			return Result.success()
		} catch (e: Exception) {
			makeStatusNotification(e.toString(), applicationContext)
			return Result.failure()
		}
	}
	private fun updatePercentage(context: Context, percent: Double){
		val notification = makeStatusNotification(context.getString(R.string.status_running), context)
		notification.setProgress(100,percent.toInt(),false).setOnlyAlertOnce(true)
		sendNotification(context, Constants.NOTIFICATION_ID_SERVICE, notification)
	}
	companion object {
		@JvmStatic
		fun enqueueWork(context: Context) {
			val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
			val dailyCleanupInterval: Long = prefs.getInt("cleanevery",0).toLong();
			val constraints = Constraints.Builder()
				.setRequiresBatteryNotLow(true)
				.setRequiresDeviceIdle(true)
				.build()
			WorkManager.getInstance(context).cancelAllWorkByTag(Constants.BGCLEAN_WORK_NAME)
			if (dailyCleanupInterval > 0){
				val myPeriodicWork = PeriodicWorkRequestBuilder<ScheduledWorker>(
						dailyCleanupInterval, TimeUnit.HOURS, // Interval
						15, TimeUnit.MINUTES // Flex interval for battery optimization
					)
					.addTag(Constants.BGCLEAN_WORK_TAG)
					.setConstraints(constraints)
					.build()
				WorkManager.getInstance(context).enqueueUniquePeriodicWork(
					Constants.BGCLEAN_WORK_NAME,
					ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
					myPeriodicWork
				)
			}
		}
	}
}