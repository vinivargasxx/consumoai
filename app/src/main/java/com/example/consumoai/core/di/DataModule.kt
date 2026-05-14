package com.example.consumoai.core.di

import androidx.room.Room
import com.example.consumoai.BuildConfig
import com.example.consumoai.data.classifier.ConsumptionModelApi
import com.example.consumoai.data.classifier.KeywordProductClassifierDataSource
import com.example.consumoai.data.classifier.RemoteConsumptionBehaviorClassifier
import com.example.consumoai.data.classifier.RuleBasedConsumptionBehaviorClassifier
import com.example.consumoai.data.datasource.qrcode.NfceQrCodeDataSource
import com.example.consumoai.data.local.AppDatabase
import com.example.consumoai.data.parser.NfceHtmlParserDataSource
import com.example.consumoai.data.repository.ReceiptRepositoryImpl
import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
import com.example.consumoai.domain.classifier.ProductClassifier
import com.example.consumoai.domain.repository.ReceiptRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "consumoai-db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        get<AppDatabase>().receiptDao()
    }

    single<ReceiptRepository> {
        ReceiptRepositoryImpl(
            receiptDao = get()
        )
    }

    single {
        NfceHtmlParserDataSource()
    }

    single {
        NfceQrCodeDataSource(
            nfceHtmlParserDataSource = get()
        )
    }

    single<ProductClassifier> {
        KeywordProductClassifierDataSource()
    }

    single {
        OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                }
            }
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.MODEL_API_BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(ConsumptionModelApi::class.java)
    }

    single {
        RuleBasedConsumptionBehaviorClassifier()
    }

    single<ConsumptionBehaviorClassifier> {
        RemoteConsumptionBehaviorClassifier(
            api = get(),
            fallbackClassifier = get()
        )
    }
}
