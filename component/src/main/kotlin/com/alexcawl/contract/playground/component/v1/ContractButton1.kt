package com.alexcawl.contract.playground.component.v1

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ContractButton1(
    contract: Contract1,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = contract.shape(),
        border = contract.borderStroke(),
    ) {
        Text("Contract 1")
    }
}
