package com.example.applibros.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.applibros.R
import com.example.applibros.viewmodel.AuthViewModel
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
@Composable
fun StartScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel()

    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }


    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Verificar si ya está logueado
    LaunchedEffect(Unit) {
        if (viewModel.checkUserLoggedIn()) {
            onNavigateToHome()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                viewModel.loginWithGoogle(
                    idToken = idToken,
                    onSuccess = {
                        Toast.makeText(context, "Inicio con Google exitoso", Toast.LENGTH_SHORT).show()
                        onNavigateToHome()
                    },
                    onFailure = {
                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("TU_CLIENT_ID_DE_OAUTH2") // Reemplaza esto
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        launcher.launch(googleSignInClient.signInIntent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido a AppLibros", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

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
                viewModel.login(
                    email,
                    password,
                    onSuccess = {
                        Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        onNavigateToHome()
                    },
                    onFailure = { e ->
                        Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            showResetDialog = true
        }) {
            Text("¿Olvidaste tu contraseña?")
        }


        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth()) {
            Text("Registrarse")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { launchGoogleSignIn() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Sign-In",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Continuar con Google")
        }
    }
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            viewModel.resetPassword(
                                email = resetEmail.trim(),
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Si el correo está registrado, recibirás un enlace de recuperación.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    showResetDialog = false
                                },
                                onFailure = {
                                    Toast.makeText(
                                        context,
                                        "Error al enviar el correo: ${it.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Recuperar contraseña") },
            text = {
                Column {
                    Text("Introduce tu correo electrónico para recuperar tu contraseña:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        singleLine = true,
                        placeholder = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }


}

