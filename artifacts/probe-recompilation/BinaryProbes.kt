package com.alexcawl.contract.playground.styling

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.alexcawl.contract.playground.component.v1.Contract1
import com.alexcawl.contract.playground.component.v1.copy
import com.alexcawl.contract.playground.component.v1.rememberCopy
import com.alexcawl.contract.playground.component.v2.Contract2
import com.alexcawl.contract.playground.component.v2.copy
import com.alexcawl.contract.playground.component.withIntroducedAt
import com.alexcawl.contract.playground.component.withJvmOverloads

// These call sites are published once in styling:1.0 and never recompiled for Component 1.1.
object Contract1BinaryProbe {
    fun factoryDefault(): Contract1 = Contract1(cornerSize = { 10.dp })

    fun factoryFull(): Contract1 = Contract1(
        cornerSize = { 10.dp },
        shape = { RoundedCornerShape(cornerSize()) },
    )

    fun copyDefault(source: Contract1): Contract1 = source.copy(cornerSize = { 20.dp })

    fun copyFull(source: Contract1): Contract1 = source.copy(
        cornerSize = { 20.dp },
        shape = { RoundedCornerShape(cornerSize()) },
    )
}

object Contract2BinaryProbe {
    fun factoryDefault(): Contract2 = Contract2(cornerSize = { 10.dp })

    fun factoryFull(): Contract2 = Contract2(
        cornerSize = { 10.dp },
        shape = { RoundedCornerShape(cornerSize()) },
    )

    fun copyDefault(source: Contract2): Contract2 = source.copy(cornerSize = { 20.dp })

    fun copyFull(source: Contract2): Contract2 = source.copy(
        cornerSize = { 20.dp },
        shape = { RoundedCornerShape(cornerSize()) },
    )
}

object FunctionBinaryProbe {
    fun jvmOverloadsDefault(): String = withJvmOverloads("value")

    fun jvmOverloadsFull(): String = withJvmOverloads("value", ":explicit")

    fun introducedAtDefault(): String = withIntroducedAt("value")

    fun introducedAtFull(): String = withIntroducedAt("value", ":explicit")
}
