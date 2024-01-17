/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss.controllers
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import theredspy15.ltecleanerfoss.R
import theredspy15.ltecleanerfoss.databinding.ActivityWhitelistBinding
class WhitelistActivity: AppCompatActivity(){
	lateinit var binding: ActivityWhitelistBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_whitelist)
		binding = ActivityWhitelistBinding.inflate(layoutInflater)
		setContentView(binding.root)
		binding.newButton.setOnClickListener {
			// Creates a dialog asking for a file/folder name to add to the whitelist
			mGetContent.launch(Uri.fromFile(Environment.getDataDirectory()))
		}
		getWhiteList(MainActivity.prefs)
		loadViews()
	}

	private fun loadViews() {
		binding.pathsLayout.removeAllViews()
		val layout = LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT
		)
		layout.setMargins(0, 20, 0, 20)

		if (whiteList.isNullOrEmpty()) {
			val textView = TextView(this)
			textView.setText(R.string.empty_whitelist)
			textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
			textView.textSize = 18f
			runOnUiThread { binding.pathsLayout.addView(textView, layout) }
		} else {
			for (path in whiteList) {
				val button = Button(this)
				button.text = path
				button.textSize = 18f
				button.isAllCaps = false
				button.setOnClickListener { removePath(path, button) }
				button.setPadding(24, 24, 24, 24)
				button.setBackgroundResource(R.drawable.rounded_view)
				runOnUiThread { binding.pathsLayout.addView(button, layout) }
			}
		}
	}

	private fun removePath(path: String?, button: Button?) {
		val alertDialog = AlertDialog.Builder(this).create()
		alertDialog.setTitle(getString(R.string.remove_from_whitelist))
		alertDialog.setMessage(path!!)
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.delete)){ dialogInterface:DialogInterface, _:Int ->
			rmWhiteList(MainActivity.prefs,path);
			dialogInterface.dismiss()
			binding.pathsLayout.removeView(button)
		}
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)) { dialogInterface:DialogInterface, _:Int -> dialogInterface.dismiss() }
		alertDialog.show()
	}

	/**
	 * Prepare a dialog that asks for a file/folder name to add to the whitelist
	 */
	private var mGetContent = registerForActivityResult(OpenDocumentTree()) { uri: Uri? ->
		if (uri != null) {
			val path: String = uri.path!!.substring(uri.path!!.indexOf(":") + 1)
			addWhiteList(MainActivity.prefs, path)
		}
		loadViews()
	}

	companion object {
		private var whiteList: ArrayList<String> = ArrayList()
		fun getWhiteList(prefs: SharedPreferences?): List<String?> {
			if (whiteList.isNullOrEmpty() && prefs != null) {
				// Type mismatch:inferred type is
				// (Mutable)Set<String!>? but
				// (MutableCollection<out String!>..Collection<String!>) was expected
				whiteList = ArrayList(prefs.getStringSet("whitelist", emptySet()))
				whiteList.remove("[")
				whiteList.remove("]")
			}
			return whiteList
		}
		fun addWhiteList(prefs: SharedPreferences?, path: String) {
			// TODO: check for duplicates first before adding it
			if (whiteList.isNullOrEmpty()) getWhiteList(prefs)
			whiteList.add(path)
			prefs!!
				.edit()
				.putStringSet("whitelist", HashSet(whiteList))
				.apply()
		}
		fun rmWhiteList(prefs: SharedPreferences?, path: String) {
			if (whiteList.isNullOrEmpty()) getWhiteList(prefs)
			whiteList.remove(path)
			prefs!!
				.edit()
				.putStringSet("whitelist", HashSet(whiteList))
				.apply()
		}
	}
}