package com.ET.telechat.Fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.ET.telechat.R

class Settings : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}