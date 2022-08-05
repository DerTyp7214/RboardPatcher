package de.dertyp7214.rboardpatcher.components

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.LinearLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import de.dertyp7214.rboardpatcher.R
import de.dertyp7214.rboardpatcher.core.dp
import de.dertyp7214.rboardpatcher.core.getAttr
import java.util.*

class ChipContainer(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val chipGroup: ChipGroup
    private val clearTags: Chip

    private var filterToggleListener: (filters: List<String>) -> Unit = {}
    private val chips = arrayListOf<ChipData>()

    init {
        inflate(context, R.layout.chip_container, this)

        chipGroup = findViewById(R.id.chipGroup)
        clearTags = findViewById(R.id.clear_tags)

        clearTags.setOnClickListener {
            chips.forEachIndexed { index, _ ->
                chips[index].selected = false
            }
            refreshChips()
            filterToggleListener(listOf())
        }
    }

    fun setChips(chips: List<String>) {
        synchronized(this.chips) {
            this.chips.clear()
            this.chips.addAll(chips.map { ChipData(it, false) }
                .sortedBy { it.text.lowercase(Locale.getDefault()) })
            refreshChips()
        }
    }

    fun setOnFilterToggle(listener: (filters: List<String>) -> Unit) {
        filterToggleListener = listener
    }

    val filters: List<String>
        get() {
            return chips.filter { it.selected }.map { it.text }
        }

    private fun refreshChips() {
        chipGroup.removeAllViews()

        val backgroundColor = context.getAttr(android.R.attr.colorBackground)
        val strokeColor = context.getAttr(androidx.appcompat.R.attr.colorBackgroundFloating)
        val rippleColor = context.getAttr(androidx.appcompat.R.attr.colorBackgroundFloating)
        val textColor = context.getAttr(com.google.android.material.R.attr.colorOnSecondary)

        val backgroundColorSelected = context.getAttr(androidx.appcompat.R.attr.colorBackgroundFloating)
        val strokeColorSelected = context.getAttr(com.google.android.material.R.attr.colorOnSecondary)
        val rippleColorSelected = context.getAttr(com.google.android.material.R.attr.colorOnSecondary)

        chips.forEachIndexed { index, it ->
            val chip = Chip(context)
            chip.rippleColor =
                ColorStateList.valueOf(if (it.selected) rippleColorSelected else rippleColor)
            chip.chipBackgroundColor =
                ColorStateList.valueOf(if (it.selected) backgroundColorSelected else backgroundColor)
            chip.chipStrokeColor =
                ColorStateList.valueOf(if (it.selected) strokeColorSelected else strokeColor)
            chip.chipStrokeWidth = 1.dp(context).toFloat()
            chip.text = it.text
            chip.setTextColor(textColor)
            chip.setOnClickListener {
                toggleChip(index)
            }

            chipGroup.addView(chip)
        }
    }

    private fun toggleChip(index: Int) {
        chips[index].selected = !chips[index].selected
        refreshChips()

        filterToggleListener(chips.filter { it.selected }.map { it.text })
    }

    private data class ChipData(val text: String, var selected: Boolean)
}