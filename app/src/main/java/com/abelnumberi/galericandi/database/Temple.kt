package com.abelnumberi.galericandi.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "temples")
data class Temple(
    @PrimaryKey
    @Json(name = "id")
    val id: String,
    
    @Json(name = "userId")
    val userId: String,
    
    @Json(name = "name")
    val name: String,
    
    @Json(name = "location")
    val location: String,
    
    @Json(name = "description")
    val description: String,
    
    @Json(name = "imageUrl")
    val imageUrl: String
)
