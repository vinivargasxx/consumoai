package com.example.consumoai.core.di

import com.example.consumoai.data.repository.FakeReceiptRepository
import com.example.consumoai.domain.repository.ReceiptRepository
import org.koin.dsl.module

val dataModule = module {

    single<ReceiptRepository> {
        FakeReceiptRepository()
    }
}

