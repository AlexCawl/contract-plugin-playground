package com.alexcawl.contract.playground.component

@JvmOverloads
fun withJvmOverloads(
    // 1.0
    required: String,
    // 1.0
    optional: String = ":default",
): String = required + optional

fun withIntroducedAt(
    // 1.0
    required: String,
    // 1.0
    optional: String = ":default",
): String = required + optional
