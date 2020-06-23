package com.liad.coronaapp.activities

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.liad.coronaapp.R
import com.liad.coronaapp.fragments.CountriesFragment
import com.liad.coronaapp.utils.extension.changeFragment

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Display MainFragment
        changeFragment(supportFragmentManager , R.id.main_activity_frame_layout , CountriesFragment.newInstance())
    }
}