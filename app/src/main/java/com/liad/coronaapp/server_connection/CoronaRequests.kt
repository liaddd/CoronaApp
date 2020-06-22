package com.liad.coronaapp.server_connection

import co.climacell.statefulLiveData.core.StatefulLiveData
import com.liad.coronaapp.models.Country
import com.liad.coronaapp.models.CountryDetails
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoronaRequests {

    @GET("countries")
    fun getCountries() : StatefulLiveData<List<Country>>

    @GET("country/{country}")
    fun getCountryBy(@Path("country") country : String , @Query("from") fromDate : String , @Query("to") toDate : String) : StatefulLiveData<List<CountryDetails>>

    @GET("total/country/{country}")
    fun getCountryByName(@Path("country") country : String) : StatefulLiveData<List<CountryDetails>>
}