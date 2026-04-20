package com.tanim.omniguard.utils.extensions

fun String.isValidPackageName(): Boolean {
    return this.isNotEmpty() && this.contains(".")
}