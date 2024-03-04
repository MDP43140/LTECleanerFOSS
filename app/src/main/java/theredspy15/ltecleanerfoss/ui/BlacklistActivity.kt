/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss.ui
import android.content.DialogInterface
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import theredspy15.ltecleanerfoss.R
import theredspy15.ltecleanerfoss.App
import theredspy15.ltecleanerfoss.Constants.blacklistDefault
import theredspy15.ltecleanerfoss.Constants.blacklistOnDefault
import theredspy15.ltecleanerfoss.databinding.ActivityBlacklistBinding
class BlacklistActivity: AppCompatActivity(){
	lateinit var binding: ActivityBlacklistBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_blacklist)
		binding = ActivityBlacklistBinding.inflate(layoutInflater)
		setContentView(binding.root)
		binding.newButton.setOnClickListener {
			val inputEditText = EditText(this)
			val alertDialog = AlertDialog.Builder(this).create()
			alertDialog.setTitle("Add filter")
			alertDialog.setMessage("You can use Kotlin regular expression, such as \".*\"")
			alertDialog.setView(inputEditText)
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK"){ dialog:DialogInterface, _:Int ->
				val userInput = inputEditText.text.toString().replace("^/sdcard/", "/storage/emulated/0")
				if (userInput != "") addBlackList(App.prefs,userInput)
				dialog.dismiss()
				loadViews()
			}
			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)) { dialog:DialogInterface, _:Int ->
				dialog.dismiss()
			}
			alertDialog.show()
		}
		getBlackList(App.prefs)
		getBlacklistOn(App.prefs)
		loadViews()
	}
	private fun loadViews() {
		binding.pathsLayout.removeAllViews()
		val layout = LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT
		)
		layout.setMargins(0,20,0,20)
		if (blackList.isNullOrEmpty()) {
			val textView = TextView(this)
			textView.setText(R.string.empty_blacklist)
			textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
			textView.textSize = 18f
			runOnUiThread { binding.pathsLayout.addView(textView, layout) }
		} else {
			for (path in blackList) {
				val horizontalLayout = LinearLayout(this)
				val checkBox = CheckBox(this)
				val button = Button(this)
				button.text = path
				button.textSize = 18f
				button.isAllCaps = false
				button.setPadding(0,0,0,0)
				button.background = null
				button.layoutParams = LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT
				)
				button.setOnClickListener { removeOrEditPattern(path) }
				checkBox.isChecked = blackListOn.contains(path);
				checkBox.setOnCheckedChangeListener { _, checked ->
					setBlacklistOn(App.prefs,path,checked)
				}
				horizontalLayout.setBackgroundResource(R.drawable.rounded_view)
				horizontalLayout.orientation = LinearLayout.HORIZONTAL
				horizontalLayout.setPadding(12,12,12,12)
				horizontalLayout.addView(checkBox)
				horizontalLayout.addView(button)
				runOnUiThread { binding.pathsLayout.addView(horizontalLayout, layout) }
			}
		}
	}
	private fun removeOrEditPattern(path: String?) {
		val inputEditText = EditText(this)
		inputEditText.setText(path!!)
		val alertDialog = AlertDialog.Builder(this).create()
		alertDialog.setTitle("Edit or remove filter")
		alertDialog.setMessage("You can use Kotlin regular expression, such as \".*\"")
		alertDialog.setView(inputEditText)
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK"){ dialog:DialogInterface, _:Int ->
			val userInput = inputEditText.text.toString().replace("^/sdcard/", "/storage/emulated/0")
			rmBlackList(App.prefs,path)
			if (userInput != "") addBlackList(App.prefs,userInput)
			dialog.dismiss()
			loadViews()
		}
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)) { dialog:DialogInterface, _:Int ->
			dialog.dismiss()
		}
		alertDialog.show()
	}

	companion object {
		private var blackList: ArrayList<String> = ArrayList()
		private var blackListOn: ArrayList<String> = ArrayList()
		fun getBlackList(prefs: SharedPreferences?): List<String?> {
			if (blackList.isNullOrEmpty() && prefs != null) {
				// Type mismatch: inferred type is
				// (Mutable)Set<String!>? but
				// (MutableCollection<out String!>..Collection<String!>) was expected
				blackList = ArrayList(prefs.getStringSet("blacklist",blacklistDefault))
				blackList.remove("[")
				blackList.remove("]")
			}
			return blackList
		}
		fun addBlackList(prefs: SharedPreferences?, path: String) {
			if (blackList.isNullOrEmpty()) getBlackList(prefs)
			blackList.add(path)
			blackList.distinct()
			blackList.sort()
			blackListOn.add(path)
			prefs!!
				.edit()
				.putStringSet("blacklist", HashSet(blackList))
				.putStringSet("blacklistOn",HashSet(blackListOn))
				.apply()
		}
		fun rmBlackList(prefs: SharedPreferences?, path: String) {
			if (blackList.isNullOrEmpty()) getBlackList(prefs)
			blackList.remove(path)
			blackListOn.remove(path)
			prefs!!
				.edit()
				.putStringSet("blacklist", HashSet(blackList))
				.putStringSet("blacklistOn",HashSet(blackListOn))
				.apply()
		}
		fun getBlacklistOn(prefs: SharedPreferences?): List<String?> {
			if (blackListOn.isNullOrEmpty() && prefs != null) {
				val blackListOnSet = prefs.getStringSet("blacklistOn",blacklistOnDefault)
				blackListOn = ArrayList(blackListOnSet)
				for (path in blackListOnSet.orEmpty()) {
					blackListOn.add(path)
				}
			}
			return blackListOn
		}
		fun setBlacklistOn(prefs: SharedPreferences?, path: String, checked: Boolean) {
			if (blackListOn.isNullOrEmpty()) getBlacklistOn(prefs)
			if (checked) blackListOn.add(path)
			else blackListOn.remove(path)
			prefs!!
				.edit()
				.putStringSet("blacklistOn",HashSet(blackListOn))
				.apply()
		}
	}
}