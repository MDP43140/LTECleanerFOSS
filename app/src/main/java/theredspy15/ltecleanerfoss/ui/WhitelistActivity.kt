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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import theredspy15.ltecleanerfoss.R
import theredspy15.ltecleanerfoss.App
import theredspy15.ltecleanerfoss.Constants.whitelistDefault
import theredspy15.ltecleanerfoss.Constants.whitelistOnDefault
import theredspy15.ltecleanerfoss.databinding.ActivityWhitelistBinding
class WhitelistActivity: AppCompatActivity(){
	lateinit var binding: ActivityWhitelistBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_whitelist)
		binding = ActivityWhitelistBinding.inflate(layoutInflater)
		setContentView(binding.root)
		binding.addBtn.setOnClickListener {
			// Creates a dialog asking for a file/folder name to add to the whitelist
			mGetContent.launch(Uri.fromFile(Environment.getDataDirectory()))
		}
		getWhiteList(App.prefs)
		getWhitelistOn(App.prefs)
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
		if (whiteList.isNullOrEmpty()) {
			val textView = TextView(this)
			textView.apply {
				text = getString(R.string.empty_whitelist)
				textAlignment = View.TEXT_ALIGNMENT_CENTER
				textSize = 18f
			}
			runOnUiThread { binding.pathsLayout.addView(textView, layout) }
		} else {
			for (path in whiteList) {
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
					setOnClickListener { removePath(path,button) }
				}
				checkBox.apply {
					isChecked = whiteListOn.contains(path);
					setOnCheckedChangeListener { _, checked ->
						setWhitelistOn(App.prefs,path,checked)
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
	private fun removePath(path: String?, button: Button?) {
		MaterialAlertDialogBuilder(this)
			.setTitle(getString(R.string.remove_from_whitelist))
			.setMessage(path!!)
			.setPositiveButton(getString(R.string.delete)){ dialogInterface:DialogInterface, _:Int ->
				rmWhiteList(App.prefs,path)
				dialogInterface.dismiss()
				binding.pathsLayout.removeView(button)
			}
			.setNegativeButton(getString(android.R.string.cancel)) { dialogInterface:DialogInterface, _:Int -> dialogInterface.dismiss() }
			.show()
	}

	/**
	 * Prepare a dialog that asks for a file/folder name to add to the whitelist
	 */
	private var mGetContent = registerForActivityResult(OpenDocumentTree()) { uri: Uri? ->
		if (uri != null){
			val path: String = uri.path!!.substring(uri.path!!.indexOf(":") + 1)
			addWhiteList(App.prefs, "/storage/emulated/0/" + path)
			loadViews()
		}
	}

	companion object {
		private var whiteList: ArrayList<String> = ArrayList()
		private var whiteListOn: ArrayList<String> = ArrayList()
		fun getWhiteList(prefs: SharedPreferences?): List<String?> {
			if (whiteList.isNullOrEmpty() && prefs != null) {
				// Type mismatch:inferred type is
				// (Mutable)Set<String!>? but
				// (MutableCollection<out String!>..Collection<String!>) was expected
				whiteList = ArrayList(prefs.getStringSet("whitelist",whitelistDefault))
				whiteList.remove("[")
				whiteList.remove("]")
			}
			return whiteList
		}
		fun addWhiteList(prefs: SharedPreferences?, path: String) {
			if (whiteList.isNullOrEmpty()) getWhiteList(prefs)
			whiteList.apply {
				add(path)
				distinct()
				sort()
			}
			whiteListOn.add(path)
			prefs!!
				.edit()
				.putStringSet("whitelist", HashSet(whiteList))
				.putStringSet("whitelistOn", HashSet(whiteListOn))
				.apply()
		}
		fun rmWhiteList(prefs: SharedPreferences?, path: String) {
			if (whiteList.isNullOrEmpty()) getWhiteList(prefs)
			whiteList.remove(path)
			whiteListOn.remove(path)
			prefs!!
				.edit()
				.putStringSet("whitelist", HashSet(whiteList))
				.putStringSet("whitelistOn", HashSet(whiteListOn))
				.apply()
		}
		fun getWhitelistOn(prefs: SharedPreferences?): List<String?> {
			if (whiteListOn.isNullOrEmpty() && prefs != null) {
				val whiteListOnSet = prefs.getStringSet("whitelistOn",whitelistOnDefault)
				whiteListOn = ArrayList(whiteListOnSet)
				for (path in whiteListOnSet.orEmpty()) {
					whiteListOn.add(path)
				}
			}
			return whiteListOn
		}
		fun setWhitelistOn(prefs: SharedPreferences?, path: String, checked: Boolean) {
			if (whiteListOn.isNullOrEmpty()) getWhitelistOn(prefs)
			if (checked) whiteListOn.add(path)
			else whiteListOn.remove(path)
			prefs!!
				.edit()
				.putStringSet("whitelistOn",HashSet(whiteListOn))
				.apply()
		}
	}
}