package com.example.applibros.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.applibros.viewmodel.AuthViewModel

@Composable
fun StartScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    //Habilitar cuando ya tenga mi aplicacion mas hecha
    //Esto sirve para que un usuario no tenga que logearse cada que
    //ingresa a la app
    /*
    LaunchedEffect(Unit) {
        if (viewModel.checkUserLoggedIn()) {
            onNavigateToHome()
        }
    }
    */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido a AppLibros", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth()) {
            Text("Registrarse")
        }
    }
}
