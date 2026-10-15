package com.alexcawl.contract.playground.styling.v1

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.alexcawl.contract.playground.component.v1.Contract1

object Contract1Presets {
    val default: Contract1
        @Composable
        get() = remember {
            Contract1(
                cornerSize = { 12.dp },
            )
        }
}
