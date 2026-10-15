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
import com.alexcawl.contract.playground.styling.v1.Contract1Presets
import com.alexcawl.contract.playground.styling.v2.Contract2Presets
import kotlin.coroutines.EmptyCoroutineContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assume.assumeNotNull
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
    fun v1RememberCopy() = inComposition {
        val copy = Contract1BinaryProbe.rememberedCopy(Contract1(cornerSize = { 10.dp }))
        assertEquals(20.dp, copy.cornerSize())
        assertEquals(RoundedCornerShape(10.dp), copy.shape())
    }

    @Test
    fun v1Preset() = inComposition {
        assertEquals(RoundedCornerShape(12.dp), Contract1Presets.default.shape())
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
    fun v2SnapshotDefault() {
        val snapshot = Contract2BinaryProbe.snapshotDefault()
        assertEquals(10.dp, snapshot.cornerSize(Contract2(cornerSize = { 20.dp })))
        assertNull(snapshot.shape)
    }

    @Test
    fun v2SnapshotFull() {
        val snapshot = Contract2BinaryProbe.snapshotFull()
        val source = Contract2(cornerSize = { 20.dp })
        assertEquals(10.dp, snapshot.cornerSize(source))
        assertEquals(RoundedCornerShape(20.dp), snapshot.shape!!.invoke(source))
    }

    @Test
    fun v2Preset() = inComposition {
        assertEquals(RoundedCornerShape(12.dp), Contract2Presets.default.shape())
    }

    @Test
    fun newV2CallbackSurvivesOldCopy() {
        val factory = Class.forName("com.alexcawl.contract.playground.component.v2.Contract2Kt")
            .declaredMethods.find { it.name == "Contract2" && it.parameterCount == 3 }
        assumeNotNull(factory)
        val cornerSize: Contract2.() -> androidx.compose.ui.unit.Dp = { 10.dp }
        val borderStroke: Contract2.() -> BorderStroke? = { BorderStroke(this.cornerSize(), Color.Black) }
        val original = factory!!.invoke(null, cornerSize, null, borderStroke) as Contract2
        val copy = Contract2BinaryProbe.copyDefault(original)
        val border = Contract2::class.java.getMethod("borderStroke").invoke(copy)
        assertEquals(BorderStroke(20.dp, Color.Black), border)
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
