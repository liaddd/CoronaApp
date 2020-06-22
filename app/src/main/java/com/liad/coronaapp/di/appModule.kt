package com.liad.coronaapp.di

import com.liad.coronaapp.CoronaApp
import com.liad.coronaapp.repositories.CoronaRepository
import com.liad.coronaapp.viewmodels.CoronaViewModel
import org.koin.dsl.module

val appModule = module {

    // single instance of CoronaApp
    single { CoronaApp.instance }

    // single instance of Retrofit
    single { RetrofitImpl.getRetrofit() }

    // single instance of Repository
    single { CoronaRepository(get()) }

    //
    factory { CoronaViewModel(get()) }
}
