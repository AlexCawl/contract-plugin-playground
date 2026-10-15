package com.alexcawl.contract.playground.component.v2

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Stable
interface Contract2 {
    // 1.0
    fun cornerSize(): Dp

    // 1.0
    fun shape(): Shape = RoundedCornerShape(cornerSize())

    // 1.1
    fun borderStroke(): BorderStroke? = null

    fun contract(): Contract

    class Contract(
        val cornerSize: Contract2.() -> Dp,
        val shape: (Contract2.() -> Shape)? = null,
        val borderStroke: (Contract2.() -> BorderStroke?)? = null,
    )
}

private class Contract2Impl(
    private val cornerSize: Contract2.() -> Dp,
    private val shape: (Contract2.() -> Shape)?,
    private val borderStroke: (Contract2.() -> BorderStroke?)?,
) : Contract2 {
    override fun cornerSize(): Dp = cornerSize.invoke(this)

    override fun shape(): Shape {
        val callback = shape
        return if (callback == null) super<Contract2>.shape() else callback.invoke(this)
    }

    override fun borderStroke(): BorderStroke? {
        val callback = borderStroke
        return if (callback == null) super<Contract2>.borderStroke() else callback.invoke(this)
    }

    override fun contract(): Contract2.Contract = Contract2.Contract(cornerSize, shape, borderStroke)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Contract2Impl) return false
        return cornerSize == other.cornerSize && shape == other.shape && borderStroke == other.borderStroke
    }

    override fun hashCode(): Int {
        var result = cornerSize.hashCode()
        result = 31 * result + (shape?.hashCode() ?: 0)
        result = 31 * result + (borderStroke?.hashCode() ?: 0)
        return result
    }
}

fun Contract2(
    cornerSize: Contract2.() -> Dp,
    shape: (Contract2.() -> Shape)? = null,
    borderStroke: (Contract2.() -> BorderStroke?)? = null,
): Contract2 = Contract2Impl(cornerSize, shape, borderStroke)

fun Contract2.copy(
    cornerSize: (Contract2.() -> Dp)? = null,
    shape: (Contract2.() -> Shape)? = null,
    borderStroke: (Contract2.() -> BorderStroke?)? = null,
): Contract2 {
    val original = contract()
    return Contract2(
        cornerSize = cornerSize ?: original.cornerSize,
        shape = shape ?: original.shape,
        borderStroke = borderStroke ?: original.borderStroke,
    )
}
