package de.dertyp7214.rboardpatcher.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import de.dertyp7214.rboardcomponents.core.dpToPxRounded
import de.dertyp7214.rboardpatcher.R
import de.dertyp7214.rboardpatcher.screens.types.MainOption

class MainOptionAdapter(
    private val context: Context,
    private val list: List<MainOption>
) : RecyclerView.Adapter<MainOptionAdapter.ViewHolder>() {
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val root: MaterialCardView = v as MaterialCardView
        val icon: ImageView = v.findViewById(R.id.icon)
        val title: TextView = v.findViewById(R.id.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.main_option, parent, false))
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        when (position) {
            0 -> {
                if (list.size == 1){
                    holder.root.setBackgroundResource(R.drawable.color_surface_overlay_background_rounded)
                }
                else{
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
        holder.icon.setImageResource(item.icon)
        holder.title.text = item.title

        holder.root.setOnClickListener {
            item.action()
        }
    }
}