/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import java.text.DecimalFormat
//import theredspy15.ltecleanerfoss.Constants
import theredspy15.ltecleanerfoss.ui.ErrorActivity
import kotlin.system.exitProcess
object CommonFunctions {
	fun makeNotificationChannel(ctx: Context, name: String, description: String?, channelName: String, importance: Int){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			val channel = NotificationChannel(channelName, name, importance)
			channel.description = description
			(ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
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
	fun shareFile(ctx: Context, text: String, mimeType: String){
		val intent = Intent(Intent.ACTION_SEND)
		intent.type = mimeType
		intent.putExtra(Intent.EXTRA_TEXT, text)
		ctx.startActivity(Intent.createChooser(intent,null))
	}
	fun shareFile(ctx: Context, uri: Uri, mimeType: String){
		val intent = Intent(Intent.ACTION_SEND)
		intent.type = mimeType
		intent.putExtra(Intent.EXTRA_STREAM, uri)
		ctx.startActivity(Intent.createChooser(intent,null))
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
	fun handleError(ctx: Context, severity: Int, throwable: Throwable){
		handleError(ctx,severity,Log.getStackTraceString(throwable))
	}
	fun handleError(ctx: Context, severity: Int, exception: Exception){
		handleError(ctx,severity,exception.stackTraceToString())
	}
	fun handleError(ctx: Context, severity: Int, exceptionMessage: String){
		// Severity level:
		// 1: Can be ignored, print Log.e()
		// 2: Toast + Notification that opens ErrorActivity
		// 3: Critical error: App crashed, and ErrorActivity launched (default)
		// Below level 3 (avoids crash) must wrap the code within try-catch block, or the whole app freezes

		Log.e("ErrorActivity",exceptionMessage)
		if (severity != 1){
			val intent = Intent(ctx, ErrorActivity::class.java)
			intent.putExtra("exception_message", exceptionMessage)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
			if (severity == 2){
				makeNotificationChannel(
					ctx,
					ctx.getString(R.string.errLog_notification_name),
					ctx.getString(R.string.errLog_notification_sum),
					Constants.NOTIFICATION_CHANNEL_ERROR_LOG,
					NotificationManager.IMPORTANCE_DEFAULT
				)

				sendNotification(
					ctx,
					Constants.NOTIFICATION_ID_ERROR_LOG,
					makeNotification(ctx,Constants.NOTIFICATION_CHANNEL_ERROR_LOG)
						.setContentTitle(exceptionMessage)
						.setContentIntent(PendingIntent.getActivity(ctx,0,intent,PendingIntent.FLAG_UPDATE_CURRENT))
				)
			} else {
				ctx.startActivity(intent)
				exitProcess(10)
			}
		}
	}
	fun writeContentToUri(ctx: Context,uri: Uri, content: String){
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