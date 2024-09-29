/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.DecimalFormat
//import theredspy15.ltecleanerfoss.Constants
import kotlin.system.exitProcess
object CommonFunctions {
	fun makeNotificationChannel(ctx: Context, name: String, description: String?, channelName: String, importance: Int){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			val channel = NotificationChannel(channelName, name, importance)
			channel.description = description
			(ctx.getSystemService(NotificationManager::class.java)).createNotificationChannel(channel)
		}
	}
	fun makeNotification(ctx: Context, channel: String): NotificationCompat.Builder {
		return NotificationCompat.Builder(ctx, channel)
			.setSmallIcon(R.drawable.ic_baseline_cleanup_24)
			.setAutoCancel(true)
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setVibrate(LongArray(0))
	}
	fun makeStatusNotification(ctx: Context, message: String?): NotificationCompat.Builder {
		// Name of Notification Channel for verbose notifications of background work
		val title: CharSequence = ctx.getString(R.string.svc_notification_title)

		// Make a channel if necessary
		makeNotificationChannel(
			ctx,
			ctx.getString(R.string.default_notification_name),
			ctx.getString(R.string.default_notification_sum),
			Constants.NOTIFICATION_CHANNEL_SERVICE,
			NotificationManager.IMPORTANCE_DEFAULT
		)

		// Create the notification
		return makeNotification(ctx, Constants.NOTIFICATION_CHANNEL_SERVICE)
			.setContentTitle(title)
			.setContentText(message)
	}
	fun sendNotification(ctx: Context, id: Int, notification: Notification){
		NotificationManagerCompat.from(ctx).notify(id, notification)
	}
	fun sendNotification(ctx: Context, id: Int, notification: NotificationCompat.Builder){
		sendNotification(ctx, id, notification.build())
	}
	fun updateTheme(ctx: Context, prefs: SharedPreferences?){
		val themeStr = ctx.resources.getStringArray(R.array.themes_key)
		AppCompatDelegate.setDefaultNightMode(when (prefs!!.getString("theme",themeStr[0])){
			themeStr[1] -> AppCompatDelegate.MODE_NIGHT_NO
			themeStr[2] -> AppCompatDelegate.MODE_NIGHT_YES
			else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
		})
	}
	fun writeContentToUri(ctx: Context,uri: Uri, content: String){
		ctx.contentResolver.openOutputStream(uri)?.use { outputStream ->
			outputStream.write(content.toByteArray())
		}
	}
	fun convertSize(length: Long): String {
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