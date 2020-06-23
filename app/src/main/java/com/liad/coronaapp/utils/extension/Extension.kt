package com.liad.coronaapp.utils.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputEditText
import com.liad.coronaapp.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

// Change fragment extension function to handle navigation easily
@SuppressLint("PrivateResource")
fun changeFragment(fragmentManager: FragmentManager?, @IdRes containerId: Int, fragment: Fragment, addToBackStack: Boolean = false) {
    if (fragmentManager == null) return
    val fragmentTransaction = fragmentManager.beginTransaction()
    if (addToBackStack) fragmentTransaction.addToBackStack(null)
    fragmentTransaction.setCustomAnimations(
        R.anim.abc_fade_in,
        R.anim.abc_shrink_fade_out_from_bottom,
        R.anim.abc_grow_fade_in_from_bottom,
        R.anim.abc_popup_exit
    )
    fragmentTransaction
        .replace(containerId, fragment, fragment::class.java.simpleName)
        .commit()
}

// Navigate between activities easier
fun Activity.changeActivity(
    destination: Class<*>,
    closeCurrent: Boolean = false,
    data: Bundle? = null
) {
    val intent = Intent(this, destination)
    data?.let { intent.putExtras(it) }
    startActivity(intent)
    if (closeCurrent) finish()
}

fun toast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(context, message, duration).show()

// MutableList extension function used to clear list and add new values
fun <T> MutableList<T>.clearAndAddAll(newData: List<T>) {
    clear()
    addAll(newData)
}

// View extension function to Toggle between View visibility states
fun View.show(show: Boolean = true) {
    visibility = if (show) View.VISIBLE else View.GONE
}

// Formatting receiver String value to Date format
fun String.toDateFormat(): String {
    val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    var date: Date? = null
    try {
        date = inputFormat.parse(this)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return outputFormat.format(date)
}

// Formatting receiver String (as Date) value to String format
fun String.dateToStringFormat(): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date: Date?
    date = try {
        inputFormat.parse(this)
    } catch (e: ParseException) {
        e.printStackTrace()
        Date()
    }
    return outputFormat.format(date)
}

// Validate TextInputEditText receiver
fun TextInputEditText.isValid(): Boolean {
    return !text.isNullOrBlank()
}

// Compare between two dates (validate toDate is after or the same as String receiver)
fun String.isBefore(toDate : String): Boolean {
    val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var date: Date? = null
    var date2: Date? = null
    try {
        date = inputFormat.parse(this)
        date2 = inputFormat.parse(toDate)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return date?.before(date2) ?: false || date?.compareTo(date2) == 0
}

// Activity extension function to determine Device location enable/disable
fun Activity.isDeviceLocationEnabled(): Boolean {
    val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

