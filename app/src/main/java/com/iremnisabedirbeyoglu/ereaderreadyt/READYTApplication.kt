package com.iremnisabedirbeyoglu.ereaderreadyt
import android.app.Application
import dagger.hilt.android.HiltAndroidApp


    // Hilt'i bu uygulama sınıfında başlatır
    @HiltAndroidApp
    class READYTApplication : Application() {
        // Uygulama başlatıldığında yapılacak ek işlemler buraya eklenebilir
        override fun onCreate() {
            super.onCreate()
            // Örneğin, Timber gibi bir loglama kütüphanesi başlatılabilir
        }
    }
