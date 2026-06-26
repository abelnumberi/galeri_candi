package com.abelnumberi.galericandi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.abelnumberi.galericandi.R
import com.abelnumberi.galericandi.data.UserDataStore
import com.abelnumberi.galericandi.database.Temple
import com.abelnumberi.galericandi.viewmodel.TempleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: String,
    viewModel: TempleViewModel,
    userDataStore: UserDataStore,
    onAddTempleClick: () -> Unit,
    onTempleClick: (Temple) -> Unit,
    onEditTempleClick: (Temple) -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val temples by viewModel.getTemples(userId).collectAsState(initial = emptyList())
    val userProfilePic by userDataStore.userProfilePic.collectAsState(initial = "")
    var searchQuery by remember { mutableStateOf("") }
    var templeToDelete by remember { mutableStateOf<Temple?>(null) }

    LaunchedEffect(key1 = userId) {
        viewModel.fetchTemples(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        if (!userProfilePic.isNullOrEmpty()) {
                            AsyncImage(
                                model = userProfilePic,
                                contentDescription = "Profile",
                                placeholder = painterResource(id = R.drawable.account_circle_24),
                                error = painterResource(id = R.drawable.account_circle_24),
                                fallback = painterResource(id = R.drawable.account_circle_24),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTempleClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Temple")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Cari nama atau lokasi candi...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            val filteredTemples = temples.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.location.contains(searchQuery, ignoreCase = true)
            }

            if (filteredTemples.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Tidak ada data candi.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filteredTemples) { temple ->
                        TempleCard(
                            temple = temple,
                            onClick = { onTempleClick(temple) },
                            onEditClick = { onEditTempleClick(temple) },
                            onDeleteClick = { templeToDelete = temple }
                        )
                    }
                }
            }
        }
    }

    if (templeToDelete != null) {
        AlertDialog(
            onDismissRequest = { templeToDelete = null },
            title = { Text(text = "Hapus Candi") },
            text = { Text(text = "Apakah Anda yakin ingin menghapus candi ${templeToDelete?.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        templeToDelete?.let { temple ->
                            viewModel.deleteTemple(temple, context) {
                                // Deletion callback success
                            }
                        }
                        templeToDelete = null
                    }
                ) {
                    Text(text = stringResource(id = R.string.btn_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { templeToDelete = null }) {
                    Text(text = stringResource(id = R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
fun TempleCard(
    temple: Temple,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = temple.imageUrl,
                    contentDescription = temple.name,
                    placeholder = painterResource(id = R.drawable.loading_img),
                    error = painterResource(id = R.drawable.account_circle_24),
                    fallback = painterResource(id = R.drawable.account_circle_24),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onEditClick
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onDeleteClick
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.errorContainer,
                            tonalElevation = 2.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = temple.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = temple.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = temple.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
