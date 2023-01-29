package com.example.saveplaces

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saveplaces.activities.AddYourPlace
import com.example.saveplaces.activities.MainActivity
import com.example.saveplaces.database.SavePlacesEntity
import com.example.saveplaces.databinding.ItemSavePlaceBinding

class SavePlacesAdapter(
    private val context: Context,
    private val list: ArrayList<SavePlacesEntity>
) : RecyclerView.Adapter<SavePlacesAdapter.ViewHolder>() {

    private var onClickListener: SavePlacesAdapter.OnClickListener? = null

    inner class ViewHolder(val binding: ItemSavePlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val ivPlaceImage = binding.ivPlaceImage
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
        // TODO( Add var for date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSavePlaceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    fun setOnClickListener(onClickListener: SavePlacesAdapter.OnClickListener) {
        this.onClickListener = onClickListener
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddYourPlace::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun removeAt(position: Int) : SavePlacesEntity{
        notifyItemRemoved(position)
        return list[position]
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        holder.ivPlaceImage.setImageURI(Uri.parse(model.image))
        holder.tvTitle.text = model.title
        holder.tvDescription.text = model.description
        holder.binding.root.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, model)
            }
        }
    }

    interface OnClickListener {
        fun onClick(position: Int, model: SavePlacesEntity)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}






