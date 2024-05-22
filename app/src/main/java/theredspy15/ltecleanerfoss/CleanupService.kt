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
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import io.mdp43140.ael.ErrorLogger
import java.util.Locale
//import theredspy15.ltecleanerfoss.CommonFunctions
//import theredspy15.ltecleanerfoss.Constants
class CleanupService: Service(){
	private lateinit var notification: NotificationCompat.Builder
	override fun onBind(intent: Intent?): IBinder? {
		return null
	}
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		CommonFunctions.makeNotificationChannel(
			applicationContext,
			applicationContext.getString(R.string.default_notification_name),
			applicationContext.getString(R.string.default_notification_sum),
			Constants.NOTIFICATION_CHANNEL_SERVICE,
			NotificationManager.IMPORTANCE_DEFAULT
		)
		try {
			Thread {
				notification = CommonFunctions.makeNotification(applicationContext, Constants.NOTIFICATION_CHANNEL_SERVICE)
					.setContentTitle(applicationContext.getString(R.string.svc_notification_title))
					.setOngoing(true)
					.setOnlyAlertOnce(true)

				val path = Environment.getExternalStorageDirectory()

				// scanner setup
				val fs = FileScanner(path,applicationContext)
				fs.setFilters(
					App.prefs!!.getBoolean("generic",true),
					App.prefs!!.getBoolean("apk",false)
				)
				fs.delete = true
				fs.updateProgress = ::updatePercentage

				// kilobytes found/freed text
				val kilobytesTotal = fs.start()
				val title =
					getString(R.string.freed) +
					" " +
					CommonFunctions.convertSize(kilobytesTotal)
				stopForeground(STOP_FOREGROUND_REMOVE)
				CommonFunctions.sendNotification(
					applicationContext,
					Constants.NOTIFICATION_ID_SERVICE,
					notification.setContentText(title).setProgress(0,0,false).setOnlyAlertOnce(false).setOngoing(false)
				)
				stopSelf()
			}.start()
		} catch (e: Exception){
			stopForeground(STOP_FOREGROUND_REMOVE)
			ErrorLogger.instance?.handleError(e)
			stopSelf()
			throw e
		}
		return START_NOT_STICKY
	}
	private fun updatePercentage(ctx: Context, percent: Double){
		notification.setProgress(100,percent.toInt(),false)
			.setContentText(String.format(Locale.US,"%s %.0f%%",ctx.getString(R.string.status_running),percent))
		startForeground(Constants.NOTIFICATION_ID_SERVICE, notification.build())
	}
}