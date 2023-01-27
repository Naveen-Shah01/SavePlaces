package com.example.saveplaces.activities

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.saveplaces.database.SavePlacesEntity
import com.example.saveplaces.databinding.ActivitySavePlacesDetailBinding

class SavePlacesDetailActivity : AppCompatActivity() {

    private var binding: ActivitySavePlacesDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavePlacesDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarSavePlaceDetail)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolbarSavePlaceDetail?.setNavigationOnClickListener {
            onBackPressed()
        }

        fillDetails()
    }

    private fun fillDetails() {
        lateinit var savePlaceDetailModel: SavePlacesEntity
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            savePlaceDetailModel =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as SavePlacesEntity
        }
        binding?.tvTitle?.text = savePlaceDetailModel.title
        binding?.ivPlaceImage?.setImageURI(Uri.parse(savePlaceDetailModel.image))
        binding?.tvDescription?.text = savePlaceDetailModel.description
        binding?.tvLocation?.text = savePlaceDetailModel.location

//        binding?.btnViewOnMap?.setOnClickListener {
//            val intent = Intent(this,MapActivity::class.java)
//            intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,happyPlaceDetailModel)
//            startActivity(intent)
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}

