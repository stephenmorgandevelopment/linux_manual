package com.stephenmorgandevelopment.thelinuxmanual.utils

import org.jsoup.select.Elements


fun Any?.isNull(): Boolean = this == null
fun Any?.isNotNull(): Boolean = this != null

fun Elements.toStringsList(): List<String> {
    return asList().map { it.text() }
}

fun <T> List<T>.add(item: T): List<T> {
    return this.toMutableList().apply { add(item) }.toList()
}

fun <T> List<T>.remove(item: T): List<T> {
    return this.toMutableList().apply { remove(item) }.toList()
}
