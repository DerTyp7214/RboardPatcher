package de.dertyp7214.rboardpatcher.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import de.dertyp7214.rboardcomponents.core.dpToPxRounded
import de.dertyp7214.rboardpatcher.R
import de.dertyp7214.rboardpatcher.api.types.PatchMeta
import de.dertyp7214.rboardpatcher.core.getAttr
import de.dertyp7214.rboardpatcher.core.preferences

class PatchAdapter(
    private val context: Context,
    private val list: List<PatchMeta>,
    private val unfiltered: List<PatchMeta>,
    private val onLongPress: (PatchMeta) -> Unit = {},
    private val onSelect: PatchAdapter.(List<PatchMeta>, PatchMeta, selected: Boolean) -> Unit
) : RecyclerView.Adapter<PatchAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private val selectedColor =
        context.getAttr(com.google.android.material.R.attr.colorSurfaceVariant)
    private val selected = HashMapWrapper(unfiltered) { _, item, selected ->
        if (isEnabled) onSelect(getSelected(), item, selected)
    }

    private val previousVisit =
        context.preferences.getLong("previousVisit", System.currentTimeMillis())

    var isEnabled = true

    override fun getItemId(position: Int): Long {
        return list[position].hashCode().toLong()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getSelected(): List<PatchMeta> {
        return try {
            selected.filter { (_, v) -> v }.map { (k, _) -> k }
        } catch (_: Exception) {
            listOf()
        }
    }

    fun select(vararg name: String, internal: Boolean = true) = select(name.toList(), internal)

    @SuppressLint("NotifyDataSetChanged")
    fun select(list: List<String>, internal: Boolean = true) {
        val tmp = selected.map { Pair(it.key.getSafeName(), it.key) }
        list.forEach { patchName ->
            tmp[patchName]?.let {
                selected.set(it, true, internal)
                if (!internal) notifyItemChanged(this@PatchAdapter.list.indexOf(it))
            }
        }
    }

    fun unselect(vararg name: String, internal: Boolean = true) = unselect(name.toList(), internal)

    @SuppressLint("NotifyDataSetChanged")
    fun unselect(list: List<String>, internal: Boolean = true) {
        val tmp = selected.map { Pair(it.key.getSafeName(), it.key) }
        list.forEach { patchName ->
            tmp[patchName]?.let {
                selected.set(it, false, internal)
                if (!internal) notifyItemChanged(this@PatchAdapter.list.indexOf(it))
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyDataChanged() {
        selected.setItems(unfiltered)
        notifyDataSetChanged()
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val root = v as MaterialCardView
        val title: TextView = v.findViewById(R.id.title)
        val author: TextView = v.findViewById(R.id.author)
        val newTag: TextView = v.findViewById(R.id.newTag)
        val image: ImageView = v.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.patch_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val patchMeta = list[position]
        when (position) {
            0 -> {
                if (list.size == 1){
                    holder.root.setBackgroundResource(R.drawable.color_surface_overlay_background_rounded)
                    val param = holder.root.layoutParams as ViewGroup.MarginLayoutParams
                    param.setMargins(16.dpToPxRounded(context), 4.dpToPxRounded(context), 16.dpToPxRounded(context), 0.dpToPxRounded(context))
                    holder.root.layoutParams = param

                }
                else{
                    holder.root.setBackgroundResource(R.drawable.color_surface_overlay_background_top)
                    val param = holder.root.layoutParams as ViewGroup.MarginLayoutParams
                    param.setMargins(16.dpToPxRounded(context), 4.dpToPxRounded(context), 16.dpToPxRounded(context), 0.dpToPxRounded(context))
                    holder.root.layoutParams = param

                }
            }
            list.lastIndex -> {
                holder.root.setBackgroundResource(R.drawable.color_surface_overlay_background_bottom)
                val param = holder.root.layoutParams as ViewGroup.MarginLayoutParams
                param.setMargins(16.dpToPxRounded(context), 0.dpToPxRounded(context), 16.dpToPxRounded(context), 4.dpToPxRounded(context))
                holder.root.layoutParams = param
            }
            else -> {
                holder.root.setBackgroundResource(R.drawable.color_surface_overlay_background)
                val param = holder.root.layoutParams as ViewGroup.MarginLayoutParams
                param.setMargins(16.dpToPxRounded(context), 0.dpToPxRounded(context), 16.dpToPxRounded(context), 0.dpToPxRounded(context))
                holder.root.layoutParams = param
            }
        }
        if (selected[patchMeta]) holder.root.setCardBackgroundColor(selectedColor)
        else holder.root.setCardBackgroundColor(Color.TRANSPARENT)

        holder.title.typeface = Typeface.create(patchMeta.font, Typeface.NORMAL)

        holder.title.text = patchMeta.name
        holder.author.text = patchMeta.author

        holder.newTag.visibility = if (patchMeta.date > previousVisit) View.VISIBLE else View.GONE

        holder.image.setImageResource(if (patchMeta.customName != null) R.drawable.ic_patch_filled else R.drawable.ic_patch)

        holder.root.setOnLongClickListener {
            onLongPress(patchMeta)
            true
        }

        holder.root.setOnClickListener {
            if (isEnabled) {
                selected[patchMeta] = !selected[patchMeta]

                if (selected[patchMeta]) {
                    when (position) {
                        0 -> {
                            if (list.size == 1) {
                                holder.root.setBackgroundResource(R.drawable.color_surface_overlay_background_rounded_colored)
                            } else {
                                holder.root.setBackgroundResource(R.drawable.color_surface_overlay_background_top_colored)
                            }
                        }

                        list.lastIndex -> {
                            holder.root.setBackgroundResource(R.drawable.color_surface_overlay_background_bottom_colored)
                        }

                        else -> {
                            holder.root.setBackgroundResource(R.drawable.color_surface_overlay_background_colored)
                        }
                    }
                }
                else
                    when (position) {
                        0 -> {
                            if (list.size == 1) {
                                holder.root.setBackgroundResource(R.drawable.color_surface_overlay_background_rounded)
                            } else {
                                holder.root.setBackgroundResource(R.drawable.color_surface_overlay_background_top)
                            }
                        }

                        list.lastIndex -> {
                            holder.root.setBackgroundResource(R.drawable.color_surface_overlay_background_bottom)
                        }

                        else -> {
                            holder.root.setBackgroundResource(R.drawable.color_surface_overlay_background)
                        }
                    }
            }
        }
    }

    override fun getItemCount() = list.size

    private class HashMapWrapper(
        private val map: HashMap<PatchMeta, Boolean>,
        private val onSet: (items: HashMap<PatchMeta, Boolean>, PatchMeta, selected: Boolean) -> Unit
    ) {
        constructor(
            map: List<PatchMeta>,
            onSet: (items: HashMap<PatchMeta, Boolean>, PatchMeta, selected: Boolean) -> Unit
        ) : this(
            HashMap(map.associateWith { false }), onSet
        )

        fun setItems(map: List<PatchMeta>) {
            val tmp = map.associateWith { this.map[it] == true }
            this.map.clear()
            this.map.putAll(tmp)
        }

        fun filter(predicate: (Map.Entry<PatchMeta, Boolean>) -> Boolean) = map.filter(predicate)
        fun <A, B> map(transform: (Map.Entry<PatchMeta, Boolean>) -> Pair<A, B>) =
            map.map(transform).toMap()

        operator fun get(index: PatchMeta) = try {
            map[index] ?: false
        } catch (_: Exception) {
            false
        }

        fun set(index: PatchMeta, e: Boolean, internal: Boolean) {
            try {
                map[index] = e
                if (!internal) onSet(map, index, e)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        operator fun set(index: PatchMeta, e: Boolean) = set(index, e, false)
    }
}