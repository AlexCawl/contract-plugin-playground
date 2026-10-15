package com.alexcawl.contract.playground.component.v2

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ContractButton2(
    contract: Contract2,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = contract.shape(),
    ) {
        Text("Contract 2")
    }
}
