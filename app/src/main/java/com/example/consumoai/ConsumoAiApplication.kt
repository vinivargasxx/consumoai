package com.example.consumoai

import android.app.Application
import com.example.consumoai.core.di.appModule
import com.example.consumoai.core.di.dataModule
import com.example.consumoai.core.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ConsumoAiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ConsumoAiApplication)
            modules(
                appModule,
                domainModule,
                dataModule
            )
        }
    }
}

