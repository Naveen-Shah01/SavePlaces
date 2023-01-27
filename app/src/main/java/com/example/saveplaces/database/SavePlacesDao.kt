package com.example.saveplaces.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavePlacesDao {
    //create a suspend insert function for saving an entry
    @Insert
    suspend fun insert(savePlacesEntity: SavePlacesEntity)

    // create a suspend update function for updating an existing entry
    @Update
    suspend fun update(savePlacesEntity: SavePlacesEntity)

    // create a suspend delete function for deleting an existing entry
    @Delete
    suspend fun delete(savePlacesEntity: SavePlacesEntity)

    //create a function to read all places, this returns a Flow
    @Query("SELECT *FROM `tbl-save-place`")
    fun fetchAllPlace(): Flow<List<SavePlacesEntity>>

    // create a function to read one place, this returns a Flow
    @Query("SELECT * FROM `tbl-save-place` WHERE id=:id")
    fun fetchPlaceById(id: Int): Flow<List<SavePlacesEntity>>
}


