package com.alexcawl.contract.playground.component.v1

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.coroutines.EmptyCoroutineContext
import org.junit.Assert.assertEquals
import org.junit.Test

class Contract1Test {
    @Test
    fun isDelegated() = inComposition {
        val original = Contract1(
            cornerSize = { 10.dp },
            borderStroke = { BorderStroke(cornerSize(), Color.Black) },
        )
        val copy = remember(original) { original.copy(cornerSize = { 20.dp }) }

        assertEquals(20.dp, copy.cornerSize())
        assertEquals(RoundedCornerShape(10.dp), copy.shape())
        assertEquals(BorderStroke(10.dp, Color.Black), copy.borderStroke())
    }

    @Test
    fun isRebinding() = inComposition {
        val original = Contract1(
            cornerSize = { 10.dp },
            borderStroke = { BorderStroke(cornerSize(), Color.Black) },
        )
        val copy = remember(original) { original.copy(cornerSize = { 20.dp }) }

        assertEquals(20.dp, copy.cornerSize())
        assertEquals(RoundedCornerShape(20.dp), copy.shape())
        assertEquals(BorderStroke(20.dp, Color.Black), copy.borderStroke())
    }

    private fun inComposition(content: @Composable () -> Unit) {
        val recomposer = Recomposer(EmptyCoroutineContext)
        val composition = Composition(
            object : AbstractApplier<Unit>(Unit) {
                override fun insertTopDown(index: Int, instance: Unit) = Unit
                override fun insertBottomUp(index: Int, instance: Unit) = Unit
                override fun move(from: Int, to: Int, count: Int) = Unit
                override fun remove(index: Int, count: Int) = Unit
                override fun onClear() = Unit
            },
            recomposer,
        )
        try {
            composition.setContent(content)
        } finally {
            composition.dispose()
            recomposer.cancel()
        }
    }
}
