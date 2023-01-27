package com.example.saveplaces

import android.app.Application
import com.example.saveplaces.database.SavePlacesDatabase

class SavePlacesApp : Application() {
    val db by lazy {
        SavePlacesDatabase.getInstance(this)
    }
}