package com.liad.coronaapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.liad.coronaapp.R
import com.liad.coronaapp.models.Country
import com.liad.coronaapp.utils.extension.clearAndAddAll

class CountryAdapter : RecyclerView.Adapter<CountryAdapter.ViewHolder>() {

    private var countries = mutableListOf<Country>()
    var listener: ICountryClickedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.country_list_item, parent, false)
        )
    }

    override fun getItemCount(): Int = countries.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val country = countries[position]

        handleCountryClick(country , holder)
        country.isSelected = country.isSelected
        holder.countryName.text = country.name
        holder.itemView.setOnClickListener {
            handleAlreadySelected()
            country.isSelected = !country.isSelected
            handleCountryClick(country, holder)
            listener?.onClick(country)
        }
    }

    private fun handleCountryClick(country: Country, holder: ViewHolder) {
        if (country.isSelected) {
            holder.itemView.background = ResourcesCompat.getDrawable(holder.itemView.resources, R.drawable.black_background_rounded_corners, null)
            holder.countryName.setTextColor(Color.WHITE)
        } else {
            holder.itemView.background = ResourcesCompat.getDrawable(holder.itemView.resources, R.drawable.white_background_black_rounded_corners, null)
            holder.countryName.setTextColor(Color.BLACK)
        }
    }

    // Enable only one selection from countries
    private fun handleAlreadySelected() {
        countries.forEach {country ->
            if (country.isSelected){
                country.isSelected = false
            }
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val countryName = itemView.findViewById(R.id.country_list_item_name_text_view) as TextView
    }

    fun setCountries(newCountries: List<Country>) {
        countries.clearAndAddAll(newCountries)
        notifyDataSetChanged()
    }

    interface ICountryClickedListener {
        fun onClick(country: Country)
    }
}