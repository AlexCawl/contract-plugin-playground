package com.alexcawl.contract.playground.component.v1

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

}

inline fun Contract1(
    crossinline cornerSize: @Composable Contract1.() -> Dp,
    noinline shape: (@Composable Contract1.() -> Shape)? = null,
): Contract1 = object : ReflectiveEqualsHashCode(), Contract1 {
    @Composable
    override fun cornerSize(): Dp = cornerSize.invoke(this)

    @Composable
    override fun shape(): Shape = if (shape != null) shape.invoke(this) else super.shape()

}

interface Contract1Wrapper : Contract1 {
    val wrapped: Contract1
}

@RememberInComposition
inline fun Contract1.copy(
    crossinline cornerSize: @Composable Contract1Wrapper.() -> Dp = { wrapped.cornerSize() },
    crossinline shape: @Composable Contract1Wrapper.() -> Shape = { wrapped.shape() },
): Contract1 = object : ReflectiveEqualsHashCode(), Contract1Wrapper {
    override val wrapped: Contract1
        get() = this@copy

    @Composable
    override fun cornerSize(): Dp = cornerSize.invoke(this)

    @Composable
    override fun shape(): Shape = shape.invoke(this)

}

@Composable
inline fun Contract1.rememberCopy(
    crossinline cornerSize: @Composable Contract1Wrapper.() -> Dp = { wrapped.cornerSize() },
    crossinline shape: @Composable Contract1Wrapper.() -> Shape = { wrapped.shape() },
): Contract1 = remember {
    // shortcut: retains the first copy when the source changes; add keys if reactive copies are needed.
    copy(cornerSize = cornerSize, shape = shape)
}
