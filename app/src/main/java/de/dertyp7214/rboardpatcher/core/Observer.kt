package de.dertyp7214.rboardpatcher.core

import androidx.lifecycle.Observer

fun <T> Observe(block: Observer<T>.(T) -> Unit): Observer<T> {
    var observer: Observer<T>? = null
    observer = Observer<T> { t ->
        block(observer!!, t)
    }
    return observer
}