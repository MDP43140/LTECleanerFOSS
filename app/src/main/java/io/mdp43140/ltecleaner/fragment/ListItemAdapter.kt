package io.mdp43140.ltecleaner.fragment
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.mdp43140.ltecleaner.App
import io.mdp43140.ltecleaner.databinding.ListItemBinding
class ListItemAdapter(
	private val ctx: Activity,
	val isWhitelist: Boolean
): RecyclerView.Adapter<ViewHolder>(){
	lateinit var list: MutableList<String>
	var onItemClick: ((String) -> Unit)? = null
	private val fullWidthLP = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
	init {
		setHasStableIds(true)
	}
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ListViewHolder(ListItemBinding.inflate(ctx.layoutInflater).apply {
			// hacky workaround because somehow the layout parameter seems somehow reverted to nothing
			root.setLayoutParams(fullWidthLP)
		})
	}
	override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
		(holder as ListViewHolder).bind(list[pos],isWhitelist)
	}
	override fun getItemCount(): Int = list.size
	override fun getItemId(pos: Int): Long {
		return list[pos].hashCode().toLong()
	}
	override fun getItemViewType(pos: Int): Int {
		return 1
	}
	inner class ListViewHolder(binding: ListItemBinding): ViewHolder(binding.root), View.OnClickListener {
		private val checkbox: CheckBox = binding.checkbox
		private val btn: Button = binding.btn
		private var txt: String = "[empty]"
		init {
			btn.setOnClickListener(this)
		}
		fun bind(path: String, isWhitelist: Boolean){
			txt = path
			checkbox.isChecked = if (isWhitelist)
				WhitelistFragment.whiteListOn.contains(path) else
				BlacklistFragment.blackListOn.contains(path)
			checkbox.setOnCheckedChangeListener { _, checked ->
				if (isWhitelist){
					WhitelistFragment.setWhitelistOn(App.prefs,path,checked)
					checkbox.isChecked = WhitelistFragment.whiteListOn.contains(path)
				} else {
					BlacklistFragment.setBlacklistOn(App.prefs,path,checked)
					checkbox.isChecked = BlacklistFragment.blackListOn.contains(path)
				}
			}
			btn.text = path
		}
		override fun onClick(view: View) {
			onItemClick?.invoke(txt)
		}
	}
	class VerticalSpaceItemDecoration(): RecyclerView.ItemDecoration(){
		override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State){
			outRect.bottom = 24
		}
	}
}
