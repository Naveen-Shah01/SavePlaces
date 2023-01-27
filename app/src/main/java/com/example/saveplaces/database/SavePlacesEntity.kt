package com.example.saveplaces.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tbl-save-place")
data class SavePlacesEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var title: String,
    var image: String,
    var description: String,
    var date: String,
    var location: String,
    var latitude: Double,
    var longitude: Double,
    ) : Serializable