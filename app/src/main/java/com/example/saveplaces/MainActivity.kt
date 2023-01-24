package com.example.saveplaces

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.saveplaces.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        binding?.fabAddHappyPlace?.setOnClickListener {
            val intent = Intent(this, AddYourPlace::class.java)
            startActivity(intent)
        }
    }
}