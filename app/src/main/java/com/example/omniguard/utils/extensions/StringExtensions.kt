package com.example.omniguard.utils.extensions

fun String.isValidPackageName(): Boolean {
    return this.isNotEmpty() && this.contains(".")
}
