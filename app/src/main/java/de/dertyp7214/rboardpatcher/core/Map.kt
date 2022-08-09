@file:Suppress("unused")

package de.dertyp7214.rboardpatcher.core

fun <T, E> HashMap<T, E>.setAll(t: E) {
    forEach { (k, _) ->
        set(k, t)
    }
}