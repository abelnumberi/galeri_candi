package com.abelnumberi.galericandi.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.abelnumberi.galericandi.R
import com.abelnumberi.galericandi.database.Temple
import com.abelnumberi.galericandi.viewmodel.TempleViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempleFormScreen(
    userId: String,
    temple: Temple?,
    viewModel: TempleViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isEdit = temple != null

    var name by remember { mutableStateOf(temple?.name ?: "") }
    var location by remember { mutableStateOf(temple?.location ?: "") }
    var description by remember { mutableStateOf(temple?.description ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf(temple?.imageUrl ?: "") }
    
    var isLoading by remember { mutableStateOf(false) }

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = if (isEdit) "Edit Candi" else "Tambah Candi") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image Selection Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (existingImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = existingImageUrl,
                            contentDescription = "Existing Image",
                            placeholder = painterResource(id = R.drawable.loading_img),
                            error = painterResource(id = R.drawable.account_circle_24),
                            fallback = painterResource(id = R.drawable.account_circle_24),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.account_circle_24),
                            contentDescription = "No Image Selected",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                ) {
                    Text(text = "Pilih Foto Candi")
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Input Fields
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = "Nama Candi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(text = "Lokasi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = "Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save & Cancel Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text(text = stringResource(id = R.string.btn_cancel))
                    }
                    Button(
                        onClick = {
                            if (name.isBlank() || location.isBlank() || description.isBlank()) {
                                Toast.makeText(context, context.getString(R.string.err_empty_fields), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedImageUri == null && existingImageUrl.isEmpty()) {
                                Toast.makeText(context, context.getString(R.string.err_no_photo), Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isLoading = true
                            val templeToSave = Temple(
                                id = temple?.id ?: UUID.randomUUID().toString(),
                                userId = userId,
                                name = name,
                                location = location,
                                description = description,
                                imageUrl = existingImageUrl
                            )

                            viewModel.saveTemple(
                                temple = templeToSave,
                                localImageUri = selectedImageUri,
                                context = context,
                                isEdit = isEdit
                            ) {
                                isLoading = false
                                onSaveSuccess()
                            }
                        },
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text(text = stringResource(id = R.string.btn_save))
                    }
                }
            }
        }

        // Full Screen Loading Overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Menyimpan data...")
                    }
                }
            }
        }
    }
}
