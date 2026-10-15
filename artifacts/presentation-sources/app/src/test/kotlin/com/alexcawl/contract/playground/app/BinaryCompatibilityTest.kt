package com.alexcawl.contract.playground.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alexcawl.contract.playground.component.v1.Contract1
import com.alexcawl.contract.playground.component.v2.Contract2
import com.alexcawl.contract.playground.styling.Contract1BinaryProbe
import com.alexcawl.contract.playground.styling.Contract2BinaryProbe
import com.alexcawl.contract.playground.styling.FunctionBinaryProbe
import kotlin.coroutines.EmptyCoroutineContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BinaryCompatibilityTest {
    @Test
    fun v1FactoryDefault() = inComposition {
        val contract = Contract1BinaryProbe.factoryDefault()
        assertEquals(RoundedCornerShape(10.dp), contract.shape())
        assertNull(contract.borderStroke())
    }

    @Test
    fun v1FactoryFull() = inComposition {
        val contract = Contract1BinaryProbe.factoryFull()
        assertEquals(RoundedCornerShape(10.dp), contract.shape())
        assertNull(contract.borderStroke())
    }

    @Test
    fun v1CopyDefault() = inComposition {
        val original = Contract1(
            cornerSize = { 10.dp },
            borderStroke = { BorderStroke(cornerSize(), Color.Black) },
        )
        val copy = Contract1BinaryProbe.copyDefault(original)
        assertEquals(20.dp, copy.cornerSize())
        assertEquals(RoundedCornerShape(10.dp), copy.shape())
        assertEquals(BorderStroke(10.dp, Color.Black), copy.borderStroke())
    }

    @Test
    fun v1CopyFull() = inComposition {
        val original = Contract1(
            cornerSize = { 10.dp },
            borderStroke = { BorderStroke(cornerSize(), Color.Black) },
        )
        val copy = Contract1BinaryProbe.copyFull(original)
        assertEquals(20.dp, copy.cornerSize())
        assertEquals(RoundedCornerShape(20.dp), copy.shape())
        assertEquals(BorderStroke(10.dp, Color.Black), copy.borderStroke())
    }

    @Test
    fun v2FactoryDefault() {
        val contract = Contract2BinaryProbe.factoryDefault()
        assertEquals(RoundedCornerShape(10.dp), contract.shape())
        assertNull(contract.borderStroke())
    }

    @Test
    fun v2FactoryFull() {
        val contract = Contract2BinaryProbe.factoryFull()
        assertEquals(RoundedCornerShape(10.dp), contract.shape())
        assertNull(contract.borderStroke())
    }

    @Test
    fun v2CopyDefault() {
        val original = Contract2(
            cornerSize = { 10.dp },
            borderStroke = { BorderStroke(cornerSize(), Color.Black) },
        )
        val copy = Contract2BinaryProbe.copyDefault(original)
        assertEquals(20.dp, copy.cornerSize())
        assertEquals(RoundedCornerShape(20.dp), copy.shape())
        assertEquals(BorderStroke(20.dp, Color.Black), copy.borderStroke())
    }

    @Test
    fun v2CopyFull() {
        val original = Contract2(
            cornerSize = { 10.dp },
            borderStroke = { BorderStroke(cornerSize(), Color.Black) },
        )
        val copy = Contract2BinaryProbe.copyFull(original)
        assertEquals(20.dp, copy.cornerSize())
        assertEquals(RoundedCornerShape(20.dp), copy.shape())
        assertEquals(BorderStroke(20.dp, Color.Black), copy.borderStroke())
    }

    @Test
    fun jvmOverloadsDefault() {
        assertEquals("value:default", FunctionBinaryProbe.jvmOverloadsDefault())
    }

    @Test
    fun jvmOverloadsFull() {
        assertEquals("value:explicit", FunctionBinaryProbe.jvmOverloadsFull())
    }

    @Test
    fun introducedAtDefault() {
        assertEquals("value:default", FunctionBinaryProbe.introducedAtDefault())
    }

    @Test
    fun introducedAtFull() {
        assertEquals("value:explicit", FunctionBinaryProbe.introducedAtFull())
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
