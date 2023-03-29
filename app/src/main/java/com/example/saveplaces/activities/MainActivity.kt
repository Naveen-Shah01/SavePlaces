package com.example.saveplaces.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saveplaces.R
import com.example.saveplaces.SavePlacesAdapter
import com.example.saveplaces.SavePlacesApp
import com.example.saveplaces.Utils.SwipeToDeleteCallback
import com.example.saveplaces.Utils.SwipeToEditCallback
import com.example.saveplaces.database.SavePlacesDao
import com.example.saveplaces.database.SavePlacesEntity
import com.example.saveplaces.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    // refactor all the names
    private var binding: ActivityMainBinding? = null
    private lateinit var savePlacesDao: SavePlacesDao

    companion object {
        var EXTRA_PLACE_DETAILS = "extra_place_details"
        private var ADD_PLACE_ACTIVITY_REQUEST_CODE = 2
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)



        binding?.fabSavePlace?.setOnClickListener {
            val intent = Intent(this, AddYourPlace::class.java)
            startActivity(intent)
        }

        savePlacesDao = (application as SavePlacesApp).db.savePlacesDao()

        lifecycleScope.launch {
            savePlacesDao.fetchAllPlace().collect {
                val list = ArrayList(it)
                setUpSavePlaceRecyclerView(list)
            }
        }

    }


    private fun setUpSavePlaceRecyclerView(savePlaceList: ArrayList<SavePlacesEntity>) {
        if (savePlaceList.isNotEmpty()) {
            val placesAdapter = SavePlacesAdapter(this, savePlaceList)
            binding?.rvSavePlacesList?.layoutManager = LinearLayoutManager(this@MainActivity)
            binding?.rvSavePlacesList?.setHasFixedSize(true)
            binding?.rvSavePlacesList?.adapter = placesAdapter

            binding?.rvSavePlacesList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE

            placesAdapter.setOnClickListener(object : SavePlacesAdapter.OnClickListener {
                override fun onClick(position: Int, model: SavePlacesEntity) {
                    val intent = Intent(this@MainActivity, SavePlacesDetailActivity::class.java)
                    intent.putExtra(EXTRA_PLACE_DETAILS, model)
                    startActivity(intent)
                }
            })


            // edit swipe
            val editSwipeHandler = object : SwipeToEditCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter = binding?.rvSavePlacesList?.adapter as SavePlacesAdapter
                    adapter.notifyEditItem(
                        this@MainActivity, viewHolder.adapterPosition,
                        ADD_PLACE_ACTIVITY_REQUEST_CODE
                    )
                }
            }
            val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
            editItemTouchHelper.attachToRecyclerView(binding?.rvSavePlacesList)


            // swipe to delete
            val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter = binding?.rvSavePlacesList?.adapter as SavePlacesAdapter
                    val itemToDelete = adapter.removeAt(viewHolder.adapterPosition)
                    deleteFromDatabase(itemToDelete)
                    Snackbar.make(binding?.fabSavePlace!!,"Saved Place Deleted",Snackbar.LENGTH_LONG).apply {

                        setAnchorView(binding?.fabSavePlace!!)
                        setAction("UNDO"){
                            lifecycleScope.launch {
                                savePlacesDao.insert(itemToDelete)
                            }
                        }


                    }.show()
                }
            }
            val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
            deleteItemTouchHelper.attachToRecyclerView(binding?.rvSavePlacesList)

        } else {
            binding?.rvSavePlacesList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    private fun deleteFromDatabase(position: SavePlacesEntity) {

        lifecycleScope.launch {
            savePlacesDao.delete(position)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}