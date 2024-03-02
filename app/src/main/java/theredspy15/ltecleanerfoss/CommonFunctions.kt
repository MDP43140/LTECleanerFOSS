/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import theredspy15.ltecleanerfoss.Constants
import theredspy15.ltecleanerfoss.controllers.ErrorActivity
import java.text.DecimalFormat
import kotlin.system.exitProcess
object CommonFunctions {
	fun makeStatusNotification(message: String?, ctx: Context): NotificationCompat.Builder {

		// Name of Notification Channel for verbose notifications of background work
		val NOTIFICATION_TITLE: CharSequence = ctx.getString(R.string.notification_title)

		// Make a channel if necessary
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
				ctx.getString(R.string.settings_notification_name)
			val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
				ctx.getString(R.string.settings_notification_sum)
			// Create the NotificationChannel, but only on API 26+ because
			// the NotificationChannel class is new and not in the support library
			val channel =
				NotificationChannel(Constants.NOTIFICATION_CHANNEL_SERVICE, VERBOSE_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
			channel.description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION

			// Add the channel
			val notificationManager =
				ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.createNotificationChannel(channel)
		}

		// Create the notification
		val notification = NotificationCompat.Builder(ctx, Constants.NOTIFICATION_CHANNEL_SERVICE)
			.setSmallIcon(R.drawable.ic_baseline_cleaning_services_24)
			.setContentTitle(NOTIFICATION_TITLE)
			.setContentText(message)
			.setAutoCancel(true)
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setVibrate(LongArray(0))
		return notification
	}
	fun sendNotification(ctx: Context, id: Int, notification: NotificationCompat.Builder){
		NotificationManagerCompat.from(ctx).notify(id, notification.build())
	}
	fun updateTheme(prefs: SharedPreferences?){
		try {
			// currently put within try-catch
			// block cuz crash vv different value type
			val theme = prefs!!.getInt("theme",AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
			AppCompatDelegate.setDefaultNightMode(theme)
		} catch (e: Exception){}
	}
	fun updateTheme(theme: Int){
		AppCompatDelegate.setDefaultNightMode(theme)
	}
	// ANTI-UNLUCK CHUNK
	fun handleError(ctx: Context, severity: Byte?, paramThrowable: Throwable){
		// Severity level:
		// 1: Can be ignored, print Log.e()
		// 2: Toast + Notification with button (when pressed, opens ErrorActivity)
		// 3: Critical error: App crashed, and ErrorActivity launched (default)
		val exceptionMessage = Log.getStackTraceString(paramThrowable)
		Log.e("ErrorActivity",exceptionMessage)
//		if (severity != 1){
			val intent = Intent(ctx, ErrorActivity::class.java)
			intent.putExtra("exception_message", exceptionMessage)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//			if (severity == 2){
				// TODO: Toast + Notification with button
//			} else {
				ctx.startActivity(intent)
				exitProcess(10)
//			}
//		}
	}
	@JvmStatic fun writeContentToUri(ctx: Context,uri: Uri, content: String){
		ctx.contentResolver.openOutputStream(uri)?.use { outputStream ->
			outputStream.write(content.toByteArray())
		}
	}
	@JvmStatic fun convertSize(length: Long): String {
		val format = DecimalFormat("#.##")
		val kib:Long = 1024
		val mib:Long = 1048576
		return if (length > mib) {
			format.format(length / mib) + " MB"
		} else if (length > kib) {
			format.format(length / kib) + " KB"
		} else {
			format.format(length) + " B"
		}
	}
}