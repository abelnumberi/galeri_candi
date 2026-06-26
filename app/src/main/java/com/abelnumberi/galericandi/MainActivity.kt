package com.abelnumberi.galericandi

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.abelnumberi.galericandi.data.UserDataStore
import com.abelnumberi.galericandi.database.TempleDatabase
import com.abelnumberi.galericandi.repository.TempleRepository
import com.abelnumberi.galericandi.ui.theme.GaleriCandiTheme
import com.abelnumberi.galericandi.viewmodel.TempleViewModel
import com.abelnumberi.galericandi.viewmodel.TempleViewModelFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var database: TempleDatabase
    private lateinit var repository: TempleRepository
    private lateinit var userDataStore: UserDataStore
    private lateinit var viewModel: TempleViewModel
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize dependencies
        database = TempleDatabase.getDatabase(this)
        repository = TempleRepository(database.templeDao())
        userDataStore = UserDataStore(this)
        
        val factory = TempleViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TempleViewModel::class.java]
        
        // Register network callback for background syncing
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        registerNetworkCallback(connectivityManager)

        enableEdgeToEdge()
        setContent {
            GaleriCandiTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
                val userIdState by userDataStore.userId.collectAsState(initial = null)
                val coroutineScope = rememberCoroutineScope()

                // Check user session on launch
                LaunchedEffect(userIdState) {
                    val userId = userIdState
                    if (!userId.isNullOrEmpty()) {
                        if (currentScreen is Screen.Login) {
                            currentScreen = Screen.Home
                        }
                    } else {
                        currentScreen = Screen.Login
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val screen = currentScreen) {
                        is Screen.Login -> {
                            com.abelnumberi.galericandi.ui.LoginScreen { uid, name, email, profilePic ->
                                coroutineScope.launch {
                                    userDataStore.saveUserSession(uid, name, email, profilePic)
                                    currentScreen = Screen.Home
                                }
                            }
                        }
                        is Screen.Home -> {
                            val uid = userIdState ?: ""
                            com.abelnumberi.galericandi.ui.HomeScreen(
                                userId = uid,
                                viewModel = viewModel,
                                userDataStore = userDataStore,
                                onAddTempleClick = {
                                    currentScreen = Screen.AddEditTemple(null)
                                },
                                onTempleClick = { temple ->
                                    currentScreen = Screen.Detail(temple)
                                },
                                onEditTempleClick = { temple ->
                                    currentScreen = Screen.AddEditTemple(temple)
                                },
                                onProfileClick = {
                                    currentScreen = Screen.Profile
                                }
                            )
                        }
                        is Screen.Detail -> {
                            com.abelnumberi.galericandi.ui.DetailScreen(
                                temple = screen.temple,
                                onBackClick = {
                                    currentScreen = Screen.Home
                                },
                                onEditClick = { temple ->
                                    currentScreen = Screen.AddEditTemple(temple)
                                }
                            )
                        }
                        is Screen.AddEditTemple -> {
                            val uid = userIdState ?: ""
                            com.abelnumberi.galericandi.ui.TempleFormScreen(
                                userId = uid,
                                temple = screen.temple,
                                viewModel = viewModel,
                                onBackClick = {
                                    currentScreen = Screen.Home
                                },
                                onSaveSuccess = {
                                    currentScreen = Screen.Home
                                }
                            )
                        }
                        is Screen.Profile -> {
                            com.abelnumberi.galericandi.ui.ProfileScreen(
                                userDataStore = userDataStore,
                                onBackClick = {
                                    currentScreen = Screen.Home
                                },
                                onLogoutClick = {
                                    coroutineScope.launch {
                                        userDataStore.clearSession()
                                        viewModel.deleteAllLocal()
                                        currentScreen = Screen.Login
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun registerNetworkCallback(connectivityManager: ConnectivityManager) {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                lifecycleScope.launch {
                    val userId = userDataStore.userId.firstOrNull()
                    if (!userId.isNullOrEmpty()) {
                        viewModel.syncOfflineTemples(applicationContext, userId)
                    }
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        networkCallback?.let { callback ->
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

sealed class Screen {
    object Login : Screen()
    object Home : Screen()
    data class Detail(val temple: com.abelnumberi.galericandi.database.Temple) : Screen()
    data class AddEditTemple(val temple: com.abelnumberi.galericandi.database.Temple?) : Screen()
    object Profile : Screen()
}