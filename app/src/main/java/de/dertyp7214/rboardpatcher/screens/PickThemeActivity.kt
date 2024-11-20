package de.dertyp7214.rboardpatcher.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import de.dertyp7214.colorutilsc.ColorUtilsC
import de.dertyp7214.rboardcomponents.utils.ThemeUtils
import de.dertyp7214.rboardpatcher.Application
import de.dertyp7214.rboardpatcher.R
import de.dertyp7214.rboardpatcher.adapter.types.ThemeDataClass
import de.dertyp7214.rboardpatcher.components.MarginItemDecoration
import de.dertyp7214.rboardpatcher.core.dp
import de.dertyp7214.rboardpatcher.core.parseThemeDataClass
import de.dertyp7214.rboardpatcher.core.readableName
import de.dertyp7214.rboardpatcher.core.set
import de.dertyp7214.rboardpatcher.core.toZip
import de.dertyp7214.rboardpatcher.utils.doAsync

class PickThemeActivity : AppCompatActivity() {

    private val forbiddenThemes = listOf(
        "silk:",
        "system_auto:",
    )

    private val preinstalledThemes = listOf(
        "color_black", "color_blue", "color_blue_grey", "color_brown", "color_cyan",
        "color_deep_purple", "color_green", "color_light_pink", "color_pink", "color_red",
        "color_sand", "color_teal", "google_blue_dark", "google_blue_light", "holo_blue",
        "holo_white", "material_dark", "material_light"
    )

    private val themes = ArrayList<Pair<String, Bitmap?>>()

    private val progressBar: CircularProgressIndicator by lazy { findViewById(R.id.progressBar) }
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }

    private val adapter by lazy {
        Adapter(this, themes) {
            val rboardTheme = Application.rboardService?.getRboardTheme(it)
            val theme = rboardTheme?.toZip(this)?.absolutePath?.parseThemeDataClass()

            if (theme != null) openPatchActivity(theme)
        }
    }

    private fun openPatchActivity(themeDataClass: ThemeDataClass) {
        ThemeUtils.applyTheme(this) { _, _ ->
            PatchActivity::class.java[this] = {
                putExtra("themePath", themeDataClass.path)
            }
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.navigationBarColor = Color.TRANSPARENT
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_theme)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(MarginItemDecoration(2.dp(this), all = true))

        Application.rboardService?.apply {
            doAsync({
                themes.clear()
                rboardThemes.forEach {
                    if (forbiddenThemes.none { forbidden -> it.startsWith(forbidden) } && preinstalledThemes.none { preinstalled ->
                            it.equals(
                                preinstalled
                            )
                        })
                        themes.add(it to getPreview(it))
                }
            }) {
                progressBar.visibility = View.GONE
                adapter.notifyDataChanged()
            }
        }
    }

    class Adapter(
        private val context: Context,
        private val themes: ArrayList<Pair<String, Bitmap?>>,
        private val onClickTheme: (String) -> Unit
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

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
            themes.forEachIndexed { index, theme ->
                doAsync({
                    val color = (theme.second ?: default).let {
                        it[0, it.height / 2]
                    }
                    Pair(color, ColorUtilsC.calculateLuminance(color) > .4)
                }, { colorCache[index] = it })
            }
        }

        private fun getColorAndCache(image: Bitmap?, position: Int): Int {
            return colorCache[position]?.first ?: (image ?: default).let { bmp ->
                val color = bmp[0, bmp.height / 2]
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
            return themes[position].hashCode().toLong()
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
                LayoutInflater.from(parent.context).inflate(R.layout.theme_grid_item, parent, false)
            )
        }

        override fun getItemCount(): Int = themes.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val theme = themes[position]

            holder.themeImage.setImageBitmap(theme.second ?: default)
            holder.themeImage.alpha = if (theme.second != null) 1F else .3F

            val color = getColorAndCache(theme.second, position)

            if (holder.gradient != null) {
                val gradient = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(color, Color.TRANSPARENT)
                )
                holder.gradient.background = gradient
            }

            holder.card.setCardBackgroundColor(color)

            holder.themeName.text = theme.first.readableName()
            holder.themeNameSelect.text = theme.first.readableName()

            holder.themeName.setTextColor(
                if (colorCache[position]?.second == true) Color.BLACK else Color.WHITE
            )

            holder.card.setOnClickListener {
                onClickTheme(theme.first)
            }

            setAnimation(holder.card, position)
        }

        private fun setAnimation(viewToAnimate: View, position: Int) {
            if (position > lastPosition) {
                val animation =
                    AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down)
                viewToAnimate.startAnimation(animation)
                lastPosition = position
            }
        }
    }
}