package com.stephenmorgandevelopment.thelinuxmanual.utils

import org.jsoup.select.Elements

fun Any?.isNull(): Boolean = this == null
fun Any?.isNotNull(): Boolean = this != null

fun Elements.toStringsList(): List<String> {
    return asList().map { it.text() }
}
