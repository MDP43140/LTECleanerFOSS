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
//import theredspy15.ltecleanerfoss.BuildConfig
import theredspy15.ltecleanerfoss.databinding.ActivityErrorBinding
import theredspy15.ltecleanerfoss.R
class ErrorActivity: AppCompatActivity(){
	private lateinit var binding: ActivityErrorBinding
	override fun onCreate(savedInstanceState: Bundle?){
		super.onCreate(savedInstanceState)
		binding = ActivityErrorBinding.inflate(layoutInflater)
		setContentView(binding.root)

//	val appVersion: String = BuildConfig.VERSION_NAME
		val appLang: String = "TODO"
		val osVersion: String = (System.getProperty("os.name") ?: "Android") +
			(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) " ${Build.VERSION.BASE_OS}" else "") +
			" " + Build.VERSION.RELEASE +
			" - " + Build.VERSION.SDK_INT
		val exceptionMessage: String? = intent.getStringExtra("exception_message")
		val formattedDateTime = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
			.format(LocalDateTime.now())
		binding.error.typeface = Typeface.MONOSPACE

//		.append("App version: $appVersion").append('\n')
		val text = StringBuilder()
			.append("App language: $appLang")
			.append("\nDevice brand: ${Build.BRAND}")
			.append("\nDevice model: ${Build.MODEL}")
			.append("\nOS Version  : $osVersion")
			.append("\nGMT Time    : $formattedDateTime").append('\n').append('\n')
			.append(exceptionMessage)
			.toString()
//		.append("* __App version:__ $appVersion").append('\n')
		val formattedText = StringBuilder()
			.append("## Exception")
			.append("\n* __App language:__ $appLang")
			.append("\n* __Device brand:__ ${Build.BRAND}")
			.append("\n* __Device model:__ ${Build.MODEL}")
			.append("\n* __OS Version  :__ $osVersion")
			.append("\n* __GMT Time    :__ $formattedDateTime")
			.append("\n<details><summary><b>Crash log </b></summary><p>")
			.append("\n```\n")
			.append(exceptionMessage)
			.append("\n```")
			.append("\n</details><hr>")
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