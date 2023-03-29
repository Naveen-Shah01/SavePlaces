package com.example.saveplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.saveplaces.R
import com.example.saveplaces.database.SavePlacesEntity
import com.example.saveplaces.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private var binding: ActivityMapBinding? = null
    private var savePlaceMapDetails: SavePlacesEntity? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            savePlaceMapDetails =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as SavePlacesEntity
        }

        setSupportActionBar(binding?.toolbarMap)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savePlaceMapDetails != null) supportActionBar!!.title = savePlaceMapDetails!!.title

        binding?.toolbarMap?.setNavigationOnClickListener {
            onBackPressed()
        }


        val supportMapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(savePlaceMapDetails!!.latitude, savePlaceMapDetails!!.longitude)
        googleMap.addMarker(
            MarkerOptions().position(position).title(savePlaceMapDetails!!.location)
        )
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position,17.5f)
        googleMap.animateCamera(newLatLngZoom)
    }
}

