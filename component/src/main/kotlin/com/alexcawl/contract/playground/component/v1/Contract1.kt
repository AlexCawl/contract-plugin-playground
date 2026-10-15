package com.alexcawl.contract.playground.component.v1

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.annotation.RememberInComposition
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Stable
interface Contract1 {
    // 1.0
    @Composable
    fun cornerSize(): Dp

    // 1.0
    @Composable
    fun shape(): Shape = RoundedCornerShape(cornerSize())

    // 1.1
    @Composable
    fun borderStroke(): BorderStroke? = null
}

inline fun Contract1(
    crossinline cornerSize: @Composable Contract1.() -> Dp,
    noinline shape: (@Composable Contract1.() -> Shape)? = null,
    noinline borderStroke: (@Composable Contract1.() -> BorderStroke?)? = null,
): Contract1 = object : ReflectiveEqualsHashCode(), Contract1 {
    @Composable
    override fun cornerSize(): Dp = cornerSize.invoke(this)

    @Composable
    override fun shape(): Shape = if (shape != null) shape.invoke(this) else super.shape()

    @Composable
    override fun borderStroke(): BorderStroke? =
        if (borderStroke != null) borderStroke.invoke(this) else super.borderStroke()
}

interface Contract1Wrapper : Contract1 {
    val wrapped: Contract1
}

@RememberInComposition
inline fun Contract1.copy(
    crossinline cornerSize: @Composable Contract1Wrapper.() -> Dp = { wrapped.cornerSize() },
    crossinline shape: @Composable Contract1Wrapper.() -> Shape = { wrapped.shape() },
    crossinline borderStroke: @Composable Contract1Wrapper.() -> BorderStroke? = { wrapped.borderStroke() },
): Contract1 = object : ReflectiveEqualsHashCode(), Contract1Wrapper {
    override val wrapped: Contract1
        get() = this@copy

    @Composable
    override fun cornerSize(): Dp = cornerSize.invoke(this)

    @Composable
    override fun shape(): Shape = shape.invoke(this)

    @Composable
    override fun borderStroke(): BorderStroke? = borderStroke.invoke(this)
}

@Composable
inline fun Contract1.rememberCopy(
    crossinline cornerSize: @Composable Contract1Wrapper.() -> Dp = { wrapped.cornerSize() },
    crossinline shape: @Composable Contract1Wrapper.() -> Shape = { wrapped.shape() },
    crossinline borderStroke: @Composable Contract1Wrapper.() -> BorderStroke? = { wrapped.borderStroke() },
): Contract1 = remember {
    // shortcut: retains the first copy when the source changes; add keys if reactive copies are needed.
    copy(cornerSize = cornerSize, shape = shape, borderStroke = borderStroke)
}
