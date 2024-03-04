/*
 * (C) 2024 Akane Foundation (https://github.com/AkaneTan/Gramophone/blob/beta/LICENSE)
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss.controllers
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.util.Locale
import theredspy15.ltecleanerfoss.databinding.ActivityErrorBinding
import theredspy15.ltecleanerfoss.R
class ErrorActivity: AppCompatActivity(){
	private lateinit var binding: ActivityErrorBinding
	override fun onCreate(savedInstanceState: Bundle?){
		super.onCreate(savedInstanceState)
		binding = ActivityErrorBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val appVersion: String = packageManager.getPackageInfo(packageName,0).versionName
		val appLang: String = Locale.getDefault().language
		val osVersion: String = (System.getProperty("os.name") ?: "Android") +
			(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) " ${Build.VERSION.BASE_OS}" else "") +
			" " + Build.VERSION.RELEASE +
			" - " + Build.VERSION.SDK_INT
		val exceptionMessage: String? = intent.getStringExtra("exception_message")
		val formattedDateTime = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
			.format(LocalDateTime.now())
		binding.error.typeface = Typeface.MONOSPACE

		val text = StringBuilder()
			.append("App language: ${appLang}\n")
			.append("App version : ${appVersion}\n")
			.append("Device      : ${Build.BRAND} ${Build.MODEL}\n")
			.append("OS Version  : ${osVersion}\n")
			.append("GMT Time    : ${formattedDateTime}\n\n")
			.append(exceptionMessage)
			.toString()
		val formattedText = StringBuilder()
			.append("## Exception\n")
			.append("* __App language:__ ${appLang}\n")
			.append("* __App version :__ ${appVersion}\n")
			.append("* __Device:__ ${Build.BRAND} ${Build.MODEL}\n")
			.append("* __OS Version  :__ ${osVersion}\n")
			.append("* __GMT Time    :__ ${formattedDateTime}\n")
			.append("<details><summary><b>Crash log </b></summary><p>\n")
			.append("```\n${exceptionMessage}\n```\n")
			.append("</details><hr>")
			.toString()

		binding.error.text = text
		binding.shareLogBtn.setOnClickListener {
			val intent = Intent(Intent.ACTION_SEND)
			intent.type = "text/plain"
			intent.putExtra(Intent.EXTRA_TEXT, text)
			startActivity(Intent.createChooser(intent,"Share with"))
		}
		binding.shareFormattedLogBtn.setOnClickListener {
			val intent = Intent(Intent.ACTION_SEND)
			intent.type = "text/plain"
			intent.putExtra(Intent.EXTRA_TEXT, formattedText)
			startActivity(Intent.createChooser(intent,"Share with"))
		}
		binding.reportIssueGithubBtn.setOnClickListener {
			startActivity(Intent(
				Intent.ACTION_VIEW,
				Uri.parse("https://github.com/mdp43140/LTECleanerFOSS/issues")
			))
		}
	}
}