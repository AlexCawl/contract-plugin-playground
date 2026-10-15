package com.alexcawl.contract.playground.styling.v2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.alexcawl.contract.playground.component.v2.Contract2

object Contract2Presets {
    val default: Contract2
        @Composable
        get(): Contract2 {
            return remember {
                Contract2(
                    cornerSize = { 12.dp },
                )
            }
        }
}
