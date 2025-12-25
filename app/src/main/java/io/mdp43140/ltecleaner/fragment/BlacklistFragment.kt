/*
 * SPDX-FileCopyrightText: 2020-2023 Hunter J Drum
 * SPDX-FileCopyrightText: 2024-2025 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.ltecleaner.fragment
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.mdp43140.ltecleaner.R
import io.mdp43140.ltecleaner.App
import io.mdp43140.ltecleaner.Constants
import io.mdp43140.ltecleaner.MainActivity
import io.mdp43140.ltecleaner.PreferenceRepository
import io.mdp43140.ltecleaner.databinding.FragmentBlacklistBinding
class BlacklistFragment: BaseFragment(){
	private lateinit var binding: FragmentBlacklistBinding
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentBlacklistBinding.inflate(inflater, container, false)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
			// Handle Edge-to-edge
			binding.root.setOnApplyWindowInsetsListener { v, windowInsets ->
				val insets = windowInsets.getInsets(
					WindowInsets.Type.systemBars() or
					WindowInsets.Type.displayCutout()
				)
				v.setPadding(
					insets.left,
					insets.top,
					insets.right,
					insets.bottom
				)
				WindowInsets.CONSUMED
			}
		}
		binding.addBtn.setOnClickListener {
			editPath(null)
		}
		binding.resetBtn.setOnClickListener {
			getBlackList(App.prefs)
			getBlacklistOn(App.prefs)
			for (i in Constants.blacklistDefault){
				if (!blackList.contains(i))
					blackList.add(i)
			}
			for (i in Constants.blacklistOnDefault){
				if (!blackListOn.contains(i))
					blackListOn.add(i)
			}
			App.prefs!!.blacklist = HashSet(blackList)
			App.prefs!!.blacklistOn = HashSet(blackListOn)
			loadViews()
		}
		getBlackList(App.prefs)
		getBlacklistOn(App.prefs)
		binding.pathsLayout.setItemViewCacheSize(1)
		val adapter = ListItemAdapter(requireActivity(), false)
		adapter.list = blackList
		adapter.onItemClick = ::editPath
		binding.pathsLayout.adapter = adapter
		binding.pathsLayout.addItemDecoration(ListItemAdapter.VerticalSpaceItemDecoration())
		binding.pathsLayout.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
		loadViews()
		return binding.root
	}
	private fun loadViews() {
		requireActivity().runOnUiThread {
			binding.pathsLayout.adapter!!.notifyDataSetChanged()
		}
	}
	private fun editPath(path: String?) {
		val inputEditText = EditText(requireContext())
		inputEditText.hint = "eg. /storage/emulated/0/.*cache"
		if (path != null) inputEditText.setText(path)
		MaterialAlertDialogBuilder(requireContext())
			.setTitle("Add/Edit/Remove filter")
			.setMessage("You can use Kotlin regular expression")
			.setView(inputEditText)
			.setPositiveButton(android.R.string.ok){ dialog:DialogInterface, _:Int ->
				val userInput = inputEditText.text.toString().replace("^/sdcard/", "/storage/emulated/0/")
				dialog.dismiss()
				if (path != null) rmBlackList(App.prefs,path)
				// check if the added pattern is dangerous (can wipe potential user data)
				if ("/storage/emulated/0/Android/data".matches(userInput.toRegex()) &&
						"/storage/emulated/0/DCIM/Camera".matches(userInput.toRegex())){
					Snackbar.make(
						(requireActivity() as MainActivity).binding.root,
						"Dangerous filter detected",
						Snackbar.LENGTH_SHORT
					).setAction("Add anyway"){ _: View ->
						addBlackList(App.prefs,userInput)
						loadViews()
					}.show()
				}
				else if (userInput != ""){
					addBlackList(App.prefs,userInput)
				}
				loadViews()
			}
			.setNegativeButton(android.R.string.cancel) { dialog:DialogInterface, _:Int ->
				dialog.dismiss()
			}
			.show()
	}

	companion object {
		var blackList: ArrayList<String> = ArrayList()
		var blackListOn: ArrayList<String> = ArrayList()
		fun getBlackList(prefs: PreferenceRepository?): List<String?> { // TODO: Should it be `List<String>?` ?
			if (blackList.isNullOrEmpty() && prefs != null) {
				blackList = ArrayList(prefs.blacklist)
				blackList.remove("[")
				blackList.remove("]")
			}
			return blackList
		}
		fun addBlackList(prefs: PreferenceRepository?, path: String) {
			if (blackList.isNullOrEmpty()) getBlackList(prefs)
			if (blackListOn.isNullOrEmpty()) getBlacklistOn(prefs)
			blackList.apply {
				add(path)
				distinct()
				sort()
			}
			blackListOn.apply {
				add(path)
				distinct()
				sort()
			}
			prefs!!.blacklist = HashSet(blackList)
			prefs!!.blacklistOn = HashSet(blackListOn)
		}
		fun rmBlackList(prefs: PreferenceRepository?, path: String) {
			if (blackList.isNullOrEmpty()) getBlackList(prefs)
			if (blackListOn.isNullOrEmpty()) getBlacklistOn(prefs)
			blackList.remove(path)
			blackListOn.remove(path)
			prefs!!.blacklist = HashSet(blackList)
			prefs!!.blacklistOn = HashSet(blackListOn)
		}
		fun getBlacklistOn(prefs: PreferenceRepository?): List<String> {
			if (blackListOn.isNullOrEmpty() && prefs != null) {
				blackListOn = ArrayList(prefs.blacklistOn)
			}
			return blackListOn
		}
		fun setBlacklistOn(prefs: PreferenceRepository?, path: String, checked: Boolean) {
			blackListOn.apply {
				if (isNullOrEmpty()) getBlacklistOn(prefs)
				if (checked){
					if (!contains(path)) add(path)
					distinct()
					sort()
				} else {
					remove(path)
				}
			}
			prefs!!.blacklistOn = HashSet(blackListOn)
		}
	}
}
