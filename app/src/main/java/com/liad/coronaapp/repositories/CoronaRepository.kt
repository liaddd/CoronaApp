package com.liad.coronaapp.repositories

import co.climacell.statefulLiveData.core.*
import com.liad.coronaapp.models.Country
import com.liad.coronaapp.models.CountryDetails
import com.liad.coronaapp.server_connection.CoronaRequests
import retrofit2.Retrofit

class CoronaRepository(retrofit: Retrofit) {

    private var apiRequest: CoronaRequests = retrofit.create(CoronaRequests::class.java)

    /**
     * @return List<Country> (as StatefulLiveData) - NOT FILTERED
     * */

    fun getCountries() : StatefulLiveData<List<Country>>{
        val countriesMutableStatefulLiveData = MutableStatefulLiveData<List<Country>>()
        countriesMutableStatefulLiveData.putLoading()

        apiRequest.getCountries().observeOnce(androidx.lifecycle.Observer { countries ->
            if (countries is StatefulData.Success){
                countriesMutableStatefulLiveData.putData(countries.data)
            }
        })

        return  countriesMutableStatefulLiveData
    }

    /**
     * @param countryName used to search for specific country
     * @param fromDate used to filter by starting date
     * @param toDate used to filter by end date
     * @return List<Country> (wrap with StatefulLiveData) filtered by country name and fromDate - toDate range
     * */

    fun getCountryBy(countryName : String , fromDate : String , toDate : String) : StatefulLiveData<List<CountryDetails>>{

        val countriesMutableStatefulLiveData = MutableStatefulLiveData<List<CountryDetails>>()
        countriesMutableStatefulLiveData.putLoading()

        apiRequest.getCountryBy(countryName , fromDate , toDate).observeOnce(androidx.lifecycle.Observer { countries ->
            if (countries is StatefulData.Success){
                countriesMutableStatefulLiveData.putData(countries.data)
            }
        })
        return  countriesMutableStatefulLiveData
    }
}