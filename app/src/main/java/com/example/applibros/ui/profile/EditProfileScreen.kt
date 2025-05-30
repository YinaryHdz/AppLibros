package com.example.applibros.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.applibros.viewmodel.UserViewModel

@Composable
fun EditProfileScreen(
    viewModel: UserViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()

    val usernameState = remember(user) { mutableStateOf(user?.username ?: "") }
    val bioState = remember(user) { mutableStateOf(user?.bio ?: "") }
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri.value = uri
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Editar perfil", style = MaterialTheme.typography.headlineSmall)

        if (selectedImageUri.value != null) {
            AsyncImage(
                model = selectedImageUri.value, //
                contentDescription = "Nueva imagen de perfil",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
        } else if (!user?.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = user!!.photoUrl,
                contentDescription = "Imagen de perfil",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Imagen de perfil por defecto",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
        }

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Seleccionar imagen")
        }

        OutlinedTextField(
            value = usernameState.value,
            onValueChange = { usernameState.value = it },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bioState.value,
            onValueChange = { bioState.value = it },
            label = { Text("Biografía") },
            modifier = Modifier.fillMaxWidth()
        )




        Button(onClick = {
            val originalUsername = user?.username ?: ""
            val originalBio = user?.bio ?: ""
            val finalUsername = if (usernameState.value != originalUsername) usernameState.value else originalUsername
            val finalBio = if (bioState.value != originalBio) bioState.value else originalBio

            if (selectedImageUri.value != null) {
                viewModel.uploadProfileImageToImgBBAndSave(
                    context = context,
                    imageUri = selectedImageUri.value!!,
                    username = finalUsername,
                    bio = finalBio,
                    onComplete = {
                        Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                )

            } else {
                viewModel.updateProfile(
                    photoUrl = user?.photoUrl.orEmpty(),
                    username = finalUsername,
                    bio = finalBio,
                    onComplete = {
                        Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                )
            }
        })

        {
            Text("Guardar cambios")
        }
    }
}
