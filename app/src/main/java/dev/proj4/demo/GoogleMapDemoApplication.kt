package dev.proj4.demo

import android.app.Application
import dev.proj4.demo.di.services
import dev.proj4.demo.di.viewModels
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

@Suppress("unused")
class GoogleMapDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(viewModels, services)
        }
    }
}