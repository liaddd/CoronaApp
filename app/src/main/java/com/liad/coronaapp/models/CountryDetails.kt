package com.liad.coronaapp.models

import com.google.gson.annotations.SerializedName

data class CountryDetails(
    @SerializedName("Country") val country: String? = null,
    @SerializedName("CountryCode") val countryCode: String? = null,
    @SerializedName("Province") val province: String? = null,
    @SerializedName("City") val city: String? = null,
    @SerializedName("CityCode") val cityCode: String? = null,
    @SerializedName("Lat") val lat: String? = null,
    @SerializedName("Lon") val lon: String? = null,
    @SerializedName("Confirmed") val confirmed: Int = 0,
    @SerializedName("Recovered") val recovered: Int = 0,
    @SerializedName("Active") val active: Int = 0,
    @SerializedName("Deaths") val deaths: Int = 0,
    @SerializedName("Date") val date: String? = null
)

