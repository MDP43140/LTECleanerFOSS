/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import theredspy15.ltecleanerfoss.Constants
import theredspy15.ltecleanerfoss.CommonFunctions.makeStatusNotification
import theredspy15.ltecleanerfoss.CommonFunctions.makeNotification
import theredspy15.ltecleanerfoss.CommonFunctions.makeNotificationChannel
import theredspy15.ltecleanerfoss.CommonFunctions.sendNotification
import theredspy15.ltecleanerfoss.CommonFunctions.convertSize
class CleanupService: Service(){
	private lateinit var notification: NotificationCompat.Builder
	override fun onBind(intent: Intent?): IBinder? {
		return null
	}
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		makeNotificationChannel(
			applicationContext,
			applicationContext.getString(R.string.default_notification_name),
			applicationContext.getString(R.string.default_notification_sum),
			Constants.NOTIFICATION_CHANNEL_SERVICE,
			NotificationManager.IMPORTANCE_DEFAULT
		)
		try {
			Thread {
				notification = makeNotification(applicationContext, Constants.NOTIFICATION_CHANNEL_SERVICE)
					.setContentTitle(applicationContext.getString(R.string.svc_notification_title))
					.setOngoing(true)
					.setOnlyAlertOnce(true)

				val path = Environment.getExternalStorageDirectory()
				val prefs = PreferenceManager.getDefaultSharedPreferences(
					applicationContext
				)

				// scanner setup
				val fs = FileScanner(path,applicationContext)
				fs.setFilters(
					prefs.getBoolean("generic",true),
					prefs.getBoolean("apk",false)
				)
				fs.delete = true
				fs.updateProgress = ::updatePercentage

				// kilobytes found/freed text
				val kilobytesTotal = fs.start()
				val title =
					getString(R.string.freed) +
					" " +
					convertSize(kilobytesTotal)
				stopForeground(STOP_FOREGROUND_REMOVE)
				sendNotification(
					applicationContext,
					Constants.NOTIFICATION_ID_SERVICE,
					notification.setContentText(title).setProgress(0,0,false).setOnlyAlertOnce(false).setOngoing(false)
				)
				stopSelf()
			}.start()
		} catch (e: Exception){
			stopForeground(STOP_FOREGROUND_REMOVE)
			sendNotification(
				applicationContext,
				Constants.NOTIFICATION_ID_SERVICE,
				notification.setContentText(e.message)
			)
			stopSelf()
			throw e
		}
		return START_NOT_STICKY
	}
	private fun updatePercentage(ctx: Context, percent: Double){
		notification.setProgress(100,percent.toInt(),false)
			.setContentText(ctx.getString(R.string.status_running))
		startForeground(Constants.NOTIFICATION_ID_SERVICE, notification.build())
	}
}