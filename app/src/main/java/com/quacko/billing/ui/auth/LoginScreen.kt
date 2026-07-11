package com.quacko.billing.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.quacko.billing.R
import com.quacko.billing.data.Config
import com.quacko.billing.ui.theme.SimnTeal
import com.quacko.billing.ui.theme.TextMuted

@Composable
fun LoginScreen(
    loggingIn: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var demo by remember { mutableStateOf(Config.demoMode) }
    var serverUrl by remember { mutableStateOf(Config.baseUrl) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(16.dp))
        Image(
            painter = painterResource(R.drawable.simn_logo),
            contentDescription = "SIMN",
            modifier = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Silverminds Interpreting",
            style = MaterialTheme.typography.titleLarge,
            color = SimnTeal,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Client portal · VRI & OPI",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username or email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Demo mode", fontWeight = FontWeight.SemiBold)
                Text(
                    if (demo) "Using built-in sample data" else "Connecting to live quack-o api.php",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            Switch(checked = demo, onCheckedChange = { demo = it; Config.demoMode = it })
        }

        if (!demo) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it; Config.baseUrl = it.trim() },
                label = { Text("Server URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onLogin(username, password) },
            enabled = !loggingIn && username.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (loggingIn) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Sign in")
        }

        if (demo) {
            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text("Demo accounts", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = { username = "client"; password = "client" }) { Text("client / client") }
                TextButton(onClick = { username = "admin"; password = "admin" }) { Text("admin / admin") }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
