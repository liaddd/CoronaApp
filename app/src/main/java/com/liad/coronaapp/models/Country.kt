package com.liad.coronaapp.models

import com.google.gson.annotations.SerializedName

data class Country(
    @SerializedName("Country") var name: String? = null,
    @SerializedName("Slug") var slug: String? = null,
    @SerializedName("ISO2") var iSO2: String? = null,
    var isSelected: Boolean = false
)