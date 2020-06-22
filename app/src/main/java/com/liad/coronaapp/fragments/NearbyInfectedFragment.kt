package com.liad.coronaapp.fragments

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.liad.coronaapp.R
import com.liad.coronaapp.utils.Constants.INFECTED_PERSON_MAC_ADDRESS
import com.liad.coronaapp.utils.Constants.LOCATION_REQUEST_CODE
import com.liad.coronaapp.utils.extension.isDeviceLocationEnabled
import com.liad.coronaapp.utils.extension.show
import com.liad.coronaapp.utils.extension.toast
import kotlinx.android.synthetic.main.fragment_nearby_infected.*

class NearbyInfectedFragment : Fragment() {

    // initializing static class properties
    companion object {
        fun newInstance() = NearbyInfectedFragment()
        private const val BLUETOOTH_REQUEST_DISCOVERABLE = 2
        private const val BLUETOOTH_RESULT_OK = 120
    }

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val infectedBTDevices = mutableListOf<String>()

    // Broadcast receiver triggered by Bluetooth devices nearby
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (device.address == INFECTED_PERSON_MAC_ADDRESS){
                        infectedBTDevices.add(device.address)
                        showBluetoothDevices(infectedBTDevices)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_nearby_infected, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        fragment_nearby_infected_discover_button?.setOnClickListener { checkFroRelevantPermission() }
    }

    private fun startDiscover() = bluetoothAdapter.startDiscovery()

    // Ask the user to activate bluetooth directly
    private fun activateBluetooth() = startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), BLUETOOTH_REQUEST_DISCOVERABLE)

    // Check if Location and Bluetooth permission are granted and enabled
    private fun checkFroRelevantPermission() {
        activity?.let {
            // check for location access
            if (ContextCompat.checkSelfPermission(it , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                // check if device location enabled
                if (it.isDeviceLocationEnabled()){
                    // check for bluetooth access
                    if (ContextCompat.checkSelfPermission(it , Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED){
                        // check if device bluetooth is enabled
                        if (bluetoothAdapter.isEnabled) startDiscover()
                        else activateBluetooth()
                    }else{
                        activateBluetooth()
                    }
                }else showNoGpsDialog()
            }else{
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            }
        }
    }

    // Show GPS required dialog for users denied permission before
    private fun showNoGpsDialog() {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.no_gps))
                .setMessage(getString(R.string.activate_gps))
                .setPositiveButton("Yes") { _, _ ->
                    this.startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_REQUEST_CODE)
                }.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    // Display Bluetooth devices nearby (infected one's)
    private fun showBluetoothDevices(devices: List<String>) {
        context?.let {
            if (!devices.isNullOrEmpty()) {
                lottie_animation_view?.show(false)
                val adapter = ArrayAdapter(it, android.R.layout.simple_dropdown_item_1line, devices.map { deviceName -> "Infected MAC address: $deviceName" })
                fragment_nearby_infected_list_view?.adapter = adapter
            }else{
                lottie_animation_view?.show()
            }
        }
    }

    // overriding onResume to register available bluetoothReceiver
    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(bluetoothReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
    }

    // overriding onResume to unregister available bluetoothReceiver
    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(bluetoothReceiver)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE ->
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkFroRelevantPermission()
            }
            BLUETOOTH_REQUEST_DISCOVERABLE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(bluetoothAdapter.isEnabled){
                        bluetoothAdapter.enable()
                        startDiscover()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            BLUETOOTH_REQUEST_DISCOVERABLE ->
                if (resultCode == BLUETOOTH_RESULT_OK) {
                    checkFroRelevantPermission()
                } else {
                    activity?.supportFragmentManager?.popBackStack()
                    showBluetoothError()
                }
            LOCATION_REQUEST_CODE -> checkFroRelevantPermission()
        }
    }

    private fun showBluetoothError() {
        context?.let { toast(it, "Bluetooth permission required to use this feature") }
    }

}