/*
 * SPDX-FileCopyrightText: 2020-2023 Hunter J Drum
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
		binding.addBtn.setOnClickListener {
			val inputEditText = EditText(this)
			MaterialAlertDialogBuilder(this)
				.setTitle("Add filter")
				.setMessage("You can use Kotlin regular expression, such as \".*\"")
				.setView(inputEditText)
				.setPositiveButton("OK"){ dialog:DialogInterface, _:Int ->
					val userInput = inputEditText.text.toString().replace("^/sdcard/", "/storage/emulated/0")
					if (userInput != "") addBlackList(App.prefs,userInput)
					dialog.dismiss()
					loadViews()
				}
				.setNegativeButton(getString(android.R.string.cancel)) { dialog:DialogInterface, _:Int ->
					dialog.dismiss()
				}
				.show()
		}
		getBlackList(App.prefs)
		getBlacklistOn(App.prefs)
		loadViews()
	}
	override fun onBackPressed(){
		// suggested fix by LeakCanary
		super.onBackPressed()
		finishAfterTransition()
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
			textView.apply {
				text = getString(R.string.empty_blacklist)
				textAlignment = View.TEXT_ALIGNMENT_CENTER
				textSize = 18f
			}
			runOnUiThread { binding.pathsLayout.addView(textView, layout) }
		} else {
			for (path in blackList) {
				val horizontalLayout = LinearLayout(this)
				val checkBox = CheckBox(this)
				val button = Button(this)
				button.apply {
					text = path
					textSize = 18f
					isAllCaps = false
					setPadding(0,0,0,0)
					background = null
					layoutParams = LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT
					)
					setOnClickListener { removeOrEditPattern(path) }
				}
				checkBox.apply {
					isChecked = blackListOn.contains(path);
					setOnCheckedChangeListener { _, checked ->
						setBlacklistOn(App.prefs,path,checked)
					}
				}
				horizontalLayout.apply {
					setBackgroundResource(R.drawable.rounded_view)
					orientation = LinearLayout.HORIZONTAL
					setPadding(12,12,12,12)
					addView(checkBox)
					addView(button)
				}
				runOnUiThread { binding.pathsLayout.addView(horizontalLayout, layout) }
			}
		}
	}
	private fun removeOrEditPattern(path: String?) {
		val inputEditText = EditText(this)
		inputEditText.setText(path!!)
		MaterialAlertDialogBuilder(this)
			.setTitle("Edit or remove filter")
			.setMessage("You can use Kotlin regular expression, such as \".*\"")
			.setView(inputEditText)
			.setPositiveButton("OK"){ dialog:DialogInterface, _:Int ->
				val userInput = inputEditText.text.toString().replace("^/sdcard/", "/storage/emulated/0")
				rmBlackList(App.prefs,path)
				if (userInput != "") addBlackList(App.prefs,userInput)
				dialog.dismiss()
				loadViews()
			}
			.setNegativeButton(getString(android.R.string.cancel)) { dialog:DialogInterface, _:Int ->
				dialog.dismiss()
			}
			.show()
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
			blackList.apply {
				add(path)
				distinct()
				sort()
			}
			blackListOn.add(path)
			prefs!!
				.edit()
				.putStringSet("blacklist", HashSet(blackList))
				.putStringSet("blacklistOn", HashSet(blackListOn))
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