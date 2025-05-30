package com.example.applibros.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.applibros.viewmodel.AuthViewModel


@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {

        Text(text = "Registro", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.register(
                    email = email,
                    password = password,
                    username = username,
                    onSuccess = {
                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        onRegisterSuccess()
                    },
                    onFailure = { e ->
                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }
    }
}



