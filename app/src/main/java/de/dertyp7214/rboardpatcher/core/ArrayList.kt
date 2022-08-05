package de.dertyp7214.rboardpatcher.core

fun <T> ArrayList<T>.setAll(t: T) {
    for (i in 0 until size) set(i, t)
}