package com.liad.coronaapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.climacell.statefulLiveData.core.StatefulData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import com.liad.coronaapp.R
import com.liad.coronaapp.adapters.CountryAdapter
import com.liad.coronaapp.models.Country
import com.liad.coronaapp.utils.Constants
import com.liad.coronaapp.utils.Constants.LOCATION_REQUEST_CODE
import com.liad.coronaapp.utils.extension.*
import com.liad.coronaapp.viewmodels.CoronaViewModel
import kotlinx.android.synthetic.main.fragment_countries.*
import org.koin.android.ext.android.inject
import java.util.*

class CountriesFragment : Fragment(), View.OnClickListener, DatePickerDialog.OnDateSetListener {

    // initializing static class properties
    companion object {
        fun newInstance() = CountriesFragment()
    }

    private val countryAdapter = CountryAdapter().apply { listener = getCountryClickedListener() }
    private val coronaViewModel: CoronaViewModel by inject()
    private val calendar = Calendar.getInstance()
    private var selectedCountry: Country? = null
    private var selectedDatePickerTag = Constants.FROM
    private lateinit var fromDateTIE: TextInputEditText
    private lateinit var toDateTIE: TextInputEditText
    private var fromDate: String = ""
    private var toDate: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_countries, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        setListeners()
        // remove comment below to get all countries from API at initialization instead of 3 hard-coded countries
        //setObservers()
    }

    // Initializing Fragment Views
    private fun initViews() {
        context?.let {
            // initialize RecyclerView
            fragment_countries_recycler_view?.apply {
                adapter = countryAdapter
                layoutManager = LinearLayoutManager(it, RecyclerView.VERTICAL, false)
            }
        }

        // initialize 3 HARD-CODED countries
        countryAdapter.setCountries(
            listOf(
                Country(Constants.ISRAEL, Constants.ISRAEL_SLUG),
                Country(Constants.SOUTH_AFRICA, Constants.SOUTH_AFRICA_SLUG),
                Country(Constants.COLOMBIA, Constants.COLOMBIA_SLUG)
            )
        )

        fromDateTIE = fragment_countries_from_text_input_edit_text
        toDateTIE = fragment_countries_to_text_input_edit_text
    }

    // Set listeners for relevant views
    private fun setListeners() {
        fragment_countries_from_text_input_edit_text?.setOnClickListener(this)
        fragment_countries_to_text_input_edit_text?.setOnClickListener(this)
        fragment_countries_submit_button?.setOnClickListener(this)
        fragment_countries_my_country_button?.setOnClickListener(this)
        fragment_countries_check_for_nearby_button?.setOnClickListener(this)
    }

    // Setting Observers for 'Get all Countries' Api request (remove comment in onCreate function to use)
    private fun setObservers() {
        coronaViewModel.getCountries().observe(viewLifecycleOwner, Observer { countries ->
            when (countries) {
                is StatefulData.Loading -> fragment_countries_progress_bar?.show()
                is StatefulData.Success -> {
                    fragment_countries_progress_bar?.show(false)
                    countryAdapter.setCountries(countries.data)
                }
                is StatefulData.Error -> {
                }
            }
        })
    }

    // Get Selected Country from list
    private fun getCountryClickedListener() = object : CountryAdapter.ICountryClickedListener {
        override fun onClick(country: Country) { selectedCountry = country }
    }

    // Handle buttons on click
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.fragment_countries_from_text_input_edit_text -> showDatePickerDialog(Constants.FROM)
            R.id.fragment_countries_to_text_input_edit_text -> showDatePickerDialog(Constants.TO)
            R.id.fragment_countries_submit_button -> if(isInputsValid() && selectedCountry != null) showCountryDetails() else showRelevantError()
            R.id.fragment_countries_my_country_button -> checkForLocationPermission()
            R.id.fragment_countries_check_for_nearby_button -> { showNearbyBTDevices()}
        }
    }

    // Check if user enabled access to Location
    private fun checkForLocationPermission() {
        context?.let {
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) getCurrentLocation()
            else askLocationPermission()
        }
    }

    // Ask for location permission (after inputs validation)
    private fun askLocationPermission() {
        if (!isInputsValid()){
            context?.let { toast(it, getString(R.string.invalid_inputs)) }
            return
        }
        activity?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)) showNoGpsDialog()
                else requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            }else{
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            }
        }
    }

    // Navigate to CountryDetailFragment to show fetched country data
    private fun showCountryDetails() {
        activity?.let {
            val countryDetailsFragment = CountryDetailsFragment.newInstance()

            val bundle = Bundle().apply {
                putString(Constants.FROM, fromDate)
                putString(Constants.TO, toDate)
                putString(Constants.COUNTRY_NAME, selectedCountry?.slug)
            }

            countryDetailsFragment.arguments = bundle
            changeFragment(it.supportFragmentManager, R.id.main_activity_frame_layout, countryDetailsFragment, true)
        }
    }

    // Get Location Lat & Lon from LocationService
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        // validating relevant fields
        if (!isInputsValid()){
            showRelevantError()
            return
        }

        fragment_countries_progress_bar?.show()
        val locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        activity?.let {
            if(it.isDeviceLocationEnabled()){
                LocationServices.getFusedLocationProviderClient(it).requestLocationUpdates(locationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        super.onLocationResult(locationResult)
                        fragment_countries_progress_bar?.show(false)
                        if (locationResult != null) {
                            val geoCoder = Geocoder(it, Locale.getDefault())
                            val addresses = geoCoder.getFromLocation(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude, 1)
                            if (addresses.size > 0) {
                                val countrySlug = addresses[0].countryName.decapitalize()
                                selectedCountry = Country(slug = countrySlug)
                                showCountryDetails()
                            }
                        }
                        LocationServices.getFusedLocationProviderClient(it).removeLocationUpdates(this)
                    }
                }, Looper.getMainLooper())
            }else{
                showNoGpsDialog()
            }
        }

    }

    // Show GPS needed dialog for users that denied access before
    private fun showNoGpsDialog() {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.no_gps)).setMessage(getString(R.string.activate_gps))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_REQUEST_CODE)
                }.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }

    }

    // Navigate to NearbyInfectedFragment
    private fun showNearbyBTDevices() {
        activity?.let {  changeFragment(it.supportFragmentManager , R.id.main_activity_frame_layout , NearbyInfectedFragment.newInstance() , true)}
    }

    // Check if all necessary input was given
    private fun isInputsValid() : Boolean = fromDateTIE.isValid() && toDateTIE.isValid() && fromDateTIE.text.toString().isBefore(toDateTIE.text.toString())

    // Validate inputs and show relevant error
    private fun showRelevantError() {
        when {
            fromDateTIE.text.isNullOrEmpty() -> {
                fromDateTIE.error = getString(R.string.from_date_invalid)
                toDateTIE.error = null
            }
            toDateTIE.text.isNullOrEmpty() -> {
                toDateTIE.error = getString(R.string.to_date_invalid)
                fromDateTIE.error = null
            }
            selectedCountry == null -> {
                context?.let { toast(it , getString(R.string.no_selected_country) , Toast.LENGTH_LONG) }
                fromDateTIE.error = null
                toDateTIE.error = null
            }
            !fromDateTIE.text.toString().isBefore(toDateTIE.text.toString()) -> {
                toDateTIE.error = getString(R.string.to_date_invalid)
                context?.let { toast(it, getString(R.string.invalid_inputs) , Toast.LENGTH_LONG) }
            }
        }
    }

    // Show Date Picker Dialog
    private fun showDatePickerDialog(tag: String) {
        selectedDatePickerTag = tag
        activity?.let {
            DatePickerDialog(it, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONDAY), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    // onDateSet triggered while the user has selected a date and clicked "OK"
    override fun onDateSet(datePicker: DatePicker?, year: Int, month: Int, day: Int) {
        if (selectedDatePickerTag == Constants.FROM) {
            val selectedFromDate = "$day/0${month + 1}/$year"
            fromDate = selectedFromDate.toDateFormat()
            fragment_countries_from_text_input_edit_text.setText(fromDate.dateToStringFormat())
        } else {
            val selectedToDate = "$day/0${month + 1}/$year"
            toDate = selectedToDate.toDateFormat()
            fragment_countries_to_text_input_edit_text.setText(toDate.dateToStringFormat())
        }
    }

    // triggered after permission granted/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_REQUEST_CODE){
            getCurrentLocation()
        }
    }
}