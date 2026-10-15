package com.alexcawl.contract.playground.component.v2

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class Contract2Test {
    @Test
    fun isDelegated() {
        val original = Contract2(
            cornerSize = { 10.dp },
            borderStroke = { BorderStroke(cornerSize(), Color.Black) },
        )
        val copy = original.copy(cornerSize = { 20.dp })

        assertEquals(20.dp, copy.cornerSize())
        assertEquals(RoundedCornerShape(10.dp), copy.shape())
        assertEquals(BorderStroke(10.dp, Color.Black), copy.borderStroke())
    }

    @Test
    fun isRebinding() {
        val original = Contract2(
            cornerSize = { 10.dp },
            borderStroke = { BorderStroke(cornerSize(), Color.Black) },
        )
        val copy = original.copy(cornerSize = { 20.dp })

        assertEquals(20.dp, copy.cornerSize())
        assertEquals(RoundedCornerShape(20.dp), copy.shape())
        assertEquals(BorderStroke(20.dp, Color.Black), copy.borderStroke())
    }
}
