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

    // initializing static class properties
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

    private fun initViews() {
        fromDate = arguments?.getString(Constants.FROM) ?: ""
        toDate = arguments?.getString(Constants.TO) ?: ""
        countryName = arguments?.getString(Constants.COUNTRY_NAME) ?: ""
        if (!countryName.isBlank() && !fromDate.isBlank() && !toDate.isBlank()){
            getCountryDetails()
            fragment_country_details_title?.text = getString(R.string.results, countryName ,fromDate.dateToStringFormat(), toDate.dateToStringFormat())
        }else fragment_country_details_title?.text = "Something went wrong please try again"
    }

    private fun getCountryDetails() {
        coronaViewModel.getCountryBy(countryName, fromDate, toDate)
            .observe(viewLifecycleOwner, Observer { countries ->
                when (countries) {
                    is StatefulData.Loading -> fragment_country_details_progress_bar?.show()
                    is StatefulData.Success -> {
                        fragment_country_details_progress_bar?.show(false)
                        showData(countries.data)
                    }
                    is StatefulData.Error -> fragment_country_details_progress_bar?.show(false)
                }
            })
    }

    private fun showData(countries: List<CountryDetails>) {
        val pie = AnyChart.pie()

        val confirmed = countries.lastOrNull()?.confirmed
        val recovered = countries.lastOrNull()?.recovered
        val deaths = countries.lastOrNull()?.deaths

        pie.data(
            listOf<DataEntry>(
                ValueDataEntry("Confirmed", confirmed),
                ValueDataEntry("Recovered", recovered),
                ValueDataEntry("Death", deaths)
            )
        )
        fragment_country_details_any_chart_view?.setChart(pie)
    }
}