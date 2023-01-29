package com.example.saveplaces.Utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.util.*


class GetAddressFromLatLng(
    context: Context, private val latitude: Double,
    private val longitude: Double ){

    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

    private lateinit var mAddressListener: AddressListener

    suspend fun launchBackgroundProcessForRequest() {
        val address=getAddress()

        withContext(Main){
            //switch to Main thread, cuz we're going to update the UI related values from here on
            // if we get a valid address
            if (address.isEmpty()) {
                mAddressListener.onError()
            } else {
                mAddressListener.onAddressFound(address)  //updaing UI
            }
        }
    }


    private fun getAddress():String{
        try {
            //there may be multiple locations/places associated with the lat and lng, we take the top/most relevant address
            val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                val address:Address=addressList[0]
                val sb=StringBuilder()
                for(i in 0..address.maxAddressLineIndex){  //Returns the largest index currently in use to specify an address line.
                    sb.append(address.getAddressLine(i)).append(",")
                }
                sb.deleteCharAt(sb.length-1)   //to remove the last " "

                return sb.toString()
            }
        }
        catch (e:Exception){
            Log.e("SavePlaces", "Unable connect to Geocoder")
        }
        return ""
    }


    fun setAddressListener(addressListener: AddressListener){  //to attach the listener to the class property
        this.mAddressListener=addressListener
    }


    /**
     * A interface for AddressListener which contains the function like success and error.
     */
    //can be defined anywhere
    interface AddressListener{
        fun onAddressFound(address:String)
        fun onError()
    }
}
// END