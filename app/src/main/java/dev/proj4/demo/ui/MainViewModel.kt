package dev.proj4.demo.ui

import androidx.lifecycle.ViewModel
import dev.proj4.demo.location.LocationLiveData

class MainViewModel(val locationUpdates: LocationLiveData) : ViewModel()