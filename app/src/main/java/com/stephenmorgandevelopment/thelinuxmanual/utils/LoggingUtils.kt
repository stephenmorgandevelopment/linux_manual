package com.stephenmorgandevelopment.thelinuxmanual.utils

import android.util.Log

private val TAG_PREFIX = "UMP"
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
