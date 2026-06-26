package com.abelnumberi.galericandi.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abelnumberi.galericandi.R
import com.abelnumberi.galericandi.database.Temple
import com.abelnumberi.galericandi.network.ApiConfig
import com.abelnumberi.galericandi.repository.TempleRepository
import com.abelnumberi.galericandi.utils.ImageUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class TempleViewModel(private val repository: TempleRepository) : ViewModel() {

    fun getTemples(userId: String): Flow<List<Temple>> = repository.getLocalTemples(userId)

    fun deleteAllLocal() {
        viewModelScope.launch {
            repository.deleteAllLocal()
        }
    }

    fun saveTemple(
        temple: Temple,
        localImageUri: Uri?,
        context: Context,
        isEdit: Boolean,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                var finalImageUrl = temple.imageUrl
                var localFileToUpload: File? = null

                if (localImageUri != null) {
                    val cachedUri = ImageUtils.copyUriToInternalStorage(context, localImageUri, temple.id)
                    if (cachedUri != null) {
                        finalImageUrl = cachedUri.toString()
                        localFileToUpload = File(cachedUri.path ?: "")
                    }
                }

                val isOnline = isNetworkAvailable(context)
                if (isOnline && (localFileToUpload != null || !finalImageUrl.startsWith("file://"))) {
                    try {
                        if (localFileToUpload != null && localFileToUpload.exists()) {
                            val requestFile = localFileToUpload.asRequestBody("image/*".toMediaTypeOrNull())
                            val body = MultipartBody.Part.createFormData("image", localFileToUpload.name, requestFile)
                            
                            val uploadResponse = ApiConfig.imgBBApiService.uploadImage(
                                apiKey = com.abelnumberi.galericandi.BuildConfig.IMGBB_API_KEY,
                                image = body
                            )
                            
                            if (uploadResponse.success && uploadResponse.data != null) {
                                finalImageUrl = uploadResponse.data.url
                                localFileToUpload.delete()
                                localFileToUpload = null
                            }
                        }

                        val templeToSave = temple.copy(imageUrl = finalImageUrl)
                        if (isEdit) {
                            ApiConfig.templeApiService.updateTemple(temple.id, templeToSave)
                        } else {
                            ApiConfig.templeApiService.createTemple(templeToSave)
                        }

                        repository.insertLocal(templeToSave)
                        onComplete()
                        return@launch
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val offlineTemple = temple.copy(imageUrl = finalImageUrl)
                repository.insertLocal(offlineTemple)
                onComplete()

            } catch (e: Exception) {
                e.printStackTrace()
                onComplete()
            }
        }
    }

    fun deleteTemple(temple: Temple, context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteTemple(temple)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    context,
                    context.getString(R.string.err_delete_offline),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun syncOfflineTemples(context: Context, userId: String) {
        viewModelScope.launch {
            repository.syncOfflineTemples(context, userId)
        }
    }

    fun fetchTemples(userId: String) {
        viewModelScope.launch {
            repository.fetchTemplesFromServer(userId)
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

class TempleViewModelFactory(private val repository: TempleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TempleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TempleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
