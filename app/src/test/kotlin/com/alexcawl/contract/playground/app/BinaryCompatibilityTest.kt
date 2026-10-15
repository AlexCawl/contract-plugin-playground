package com.alexcawl.contract.playground.app

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
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
        assertEquals(RoundedCornerShape(10.dp), Contract1BinaryProbe.factoryDefault().shape())
    }

    @Test
    fun v1FactoryFull() = inComposition {
        assertEquals(RoundedCornerShape(10.dp), Contract1BinaryProbe.factoryFull().shape())
    }

    @Test
    fun v1CopyDefault() = inComposition {
        val copy = Contract1BinaryProbe.copyDefault(Contract1(cornerSize = { 10.dp }))
        assertEquals(20.dp, copy.cornerSize())
        assertEquals(RoundedCornerShape(10.dp), copy.shape())
    }

    @Test
    fun v1CopyFull() = inComposition {
        val copy = Contract1BinaryProbe.copyFull(Contract1(cornerSize = { 10.dp }))
        assertEquals(20.dp, copy.cornerSize())
        assertEquals(RoundedCornerShape(20.dp), copy.shape())
    }

    @Test
    fun v2FactoryDefault() {
        assertEquals(RoundedCornerShape(10.dp), Contract2BinaryProbe.factoryDefault().shape())
    }

    @Test
    fun v2FactoryFull() {
        assertEquals(RoundedCornerShape(10.dp), Contract2BinaryProbe.factoryFull().shape())
    }

    @Test
    fun v2CopyDefault() {
        val copy = Contract2BinaryProbe.copyDefault(Contract2(cornerSize = { 10.dp }))
        assertEquals(20.dp, copy.cornerSize())
        assertEquals(RoundedCornerShape(20.dp), copy.shape())
    }

    @Test
    fun v2CopyFull() {
        val copy = Contract2BinaryProbe.copyFull(Contract2(cornerSize = { 10.dp }))
        assertEquals(20.dp, copy.cornerSize())
        assertEquals(RoundedCornerShape(20.dp), copy.shape())
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
