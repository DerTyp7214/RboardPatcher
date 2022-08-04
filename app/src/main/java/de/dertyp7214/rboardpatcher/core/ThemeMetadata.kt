package de.dertyp7214.rboardpatcher.core

import de.dertyp7214.rboardpatcher.patcher.types.ThemeMetadata

fun ThemeMetadata.newId() {
    id = "patcher_${System.currentTimeMillis()}"
}