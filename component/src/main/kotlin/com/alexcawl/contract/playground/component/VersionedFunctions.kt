@file:OptIn(ExperimentalVersionOverloading::class)

package com.alexcawl.contract.playground.component

@JvmOverloads
fun withJvmOverloads(
    // 1.0
    required: String,
    // 1.0
    optional: String = ":default",
    // 1.1
    added: String = "",
): String = required + optional + added

fun withIntroducedAt(
    // 1.0
    required: String,
    // 1.0
    optional: String = ":default",
    // 1.1
    @IntroducedAt("1.1") added: String = "",
): String = required + optional + added
