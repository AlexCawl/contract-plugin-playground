# Полные сигнатуры Component 1.0 и 1.1

Исходники ниже извлечены из оригинальных опубликованных sources.jar. JVM-сигнатуры взяты из сохранённого javap -p -s. Типы, имена JVM-методов и descriptors не сокращены.

## Component 1.0: исходники

### Contract1.kt

```kotlin
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
```

### Contract2.kt

```kotlin
package com.alexcawl.contract.playground.component.v2

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

    fun contract(): Contract

    class Contract(
        val cornerSize: Contract2.() -> Dp,
        val shape: (Contract2.() -> Shape)? = null,
    )
}

private class Contract2Impl(
    private val cornerSize: Contract2.() -> Dp,
    private val shape: (Contract2.() -> Shape)?,
) : Contract2 {
    override fun cornerSize(): Dp = cornerSize.invoke(this)

    override fun shape(): Shape {
        val callback = shape
        return if (callback == null) super<Contract2>.shape() else callback.invoke(this)
    }

    override fun contract(): Contract2.Contract = Contract2.Contract(cornerSize, shape)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Contract2Impl) return false
        return cornerSize == other.cornerSize && shape == other.shape
    }

    override fun hashCode(): Int {
        var result = cornerSize.hashCode()
        result = 31 * result + (shape?.hashCode() ?: 0)
        return result
    }
}

fun Contract2(
    cornerSize: Contract2.() -> Dp,
    shape: (Contract2.() -> Shape)? = null,
): Contract2 = Contract2Impl(cornerSize, shape)

fun Contract2.copy(
    cornerSize: (Contract2.() -> Dp)? = null,
    shape: (Contract2.() -> Shape)? = null,
): Contract2 {
    val original = contract()
    return Contract2(
        cornerSize = cornerSize ?: original.cornerSize,
        shape = shape ?: original.shape,
    )
}
```

### VersionedFunctions.kt

```kotlin
package com.alexcawl.contract.playground.component

@JvmOverloads
fun withJvmOverloads(
    // 1.0
    required: String,
    // 1.0
    optional: String = ":default",
): String = required + optional

fun withIntroducedAt(
    // 1.0
    required: String,
    // 1.0
    optional: String = ":default",
): String = required + optional
```

## Component 1.0: JVM

### com.alexcawl.contract.playground.component.v1.Contract1

```text
Compiled from "Contract1.kt"
public interface com.alexcawl.contract.playground.component.v1.Contract1 {
  public abstract float cornerSize-chRvn1I(androidx.compose.runtime.Composer, int);
    descriptor: (Landroidx/compose/runtime/Composer;I)F

  public default androidx.compose.ui.graphics.Shape shape(androidx.compose.runtime.Composer, int);
    descriptor: (Landroidx/compose/runtime/Composer;I)Landroidx/compose/ui/graphics/Shape;

  public static androidx.compose.ui.graphics.Shape access$shape$jd(com.alexcawl.contract.playground.component.v1.Contract1, androidx.compose.runtime.Composer, int);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Landroidx/compose/runtime/Composer;I)Landroidx/compose/ui/graphics/Shape;
}
```

### com.alexcawl.contract.playground.component.v1.Contract1$DefaultImpls

```text
Compiled from "Contract1.kt"
public final class com.alexcawl.contract.playground.component.v1.Contract1$DefaultImpls {
  public static androidx.compose.ui.graphics.Shape shape(com.alexcawl.contract.playground.component.v1.Contract1, androidx.compose.runtime.Composer, int);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Landroidx/compose/runtime/Composer;I)Landroidx/compose/ui/graphics/Shape;
}
```

### com.alexcawl.contract.playground.component.v1.Contract1Wrapper

```text
Compiled from "Contract1.kt"
public interface com.alexcawl.contract.playground.component.v1.Contract1Wrapper extends com.alexcawl.contract.playground.component.v1.Contract1 {
  public abstract com.alexcawl.contract.playground.component.v1.Contract1 getWrapped();
    descriptor: ()Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static androidx.compose.ui.graphics.Shape access$shape$jd(com.alexcawl.contract.playground.component.v1.Contract1Wrapper, androidx.compose.runtime.Composer, int);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1Wrapper;Landroidx/compose/runtime/Composer;I)Landroidx/compose/ui/graphics/Shape;
}
```

### com.alexcawl.contract.playground.component.v1.Contract1Kt

```text
Compiled from "Contract1.kt"
public final class com.alexcawl.contract.playground.component.v1.Contract1Kt {
  public static final com.alexcawl.contract.playground.component.v1.Contract1 Contract1(kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, ? extends androidx.compose.ui.graphics.Shape>);
    descriptor: (Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;)Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static com.alexcawl.contract.playground.component.v1.Contract1 Contract1$default(kotlin.jvm.functions.Function3, kotlin.jvm.functions.Function3, int, java.lang.Object);
    descriptor: (Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;ILjava/lang/Object;)Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static final com.alexcawl.contract.playground.component.v1.Contract1 copy(com.alexcawl.contract.playground.component.v1.Contract1, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1Wrapper, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1Wrapper, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, ? extends androidx.compose.ui.graphics.Shape>);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;)Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static com.alexcawl.contract.playground.component.v1.Contract1 copy$default(com.alexcawl.contract.playground.component.v1.Contract1, kotlin.jvm.functions.Function3, kotlin.jvm.functions.Function3, int, java.lang.Object);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;ILjava/lang/Object;)Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static final com.alexcawl.contract.playground.component.v1.Contract1 rememberCopy(com.alexcawl.contract.playground.component.v1.Contract1, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1Wrapper, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1Wrapper, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, ? extends androidx.compose.ui.graphics.Shape>, androidx.compose.runtime.Composer, int, int);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;Landroidx/compose/runtime/Composer;II)Lcom/alexcawl/contract/playground/component/v1/Contract1;
}
```

### com.alexcawl.contract.playground.component.v2.Contract2

```text
Compiled from "Contract2.kt"
public interface com.alexcawl.contract.playground.component.v2.Contract2 {
  public abstract float cornerSize-D9Ej5fM();
    descriptor: ()F

  public default androidx.compose.ui.graphics.Shape shape();
    descriptor: ()Landroidx/compose/ui/graphics/Shape;

  public abstract com.alexcawl.contract.playground.component.v2.Contract2$Contract contract();
    descriptor: ()Lcom/alexcawl/contract/playground/component/v2/Contract2$Contract;

  public static androidx.compose.ui.graphics.Shape access$shape$jd(com.alexcawl.contract.playground.component.v2.Contract2);
    descriptor: (Lcom/alexcawl/contract/playground/component/v2/Contract2;)Landroidx/compose/ui/graphics/Shape;
}
```

### com.alexcawl.contract.playground.component.v2.Contract2$DefaultImpls

```text
Compiled from "Contract2.kt"
public final class com.alexcawl.contract.playground.component.v2.Contract2$DefaultImpls {
  public static androidx.compose.ui.graphics.Shape shape(com.alexcawl.contract.playground.component.v2.Contract2);
    descriptor: (Lcom/alexcawl/contract/playground/component/v2/Contract2;)Landroidx/compose/ui/graphics/Shape;
}
```

### com.alexcawl.contract.playground.component.v2.Contract2$Contract

```text
Compiled from "Contract2.kt"
public final class com.alexcawl.contract.playground.component.v2.Contract2$Contract {
  private final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.unit.Dp> cornerSize;
    descriptor: Lkotlin/jvm/functions/Function1;
  private final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.graphics.Shape> shape;
    descriptor: Lkotlin/jvm/functions/Function1;
  public static final int $stable;
    descriptor: I
  public com.alexcawl.contract.playground.component.v2.Contract2$Contract(kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, ? extends androidx.compose.ui.graphics.Shape>);
    descriptor: (Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V

  public com.alexcawl.contract.playground.component.v2.Contract2$Contract(kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, int, kotlin.jvm.internal.DefaultConstructorMarker);
    descriptor: (Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;ILkotlin/jvm/internal/DefaultConstructorMarker;)V

  public final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.unit.Dp> getCornerSize();
    descriptor: ()Lkotlin/jvm/functions/Function1;

  public final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.graphics.Shape> getShape();
    descriptor: ()Lkotlin/jvm/functions/Function1;

  static {};
    descriptor: ()V
}
```

### com.alexcawl.contract.playground.component.v2.Contract2Kt

```text
Compiled from "Contract2.kt"
public final class com.alexcawl.contract.playground.component.v2.Contract2Kt {
  public static final com.alexcawl.contract.playground.component.v2.Contract2 Contract2(kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, ? extends androidx.compose.ui.graphics.Shape>);
    descriptor: (Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)Lcom/alexcawl/contract/playground/component/v2/Contract2;

  public static com.alexcawl.contract.playground.component.v2.Contract2 Contract2$default(kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, int, java.lang.Object);
    descriptor: (Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;ILjava/lang/Object;)Lcom/alexcawl/contract/playground/component/v2/Contract2;

  public static final com.alexcawl.contract.playground.component.v2.Contract2 copy(com.alexcawl.contract.playground.component.v2.Contract2, kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, ? extends androidx.compose.ui.graphics.Shape>);
    descriptor: (Lcom/alexcawl/contract/playground/component/v2/Contract2;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)Lcom/alexcawl/contract/playground/component/v2/Contract2;

  public static com.alexcawl.contract.playground.component.v2.Contract2 copy$default(com.alexcawl.contract.playground.component.v2.Contract2, kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, int, java.lang.Object);
    descriptor: (Lcom/alexcawl/contract/playground/component/v2/Contract2;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;ILjava/lang/Object;)Lcom/alexcawl/contract/playground/component/v2/Contract2;
}
```

### com.alexcawl.contract.playground.component.v2.Contract2Impl

```text
Compiled from "Contract2.kt"
final class com.alexcawl.contract.playground.component.v2.Contract2Impl implements com.alexcawl.contract.playground.component.v2.Contract2 {
  private final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.unit.Dp> cornerSize;
    descriptor: Lkotlin/jvm/functions/Function1;
  private final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.graphics.Shape> shape;
    descriptor: Lkotlin/jvm/functions/Function1;
  public com.alexcawl.contract.playground.component.v2.Contract2Impl(kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, ? extends androidx.compose.ui.graphics.Shape>);
    descriptor: (Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V

  public float cornerSize-D9Ej5fM();
    descriptor: ()F

  public androidx.compose.ui.graphics.Shape shape();
    descriptor: ()Landroidx/compose/ui/graphics/Shape;

  public com.alexcawl.contract.playground.component.v2.Contract2$Contract contract();
    descriptor: ()Lcom/alexcawl/contract/playground/component/v2/Contract2$Contract;

  public boolean equals(java.lang.Object);
    descriptor: (Ljava/lang/Object;)Z

  public int hashCode();
    descriptor: ()I
}
```

### com.alexcawl.contract.playground.component.VersionedFunctionsKt

```text
Compiled from "VersionedFunctions.kt"
public final class com.alexcawl.contract.playground.component.VersionedFunctionsKt {
  public static final java.lang.String withJvmOverloads(java.lang.String, java.lang.String);
    descriptor: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

  public static java.lang.String withJvmOverloads$default(java.lang.String, java.lang.String, int, java.lang.Object);
    descriptor: (Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/String;

  public static final java.lang.String withIntroducedAt(java.lang.String, java.lang.String);
    descriptor: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

  public static java.lang.String withIntroducedAt$default(java.lang.String, java.lang.String, int, java.lang.Object);
    descriptor: (Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/String;

  public static final java.lang.String withJvmOverloads(java.lang.String);
    descriptor: (Ljava/lang/String;)Ljava/lang/String;
}
```

## Component 1.1: исходники

### Contract1.kt

```kotlin
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

@Deprecated("Binary compatibility with 1.0", level = DeprecationLevel.HIDDEN)
inline fun Contract1(
    crossinline cornerSize: @Composable Contract1.() -> Dp,
    noinline shape: (@Composable Contract1.() -> Shape)? = null,
): Contract1 = Contract1(cornerSize = cornerSize, shape = shape, borderStroke = null)

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

@Deprecated("Binary compatibility with 1.0", level = DeprecationLevel.HIDDEN)
@RememberInComposition
inline fun Contract1.copy(
    crossinline cornerSize: @Composable Contract1Wrapper.() -> Dp = { wrapped.cornerSize() },
    crossinline shape: @Composable Contract1Wrapper.() -> Shape = { wrapped.shape() },
): Contract1 = copy(
    cornerSize = cornerSize,
    shape = shape,
    borderStroke = { wrapped.borderStroke() },
)

@Deprecated("Binary compatibility with 1.0", level = DeprecationLevel.HIDDEN)
@Composable
inline fun Contract1.rememberCopy(
    crossinline cornerSize: @Composable Contract1Wrapper.() -> Dp = { wrapped.cornerSize() },
    crossinline shape: @Composable Contract1Wrapper.() -> Shape = { wrapped.shape() },
): Contract1 = rememberCopy(
    cornerSize = cornerSize,
    shape = shape,
    borderStroke = { wrapped.borderStroke() },
)
```

### Contract2.kt

```kotlin
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
    ) {
        @Deprecated("Binary compatibility with 1.0", level = DeprecationLevel.HIDDEN)
        constructor(
            cornerSize: Contract2.() -> Dp,
            shape: (Contract2.() -> Shape)? = null,
        ) : this(cornerSize, shape, null)
    }
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

@Deprecated("Binary compatibility with 1.0", level = DeprecationLevel.HIDDEN)
fun Contract2(
    cornerSize: Contract2.() -> Dp,
    shape: (Contract2.() -> Shape)? = null,
): Contract2 = Contract2(cornerSize = cornerSize, shape = shape, borderStroke = null)

@Deprecated("Binary compatibility with 1.0", level = DeprecationLevel.HIDDEN)
fun Contract2.copy(
    cornerSize: (Contract2.() -> Dp)? = null,
    shape: (Contract2.() -> Shape)? = null,
): Contract2 = copy(cornerSize = cornerSize, shape = shape, borderStroke = null)
```

### VersionedFunctions.kt

```kotlin
@file:OptIn(ExperimentalVersionOverloading::class)

package com.alexcawl.contract.playground.component

@JvmOverloads
fun withJvmOverloads(
    // 1.0
    required: String,
    // 1.0
    optional: String = ":default",
    // 1.1
    added: String = "",
): String = required + optional + added

fun withIntroducedAt(
    // 1.0
    required: String,
    // 1.0
    optional: String = ":default",
    // 1.1
    @IntroducedAt("1.1") added: String = "",
): String = required + optional + added
```

## Component 1.1: JVM

### com.alexcawl.contract.playground.component.v1.Contract1

```text
Compiled from "Contract1.kt"
public interface com.alexcawl.contract.playground.component.v1.Contract1 {
  public abstract float cornerSize-chRvn1I(androidx.compose.runtime.Composer, int);
    descriptor: (Landroidx/compose/runtime/Composer;I)F

  public default androidx.compose.ui.graphics.Shape shape(androidx.compose.runtime.Composer, int);
    descriptor: (Landroidx/compose/runtime/Composer;I)Landroidx/compose/ui/graphics/Shape;

  public default androidx.compose.foundation.BorderStroke borderStroke(androidx.compose.runtime.Composer, int);
    descriptor: (Landroidx/compose/runtime/Composer;I)Landroidx/compose/foundation/BorderStroke;

  public static androidx.compose.ui.graphics.Shape access$shape$jd(com.alexcawl.contract.playground.component.v1.Contract1, androidx.compose.runtime.Composer, int);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Landroidx/compose/runtime/Composer;I)Landroidx/compose/ui/graphics/Shape;

  public static androidx.compose.foundation.BorderStroke access$borderStroke$jd(com.alexcawl.contract.playground.component.v1.Contract1, androidx.compose.runtime.Composer, int);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Landroidx/compose/runtime/Composer;I)Landroidx/compose/foundation/BorderStroke;
}
```

### com.alexcawl.contract.playground.component.v1.Contract1$DefaultImpls

```text
Compiled from "Contract1.kt"
public final class com.alexcawl.contract.playground.component.v1.Contract1$DefaultImpls {
  public static androidx.compose.ui.graphics.Shape shape(com.alexcawl.contract.playground.component.v1.Contract1, androidx.compose.runtime.Composer, int);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Landroidx/compose/runtime/Composer;I)Landroidx/compose/ui/graphics/Shape;

  public static androidx.compose.foundation.BorderStroke borderStroke(com.alexcawl.contract.playground.component.v1.Contract1, androidx.compose.runtime.Composer, int);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Landroidx/compose/runtime/Composer;I)Landroidx/compose/foundation/BorderStroke;
}
```

### com.alexcawl.contract.playground.component.v1.Contract1Wrapper

```text
Compiled from "Contract1.kt"
public interface com.alexcawl.contract.playground.component.v1.Contract1Wrapper extends com.alexcawl.contract.playground.component.v1.Contract1 {
  public abstract com.alexcawl.contract.playground.component.v1.Contract1 getWrapped();
    descriptor: ()Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static androidx.compose.ui.graphics.Shape access$shape$jd(com.alexcawl.contract.playground.component.v1.Contract1Wrapper, androidx.compose.runtime.Composer, int);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1Wrapper;Landroidx/compose/runtime/Composer;I)Landroidx/compose/ui/graphics/Shape;

  public static androidx.compose.foundation.BorderStroke access$borderStroke$jd(com.alexcawl.contract.playground.component.v1.Contract1Wrapper, androidx.compose.runtime.Composer, int);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1Wrapper;Landroidx/compose/runtime/Composer;I)Landroidx/compose/foundation/BorderStroke;
}
```

### com.alexcawl.contract.playground.component.v1.Contract1Kt

```text
Compiled from "Contract1.kt"
public final class com.alexcawl.contract.playground.component.v1.Contract1Kt {
  public static final com.alexcawl.contract.playground.component.v1.Contract1 Contract1(kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, ? extends androidx.compose.ui.graphics.Shape>, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, androidx.compose.foundation.BorderStroke>);
    descriptor: (Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;)Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static com.alexcawl.contract.playground.component.v1.Contract1 Contract1$default(kotlin.jvm.functions.Function3, kotlin.jvm.functions.Function3, kotlin.jvm.functions.Function3, int, java.lang.Object);
    descriptor: (Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;ILjava/lang/Object;)Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static final com.alexcawl.contract.playground.component.v1.Contract1 Contract1(kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, ? extends androidx.compose.ui.graphics.Shape>);
    descriptor: (Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;)Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static com.alexcawl.contract.playground.component.v1.Contract1 Contract1$default(kotlin.jvm.functions.Function3, kotlin.jvm.functions.Function3, int, java.lang.Object);
    descriptor: (Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;ILjava/lang/Object;)Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static final com.alexcawl.contract.playground.component.v1.Contract1 copy(com.alexcawl.contract.playground.component.v1.Contract1, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1Wrapper, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1Wrapper, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, ? extends androidx.compose.ui.graphics.Shape>, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1Wrapper, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, androidx.compose.foundation.BorderStroke>);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;)Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static com.alexcawl.contract.playground.component.v1.Contract1 copy$default(com.alexcawl.contract.playground.component.v1.Contract1, kotlin.jvm.functions.Function3, kotlin.jvm.functions.Function3, kotlin.jvm.functions.Function3, int, java.lang.Object);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;ILjava/lang/Object;)Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static final com.alexcawl.contract.playground.component.v1.Contract1 rememberCopy(com.alexcawl.contract.playground.component.v1.Contract1, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1Wrapper, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1Wrapper, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, ? extends androidx.compose.ui.graphics.Shape>, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1Wrapper, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, androidx.compose.foundation.BorderStroke>, androidx.compose.runtime.Composer, int, int);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;Landroidx/compose/runtime/Composer;II)Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static final com.alexcawl.contract.playground.component.v1.Contract1 copy(com.alexcawl.contract.playground.component.v1.Contract1, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1Wrapper, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1Wrapper, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, ? extends androidx.compose.ui.graphics.Shape>);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;)Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static com.alexcawl.contract.playground.component.v1.Contract1 copy$default(com.alexcawl.contract.playground.component.v1.Contract1, kotlin.jvm.functions.Function3, kotlin.jvm.functions.Function3, int, java.lang.Object);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;ILjava/lang/Object;)Lcom/alexcawl/contract/playground/component/v1/Contract1;

  public static final com.alexcawl.contract.playground.component.v1.Contract1 rememberCopy(com.alexcawl.contract.playground.component.v1.Contract1, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1Wrapper, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function3<? super com.alexcawl.contract.playground.component.v1.Contract1Wrapper, ? super androidx.compose.runtime.Composer, ? super java.lang.Integer, ? extends androidx.compose.ui.graphics.Shape>, androidx.compose.runtime.Composer, int, int);
    descriptor: (Lcom/alexcawl/contract/playground/component/v1/Contract1;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;Landroidx/compose/runtime/Composer;II)Lcom/alexcawl/contract/playground/component/v1/Contract1;
}
```

### com.alexcawl.contract.playground.component.v2.Contract2

```text
Compiled from "Contract2.kt"
public interface com.alexcawl.contract.playground.component.v2.Contract2 {
  public abstract float cornerSize-D9Ej5fM();
    descriptor: ()F

  public default androidx.compose.ui.graphics.Shape shape();
    descriptor: ()Landroidx/compose/ui/graphics/Shape;

  public default androidx.compose.foundation.BorderStroke borderStroke();
    descriptor: ()Landroidx/compose/foundation/BorderStroke;

  public abstract com.alexcawl.contract.playground.component.v2.Contract2$Contract contract();
    descriptor: ()Lcom/alexcawl/contract/playground/component/v2/Contract2$Contract;

  public static androidx.compose.ui.graphics.Shape access$shape$jd(com.alexcawl.contract.playground.component.v2.Contract2);
    descriptor: (Lcom/alexcawl/contract/playground/component/v2/Contract2;)Landroidx/compose/ui/graphics/Shape;

  public static androidx.compose.foundation.BorderStroke access$borderStroke$jd(com.alexcawl.contract.playground.component.v2.Contract2);
    descriptor: (Lcom/alexcawl/contract/playground/component/v2/Contract2;)Landroidx/compose/foundation/BorderStroke;
}
```

### com.alexcawl.contract.playground.component.v2.Contract2$DefaultImpls

```text
Compiled from "Contract2.kt"
public final class com.alexcawl.contract.playground.component.v2.Contract2$DefaultImpls {
  public static androidx.compose.ui.graphics.Shape shape(com.alexcawl.contract.playground.component.v2.Contract2);
    descriptor: (Lcom/alexcawl/contract/playground/component/v2/Contract2;)Landroidx/compose/ui/graphics/Shape;

  public static androidx.compose.foundation.BorderStroke borderStroke(com.alexcawl.contract.playground.component.v2.Contract2);
    descriptor: (Lcom/alexcawl/contract/playground/component/v2/Contract2;)Landroidx/compose/foundation/BorderStroke;
}
```

### com.alexcawl.contract.playground.component.v2.Contract2$Contract

```text
Compiled from "Contract2.kt"
public final class com.alexcawl.contract.playground.component.v2.Contract2$Contract {
  private final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.unit.Dp> cornerSize;
    descriptor: Lkotlin/jvm/functions/Function1;
  private final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.graphics.Shape> shape;
    descriptor: Lkotlin/jvm/functions/Function1;
  private final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.foundation.BorderStroke> borderStroke;
    descriptor: Lkotlin/jvm/functions/Function1;
  public static final int $stable;
    descriptor: I
  public com.alexcawl.contract.playground.component.v2.Contract2$Contract(kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, ? extends androidx.compose.ui.graphics.Shape>, kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.foundation.BorderStroke>);
    descriptor: (Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V

  public com.alexcawl.contract.playground.component.v2.Contract2$Contract(kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, int, kotlin.jvm.internal.DefaultConstructorMarker);
    descriptor: (Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;ILkotlin/jvm/internal/DefaultConstructorMarker;)V

  public final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.unit.Dp> getCornerSize();
    descriptor: ()Lkotlin/jvm/functions/Function1;

  public final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.graphics.Shape> getShape();
    descriptor: ()Lkotlin/jvm/functions/Function1;

  public final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.foundation.BorderStroke> getBorderStroke();
    descriptor: ()Lkotlin/jvm/functions/Function1;

  public com.alexcawl.contract.playground.component.v2.Contract2$Contract(kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1);
    descriptor: (Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V

  public com.alexcawl.contract.playground.component.v2.Contract2$Contract(kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, int, kotlin.jvm.internal.DefaultConstructorMarker);
    descriptor: (Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;ILkotlin/jvm/internal/DefaultConstructorMarker;)V

  static {};
    descriptor: ()V
}
```

### com.alexcawl.contract.playground.component.v2.Contract2Kt

```text
Compiled from "Contract2.kt"
public final class com.alexcawl.contract.playground.component.v2.Contract2Kt {
  public static final com.alexcawl.contract.playground.component.v2.Contract2 Contract2(kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, ? extends androidx.compose.ui.graphics.Shape>, kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.foundation.BorderStroke>);
    descriptor: (Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)Lcom/alexcawl/contract/playground/component/v2/Contract2;

  public static com.alexcawl.contract.playground.component.v2.Contract2 Contract2$default(kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, int, java.lang.Object);
    descriptor: (Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;ILjava/lang/Object;)Lcom/alexcawl/contract/playground/component/v2/Contract2;

  public static final com.alexcawl.contract.playground.component.v2.Contract2 copy(com.alexcawl.contract.playground.component.v2.Contract2, kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, ? extends androidx.compose.ui.graphics.Shape>, kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.foundation.BorderStroke>);
    descriptor: (Lcom/alexcawl/contract/playground/component/v2/Contract2;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)Lcom/alexcawl/contract/playground/component/v2/Contract2;

  public static com.alexcawl.contract.playground.component.v2.Contract2 copy$default(com.alexcawl.contract.playground.component.v2.Contract2, kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, int, java.lang.Object);
    descriptor: (Lcom/alexcawl/contract/playground/component/v2/Contract2;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;ILjava/lang/Object;)Lcom/alexcawl/contract/playground/component/v2/Contract2;

  public static final com.alexcawl.contract.playground.component.v2.Contract2 Contract2(kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1);
    descriptor: (Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)Lcom/alexcawl/contract/playground/component/v2/Contract2;

  public static com.alexcawl.contract.playground.component.v2.Contract2 Contract2$default(kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, int, java.lang.Object);
    descriptor: (Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;ILjava/lang/Object;)Lcom/alexcawl/contract/playground/component/v2/Contract2;

  public static final com.alexcawl.contract.playground.component.v2.Contract2 copy(com.alexcawl.contract.playground.component.v2.Contract2, kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1);
    descriptor: (Lcom/alexcawl/contract/playground/component/v2/Contract2;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)Lcom/alexcawl/contract/playground/component/v2/Contract2;

  public static com.alexcawl.contract.playground.component.v2.Contract2 copy$default(com.alexcawl.contract.playground.component.v2.Contract2, kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, int, java.lang.Object);
    descriptor: (Lcom/alexcawl/contract/playground/component/v2/Contract2;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;ILjava/lang/Object;)Lcom/alexcawl/contract/playground/component/v2/Contract2;
}
```

### com.alexcawl.contract.playground.component.v2.Contract2Impl

```text
Compiled from "Contract2.kt"
final class com.alexcawl.contract.playground.component.v2.Contract2Impl implements com.alexcawl.contract.playground.component.v2.Contract2 {
  private final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.unit.Dp> cornerSize;
    descriptor: Lkotlin/jvm/functions/Function1;
  private final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.graphics.Shape> shape;
    descriptor: Lkotlin/jvm/functions/Function1;
  private final kotlin.jvm.functions.Function1<com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.foundation.BorderStroke> borderStroke;
    descriptor: Lkotlin/jvm/functions/Function1;
  public com.alexcawl.contract.playground.component.v2.Contract2Impl(kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.ui.unit.Dp>, kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, ? extends androidx.compose.ui.graphics.Shape>, kotlin.jvm.functions.Function1<? super com.alexcawl.contract.playground.component.v2.Contract2, androidx.compose.foundation.BorderStroke>);
    descriptor: (Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V

  public float cornerSize-D9Ej5fM();
    descriptor: ()F

  public androidx.compose.ui.graphics.Shape shape();
    descriptor: ()Landroidx/compose/ui/graphics/Shape;

  public androidx.compose.foundation.BorderStroke borderStroke();
    descriptor: ()Landroidx/compose/foundation/BorderStroke;

  public com.alexcawl.contract.playground.component.v2.Contract2$Contract contract();
    descriptor: ()Lcom/alexcawl/contract/playground/component/v2/Contract2$Contract;

  public boolean equals(java.lang.Object);
    descriptor: (Ljava/lang/Object;)Z

  public int hashCode();
    descriptor: ()I
}
```

### com.alexcawl.contract.playground.component.VersionedFunctionsKt

```text
Compiled from "VersionedFunctions.kt"
public final class com.alexcawl.contract.playground.component.VersionedFunctionsKt {
  public static final java.lang.String withJvmOverloads(java.lang.String, java.lang.String, java.lang.String);
    descriptor: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

  public static java.lang.String withJvmOverloads$default(java.lang.String, java.lang.String, java.lang.String, int, java.lang.Object);
    descriptor: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/String;

  public static final java.lang.String withIntroducedAt(java.lang.String, java.lang.String, java.lang.String);
    descriptor: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

  public static java.lang.String withIntroducedAt$default(java.lang.String, java.lang.String, java.lang.String, int, java.lang.Object);
    descriptor: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/String;

  public static final java.lang.String withIntroducedAt(java.lang.String, java.lang.String);
    descriptor: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

  public static java.lang.String withIntroducedAt$default(java.lang.String, java.lang.String, int, java.lang.Object);
    descriptor: (Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/String;

  public static final java.lang.String withJvmOverloads(java.lang.String, java.lang.String);
    descriptor: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

  public static final java.lang.String withJvmOverloads(java.lang.String);
    descriptor: (Ljava/lang/String;)Ljava/lang/String;
}
```

