package com.abelnumberi.galericandi.repository

import android.content.Context
import android.net.Uri
import com.abelnumberi.galericandi.BuildConfig
import com.abelnumberi.galericandi.database.Temple
import com.abelnumberi.galericandi.database.TempleDao
import com.abelnumberi.galericandi.network.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class TempleRepository(private val templeDao: TempleDao) {

    fun getLocalTemples(userId: String): Flow<List<Temple>> = templeDao.getAllTemples(userId)

    suspend fun insertLocal(temple: Temple) {
        withContext(Dispatchers.IO) {
            templeDao.insertTemple(temple)
        }
    }

    suspend fun deleteLocal(temple: Temple) {
        withContext(Dispatchers.IO) {
            templeDao.deleteTemple(temple)
        }
    }

    suspend fun fetchTemplesFromServer(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                val serverTemples = ApiConfig.templeApiService.getTemples(userId)
                templeDao.deleteAll()
                serverTemples.forEach { templeDao.insertTemple(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteTemple(temple: Temple) {
        withContext(Dispatchers.IO) {
            // Attempt delete from remote server first
            ApiConfig.templeApiService.deleteTemple(temple.id)
            // Only delete locally if the remote request was successful (doesn't throw exception)
            templeDao.deleteTemple(temple)
        }
    }

    suspend fun syncOfflineTemples(context: Context, userId: String) {
        withContext(Dispatchers.IO) {
            try {
                // Fetch the current temple list on the server
                val serverTemples = ApiConfig.templeApiService.getTemples(userId)
                
                // Get all local temples
                val localTemples = templeDao.getAllTemplesList(userId)
                
                for (temple in localTemples) {
                    if (temple.imageUrl.startsWith("file://")) {
                        try {
                            val uri = Uri.parse(temple.imageUrl)
                            val file = File(uri.path ?: "")
                            if (file.exists()) {
                                // Upload local image to ImgBB
                                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
                                
                                val uploadResponse = ApiConfig.imgBBApiService.uploadImage(
                                    apiKey = BuildConfig.IMGBB_API_KEY,
                                    image = body
                                )
                                
                                if (uploadResponse.success && uploadResponse.data != null) {
                                    val remoteUrl = uploadResponse.data.url
                                    val updatedTemple = temple.copy(imageUrl = remoteUrl)
                                    
                                    // Check if this temple already exists on the MockAPI server
                                    val existsOnServer = serverTemples.any { it.id == temple.id }
                                    if (existsOnServer) {
                                        ApiConfig.templeApiService.updateTemple(temple.id, updatedTemple)
                                    } else {
                                        ApiConfig.templeApiService.createTemple(updatedTemple)
                                    }
                                    
                                    // Delete local cache image file after successful upload to save space
                                    file.delete()
                                    
                                    // Update local room database with remote url
                                    templeDao.insertTemple(updatedTemple)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Skip to next temple if this one fails to sync
                        }
                    }
                }
                
                // Fetch final list to resolve any outstanding server-side additions/removals
                fetchTemplesFromServer(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteAllLocal() {
        withContext(Dispatchers.IO) {
            templeDao.deleteAll()
        }
    }
}
