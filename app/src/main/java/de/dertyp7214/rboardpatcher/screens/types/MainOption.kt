package de.dertyp7214.rboardpatcher.screens.types

import androidx.annotation.DrawableRes

data class MainOption(
    @DrawableRes val icon: Int,
    val title: String,
    val action: () -> Unit
)