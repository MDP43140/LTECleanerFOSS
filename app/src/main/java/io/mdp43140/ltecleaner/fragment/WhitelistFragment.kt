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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
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
import io.mdp43140.ltecleaner.databinding.FragmentWhitelistBinding
class WhitelistFragment: BaseFragment(){
	private lateinit var binding: FragmentWhitelistBinding
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentWhitelistBinding.inflate(inflater, container, false)
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
			// Creates a dialog asking for a file/folder name to add to the whitelist
			mGetContent.launch(Uri.fromFile(Environment.getDataDirectory()))
		}
		binding.resetBtn.setOnClickListener {
			getWhiteList(App.prefs)
			getWhitelistOn(App.prefs)
			for (i in Constants.whitelistDefault){
				if (!whiteList.contains(i))
					whiteList.add(i)
			}
			for (i in Constants.whitelistOnDefault){
				if (!whiteListOn.contains(i))
					whiteListOn.add(i)
			}
			App.prefs!!.whitelist = HashSet(whiteList)
			App.prefs!!.whitelistOn = HashSet(whiteListOn)
			loadViews()
		}
		getWhiteList(App.prefs)
		getWhitelistOn(App.prefs)
		binding.pathsLayout.setItemViewCacheSize(1)
		val adapter = ListItemAdapter(requireActivity(), false)
		adapter.list = whiteList
		adapter.onItemClick = ::removePath
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
	private fun removePath(path: String) {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.remove_from_whitelist)
			.setMessage(path)
			.setPositiveButton(R.string.delete){ dialog:DialogInterface, _:Int ->
				rmWhiteList(App.prefs,path)
				dialog.dismiss()
				loadViews()
			}
			.setNegativeButton(android.R.string.cancel) { dialog:DialogInterface, _:Int ->
				dialog.dismiss()
			}
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
		var whiteList: ArrayList<String> = ArrayList()
		var whiteListOn: ArrayList<String> = ArrayList()
		fun getWhiteList(prefs: PreferenceRepository?): List<String> {
			if (whiteList.isNullOrEmpty() && prefs != null) {
				whiteList = ArrayList(prefs.whitelist)
				whiteList.remove("[")
				whiteList.remove("]")
			}
			return whiteList
		}
		fun addWhiteList(prefs: PreferenceRepository?, path: String) {
			if (whiteList.isNullOrEmpty()) getWhiteList(prefs)
			if (whiteListOn.isNullOrEmpty()) getWhitelistOn(prefs)
			whiteList.apply {
				add(path)
				distinct()
				sort()
			}
			whiteListOn.apply {
				add(path)
				distinct()
				sort()
			}
			prefs!!.whitelist = HashSet(whiteList)
			prefs!!.whitelistOn = HashSet(whiteListOn)
		}
		fun rmWhiteList(prefs: PreferenceRepository?, path: String) {
			if (whiteList.isNullOrEmpty()) getWhiteList(prefs)
			if (whiteListOn.isNullOrEmpty()) getWhitelistOn(prefs)
			whiteList.remove(path)
			whiteListOn.remove(path)
			prefs!!.whitelist = HashSet(whiteList)
			prefs!!.whitelistOn = HashSet(whiteListOn)
		}
		fun getWhitelistOn(prefs: PreferenceRepository?): List<String> {
			if (whiteListOn.isNullOrEmpty() && prefs != null) {
				whiteListOn = ArrayList(prefs.whitelistOn)
			}
			return whiteListOn
		}
		fun setWhitelistOn(prefs: PreferenceRepository?, path: String, checked: Boolean) {
			whiteListOn.apply {
				if (isNullOrEmpty()) getWhitelistOn(prefs)
				if (checked){
					if (!contains(path)) add(path)
					distinct()
					sort()
				} else {
					remove(path)
				}
			}
			prefs!!.whitelistOn = HashSet(whiteListOn)
		}
	}
}
