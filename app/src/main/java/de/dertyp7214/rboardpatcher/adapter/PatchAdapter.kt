package de.dertyp7214.rboardpatcher.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
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
import de.dertyp7214.rboardpatcher.core.setAll

class PatchAdapter(
    private val context: Context,
    private val list: List<PatchMeta>,
    private val onSelect: (List<PatchMeta>) -> Unit
) : RecyclerView.Adapter<PatchAdapter.ViewHolder>() {
    private val selectedColor = ColorUtilsC.setAlphaComponent(
        context.getAttr(com.google.android.material.R.attr.colorOnSecondary),
        40
    )
    private val selected = ArrayListWrapper(list.map { false }) {
        onSelect(getSelected())
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getSelected(): List<PatchMeta> {
        return try {
            selected.mapIndexed { index, value -> Pair(list[index], value) }
                .filter { it.second }.map { it.first }
        } catch (_: Exception) {
            listOf()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyDataChanged() {
        selected.apply {
            clear()
            addAll(list.map { false })
        }
        notifyDataSetChanged()
    }

    fun unselectAll() {
        selected.setAll(false)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val root = v as MaterialCardView
        val title: TextView = v.findViewById(R.id.title)
        val author: TextView = v.findViewById(R.id.author)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.patch_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val patchMeta = list[position]

        if (selected[position]) holder.root.setCardBackgroundColor(selectedColor)
        else holder.root.setCardBackgroundColor(Color.TRANSPARENT)

        holder.title.text = patchMeta.name
        holder.author.text = patchMeta.author

        holder.root.setOnClickListener {
            selected[position] = !selected[position]

            if (selected[position]) holder.root.setCardBackgroundColor(selectedColor)
            else holder.root.setCardBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount() = list.size

    private class ArrayListWrapper<E>(
        private val list: ArrayList<E>,
        private val onSet: (items: ArrayList<E>) -> Unit
    ) {
        constructor(list: List<E>, onSet: (items: ArrayList<E>) -> Unit) : this(
            ArrayList(list),
            onSet
        )

        fun <T> mapIndexed(transform: (index: Int, e: E) -> T) = list.mapIndexed(transform)
        fun clear() = list.clear()
        fun addAll(items: List<E>) = list.addAll(items)

        @Suppress("UNCHECKED_CAST")
        operator fun get(index: Int) = try {
            list[index]
        } catch (_: Exception) {
            false as E
        }

        operator fun set(index: Int, e: E) {
            try {
                list[index] = e
                onSet(list)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun setAll(e: E) {
            list.setAll(e)
            onSet(list)
        }
    }
}