package com.stephenmorgandevelopment.thelinuxmanual.utils

import android.util.Log
import kotlin.reflect.KClass

private val TAG_PREFIX = ""
fun Class<*>.ilog(message: String) {
    Log.i("$TAG_PREFIX-${this.simpleName}", message)
}

fun Class<*>.dlog(message: String) {
    Log.d("$TAG_PREFIX-${this.simpleName}", message)
}

fun Class<*>.wlog(message: String) {
    Log.w("$TAG_PREFIX-${this.simpleName}", message)
}

fun Class<*>.elog(message: String) {
    Log.e("$TAG_PREFIX-${this.simpleName}", message)
}

fun Class<*>.vlog(message: String) {
    Log.v("$TAG_PREFIX-${this.simpleName}", message)
}

fun KClass<*>.ilog(message: String) {
    Log.i("$TAG_PREFIX-${this.simpleName}", message)
}

fun KClass<*>.dlog(message: String) {
    Log.d("$TAG_PREFIX-${this.simpleName}", message)
}

fun KClass<*>.wlog(message: String) {
    Log.w("$TAG_PREFIX-${this.simpleName}", message)
}

fun KClass<*>.elog(message: String) {
    Log.e("$TAG_PREFIX-${this.simpleName}", message)
}

fun KClass<*>.vlog(message: String) {
    Log.v("$TAG_PREFIX-${this.simpleName}", message)
}
