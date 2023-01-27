package com.example.saveplaces.activities

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.saveplaces.R
import com.example.saveplaces.SavePlacesApp
import com.example.saveplaces.database.SavePlacesDao
import com.example.saveplaces.database.SavePlacesEntity
import com.example.saveplaces.databinding.ActivityAddYourPlaceBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddYourPlace : AppCompatActivity() {

    private var binding: ActivityAddYourPlaceBinding? = null
    private var saveImageUri: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var savePlaceEditDetail: SavePlacesEntity ?=null

    companion object {
        private const val IMAGE_DIRECTORY = "SavePlacesImages"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddYourPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        val savePlaceDao = (application as SavePlacesApp).db.savePlacesDao()

        setSupportActionBar(binding?.toolbarAddPlace)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        // for update feature
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            savePlaceEditDetail =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as SavePlacesEntity

            updateDetails()
        }



        binding?.etDate?.setOnClickListener {
            materialDatePickerDialog()
        }
        binding?.tvAddImage?.setOnClickListener {
            showPermissionDialog()
        }
        binding?.btnSave?.setOnClickListener {
            addUpdatePlace(savePlaceDao)
        }


    }

    private fun updateDetails() {
        supportActionBar?.title = "EDIT YOUR PLACE"
        binding?.etTitle?.setText(savePlaceEditDetail!!.title)
        binding?.etDescription?.setText(savePlaceEditDetail!!.description)
        binding?.etDate?.setText(savePlaceEditDetail!!.date)
        binding?.etLocation?.setText(savePlaceEditDetail!!.location)
        mLatitude = savePlaceEditDetail!!.latitude
        mLongitude = savePlaceEditDetail!!.longitude

        saveImageUri = Uri.parse(savePlaceEditDetail!!.image)

        binding?.ivPlaceImage?.setImageURI(saveImageUri)
        binding?.btnSave?.text = "UPDATE"
    }

    // gallery launcher
    private val openGalleryLauncher: ActivityResultLauncher<Intent> = // Intent type launcher
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                // will set image to image view(in our background)
                val contentURI = result.data?.data
                try {
                    binding?.ivPlaceImage?.setImageURI(contentURI)
                    val inputStream = contentURI?.let { this.contentResolver.openInputStream(it) }
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    saveImageUri = saveImageToInternalStorage(bitmap)

                    Log.e("Saved image: ", "Path :: $saveImageUri")
                    Toast.makeText(this@AddYourPlace, "$saveImageUri", Toast.LENGTH_LONG)
                        .show()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@AddYourPlace,
                        "Failed to load image from gallery",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

    // Camera launcher
    private val cameraLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val thumbNail: Bitmap = result.data?.extras!!.get("data") as Bitmap

                saveImageUri = saveImageToInternalStorage(thumbNail)

                binding?.ivPlaceImage?.setImageURI(saveImageUri)
            }
        }

    //Variable Checking for permission that if permission is granted then only allow camera access otherwise Toast.
    private val requestPermission: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // If the permission is granted then go to the camera with an intent
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(intent)
            } else {
                // If the permission is denied then show another dialog
                Toast.makeText(
                    this@AddYourPlace,
                    "Oops, you just denied the permission.",
                    Toast.LENGTH_LONG
                ).show()

                showRationalDialog(
                    "Save Places", "To use this feature you need to allow the access to the camera"
                )
            }
        }


    private fun showPermissionDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select Photo from gallery", "Capture Photo from camera")

        pictureDialog.setItems(pictureDialogItems) { _, which ->
            when (which) {
                0 -> chosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }


    private fun takePhotoFromCamera() {
        // If the user denied the permission earlier than show Rational dialog with the text
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            showRationalDialog(
                "Save Places", "oops"
            )
            // If the user haven't responded yet than request permission for camera
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    // Function for showing rational dialog
    private fun showRationalDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(message).setPositiveButton("Change Settings") { _, _ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                //Here URI is giving reference to the settings page.
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }.show()
        builder.create().show()
    }

    //Here we are using Dexter library for permission management.
    private fun chosePhotoFromGallery() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    // using intent to open gallery
                    val galleryIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    // start the intent+ set image to background
                    openGalleryLauncher.launch(galleryIntent)
                } else {
                    Toast.makeText(this@AddYourPlace, "Please Allow ", Toast.LENGTH_LONG).show()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest?>?, token: PermissionToken?
            ) {
                showRationalDialog(
                    "Save Places", "Permissions are denied open settings and allow permission"
                )
            }
        }).onSameThread().check()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }


    private fun addPlace(savePlacesDao: SavePlacesDao) {
        val title = binding?.etTitle?.text.toString()
        val image = saveImageUri.toString()
        val description = binding?.etDescription?.text.toString()
        val location = binding?.etLocation?.text.toString()
        val date = binding?.etDate?.text.toString()
        if (title.isEmpty() || saveImageUri == null || description.isEmpty() || location.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Enter all the fields", Toast.LENGTH_SHORT).show()
        } else {
            // run on coroutine
            lifecycleScope.launch {
                savePlacesDao.insert(
                    SavePlacesEntity(
                        title = title,
                        image = image,
                        description = description,
                        location = location,
                        date = date,
                        latitude = mLatitude,
                        longitude = mLongitude
                    )
                )
                Toast.makeText(
                    applicationContext, "Data saved", Toast.LENGTH_SHORT
                ).show()
            }
            finish()
        }
    }

    private fun addUpdatePlace(savePlacesDao: SavePlacesDao) {
        val title = binding?.etTitle?.text.toString()
        val image = saveImageUri.toString()
        val description = binding?.etDescription?.text.toString()
        val location = binding?.etLocation?.text.toString()
        val date = binding?.etDate?.text.toString()
        if (title.isEmpty() || saveImageUri == null || description.isEmpty() || location.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Enter all the fields", Toast.LENGTH_SHORT).show()
        } else {
            if (savePlaceEditDetail == null) {
                // run on coroutine
                lifecycleScope.launch {
                    savePlacesDao.insert(
                        SavePlacesEntity(
                            title = title,
                            image = image,
                            description = description,
                            location = location,
                            date = date,
                            latitude = mLatitude,
                            longitude = mLongitude
                        )
                    )
                    Toast.makeText(
                        applicationContext, "data saved", Toast.LENGTH_SHORT
                    ).show()
                }

            } else {
                lifecycleScope.launch {
                    savePlacesDao.update(
                        SavePlacesEntity( savePlaceEditDetail!!.id,
                            title = title,
                            image = image,
                            description = description,
                            location = location,
                            date = date,
                            latitude = mLatitude,
                            longitude = mLongitude
                        )
                    )
                    Toast.makeText(
                        applicationContext, "Data Updated", Toast.LENGTH_SHORT
                    ).show()
                }
            }
            finish()
        }
    }



    /** For Calender */
    private fun materialDatePickerDialog() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setTheme(R.style.MaterialCalendarTheme).build()

        datePicker.show(supportFragmentManager, "SELECT DATE")

        datePicker.addOnPositiveButtonClickListener {
            // formatting date in dd-mm-yyyy format.
            val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = dateFormatter.format(Date(it))
            binding?.etDate?.setText(date.toString())
            Toast.makeText(this, date, Toast.LENGTH_SHORT).show()
        }
        // event when cancelled is clicked
        datePicker.addOnNegativeButtonClickListener {
            datePicker.dismiss()
        }

        // event when back button is pressed
        datePicker.addOnCancelListener {
            datePicker.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}