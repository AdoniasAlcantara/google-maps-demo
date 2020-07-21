package dev.proj4.demo.di

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import dev.proj4.demo.location.LocationLiveData
import dev.proj4.demo.ui.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val services = module {
    single {
        LocationLiveData(
            client = FusedLocationProviderClient(androidContext()),
            request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(10000)         // 10 seg
                .setFastestInterval(5000)   // 5 seg
        )
    }
}

val viewModels = module {
    viewModel { MainViewModel(get()) }
}