package de.dertyp7214.rboardpatcher.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import de.dertyp7214.colorutilsc.ColorUtilsC
import de.dertyp7214.rboardpatcher.R
import de.dertyp7214.rboardpatcher.adapter.types.ThemeDataClass
import de.dertyp7214.rboardpatcher.utils.doAsync

class ThemeAdapter(
    private val context: Context,
    private val list: List<ThemeDataClass>,
    private val onClickTheme: (ThemeDataClass) -> Unit
) : RecyclerView.Adapter<ThemeAdapter.ViewHolder>() {

    private var recyclerView: RecyclerView? = null
    private var lastPosition =
        recyclerView?.layoutManager?.let { (it as GridLayoutManager).findLastVisibleItemPosition() }
            ?: 0

    private val colorCache: HashMap<Int, Pair<Int, Boolean>> = hashMapOf()
    private val default by lazy {
        ContextCompat.getDrawable(
            context,
            R.drawable.ic_keyboard
        )!!.toBitmap()
    }

    init {
        setHasStableIds(true)
    }

    private fun cacheColor() {
        list.forEachIndexed { index, themeDataClass ->
            doAsync({
                ImageView(context).let { view ->
                    view.setImageBitmap(themeDataClass.image ?: default)
                    view.colorFilter = themeDataClass.colorFilter
                    val color = view.drawable.toBitmap().let {
                        it[0, it.height / 2]
                    }
                    Pair(color, ColorUtilsC.calculateLuminance(color) > .4)
                }
            }, { colorCache[index] = it })
        }
    }

    private fun getColorAndCache(dataClass: ThemeDataClass, position: Int): Int {
        return colorCache[position]?.first ?: ImageView(context).let { view ->
            view.setImageBitmap(dataClass.image ?: default)
            view.colorFilter = dataClass.colorFilter
            val color = view.drawable.toBitmap().let {
                it[0, it.height / 2]
            }
            colorCache[position] = Pair(color, ColorUtilsC.calculateLuminance(color) > .4)
            color
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyDataChanged() {
        colorCache.clear()
        cacheColor()
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return list[position].hashCode().toLong()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val themeImage: ImageView = v.findViewById(R.id.theme_image)
        val themeName: TextView = v.findViewById(R.id.theme_name)
        val themeNameSelect: TextView = v.findViewById(R.id.theme_name_selected)
        val card: MaterialCardView = v.findViewById(R.id.card)
        val gradient: View? = try {
            v.findViewById(R.id.gradient)
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.theme_grid_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataClass = list[position]

        holder.themeImage.setImageBitmap(dataClass.image ?: default)
        holder.themeImage.colorFilter = dataClass.colorFilter
        holder.themeImage.alpha = if (dataClass.image != null) 1F else .3F

        val color = getColorAndCache(dataClass, position)

        if (holder.gradient != null) {
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(color, Color.TRANSPARENT)
            )
            holder.gradient.background = gradient
        }

        holder.card.setCardBackgroundColor(color)

        holder.themeName.text = dataClass.readableName
        holder.themeNameSelect.text = dataClass.readableName

        holder.themeName.setTextColor(
            if (colorCache[position]?.second == true) Color.BLACK else Color.WHITE
        )

        holder.card.setOnClickListener {
            onClickTheme(dataClass)
        }

        setAnimation(holder.card, position)
    }

    override fun getItemCount() = list.size

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation =
                AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }
}