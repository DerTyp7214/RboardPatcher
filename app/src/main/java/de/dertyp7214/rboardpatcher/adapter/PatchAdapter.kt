package de.dertyp7214.rboardpatcher.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import de.dertyp7214.colorutilsc.ColorUtilsC
import de.dertyp7214.rboardpatcher.R
import de.dertyp7214.rboardpatcher.api.types.PatchMeta
import de.dertyp7214.rboardpatcher.core.getAttr
import de.dertyp7214.rboardpatcher.core.preferences

class PatchAdapter(
    private val context: Context,
    private val list: List<PatchMeta>,
    private val unfiltered: List<PatchMeta>,
    private val onLongPress: (PatchMeta) -> Unit = {},
    private val onSelect: (List<PatchMeta>) -> Unit
) : RecyclerView.Adapter<PatchAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private val selectedColor = ColorUtilsC.setAlphaComponent(
        context.getAttr(com.google.android.material.R.attr.colorOnSecondary),
        40
    )
    private val selected = HashMapWrapper(unfiltered) {
        if (isEnabled) onSelect(getSelected())
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

    fun select(list: List<String>) {
        val tmp = selected.map { Pair(it.key.getSafeName(), it.key) }
        list.forEach { patchName ->
            tmp[patchName]?.let { selected.set(it, true, internal = true) }
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.patch_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val patchMeta = list[position]

        if (selected[patchMeta]) holder.root.setCardBackgroundColor(selectedColor)
        else holder.root.setCardBackgroundColor(Color.TRANSPARENT)

        holder.title.typeface = Typeface.create(patchMeta.font, Typeface.NORMAL)

        holder.title.text = patchMeta.name
        holder.author.text = patchMeta.author

        holder.newTag.visibility = if (patchMeta.date > previousVisit) View.VISIBLE else View.GONE

        holder.root.setOnLongClickListener {
            onLongPress(patchMeta)
            true
        }

        holder.root.setOnClickListener {
            if (isEnabled) {
                selected[patchMeta] = !selected[patchMeta]

                if (selected[patchMeta]) holder.root.setCardBackgroundColor(selectedColor)
                else holder.root.setCardBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    override fun getItemCount() = list.size

    private class HashMapWrapper(
        private val map: HashMap<PatchMeta, Boolean>,
        private val onSet: (items: HashMap<PatchMeta, Boolean>) -> Unit
    ) {
        constructor(
            map: List<PatchMeta>,
            onSet: (items: HashMap<PatchMeta, Boolean>) -> Unit
        ) : this(
            HashMap(map.associateWith { false }),
            onSet
        )

        fun setItems(map: List<PatchMeta>) {
            val tmp = map.associateWith { this.map[it] == true }
            this.map.clear()
            this.map.putAll(tmp)
        }

        fun filter(predicate: (Map.Entry<PatchMeta, Boolean>) -> Boolean) = map.filter(predicate)
        fun <A, B> map(transform: (Map.Entry<PatchMeta, Boolean>) -> Pair<A, B>) =
            map.map(transform).toMap()

        @Suppress("UNCHECKED_CAST")
        operator fun get(index: PatchMeta) = try {
            map[index] ?: false
        } catch (_: Exception) {
            false
        }

        fun set(index: PatchMeta, e: Boolean, internal: Boolean) {
            try {
                map[index] = e
                if (!internal) onSet(map)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        operator fun set(index: PatchMeta, e: Boolean) = set(index, e, false)
    }
}