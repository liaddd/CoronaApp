package com.liad.coronaapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import co.climacell.statefulLiveData.core.StatefulData
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.liad.coronaapp.R
import com.liad.coronaapp.models.CountryDetails
import com.liad.coronaapp.utils.Constants
import com.liad.coronaapp.utils.extension.dateToStringFormat
import com.liad.coronaapp.utils.extension.show
import com.liad.coronaapp.viewmodels.CoronaViewModel
import kotlinx.android.synthetic.main.fragment_country_details.*
import org.koin.android.ext.android.inject

class CountryDetailsFragment : Fragment() {

    // Initializing static class properties
    companion object {
        fun newInstance() = CountryDetailsFragment()
    }

    private val coronaViewModel: CoronaViewModel by inject()
    private lateinit var countryName: String
    private lateinit var fromDate: String
    private lateinit var toDate: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_country_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    // Initializing Fragment Views
    private fun initViews() {
        fromDate = arguments?.getString(Constants.FROM) ?: ""
        toDate = arguments?.getString(Constants.TO) ?: ""
        countryName = arguments?.getString(Constants.COUNTRY_NAME) ?: ""
        // check if all required fields are Initialized
        if (countryName.isNotBlank() && fromDate.isNotBlank() && toDate.isNotBlank()){
            getCountryDetails()
            fragment_country_details_title?.text = getString(R.string.results, countryName ,fromDate.dateToStringFormat(), toDate.dateToStringFormat())
        }
        else fragment_country_details_title?.text = getString(R.string.unrecognized_error)
    }

    // Fetching Country details from api
    private fun getCountryDetails() {
        coronaViewModel.getCountryBy(countryName, fromDate, toDate)
            .observe(viewLifecycleOwner, Observer { countries ->
                when (countries) {
                    is StatefulData.Loading -> fragment_country_details_progress_bar?.show()
                    is StatefulData.Success -> {
                        fragment_country_details_progress_bar?.show(false)
                        if (countries.data.isNullOrEmpty()) fragment_country_details_empty_state_container?.show()
                        else showData(countries.data)
                    }
                    is StatefulData.Error -> fragment_country_details_progress_bar?.show(false)
                }
            })
    }

    // Display Country details in Chart
    private fun showData(countries: List<CountryDetails>) {
        val pie = AnyChart.pie()

        val confirmed = countries.lastOrNull()?.confirmed ?: 0
        val recovered = countries.lastOrNull()?.recovered ?: 0
        val deaths = countries.lastOrNull()?.deaths ?: 0

        // validate valuable values
        if (confirmed > 0 && recovered > 0 && deaths > 0){
            pie.data(
                listOf<DataEntry>(
                    ValueDataEntry("Confirmed", confirmed),
                    ValueDataEntry("Recovered", recovered),
                    ValueDataEntry("Death", deaths)
                )
            )
            fragment_country_details_any_chart_view?.setChart(pie)
        }else{
            fragment_country_details_empty_state_container?.show()
        }
    }
}