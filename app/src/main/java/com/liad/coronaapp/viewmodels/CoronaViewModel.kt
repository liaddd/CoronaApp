package com.liad.coronaapp.viewmodels

import androidx.lifecycle.ViewModel
import com.liad.coronaapp.repositories.CoronaRepository

class CoronaViewModel(private val repository: CoronaRepository) : ViewModel() {

    fun getCountries() = repository.getCountries()

    fun getCountryBy(countryName: String, fromDate: String, toDate: String) = repository.getCountryBy(countryName, fromDate, toDate)
}