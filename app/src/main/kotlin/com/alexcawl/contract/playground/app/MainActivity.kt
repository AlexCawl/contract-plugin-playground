package com.alexcawl.contract.playground.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alexcawl.contract.playground.component.v1.ContractButton1
import com.alexcawl.contract.playground.component.v2.ContractButton2
import com.alexcawl.contract.playground.styling.v1.Contract1Presets
import com.alexcawl.contract.playground.styling.v2.Contract2Presets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.safeDrawingPadding().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ContractButton1(contract = Contract1Presets.default, onClick = {})
                        ContractButton2(contract = Contract2Presets.default, onClick = {})
                    }
                }
            }
        }
    }
}
