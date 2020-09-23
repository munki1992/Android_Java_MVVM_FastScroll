package com.munki.android_java_mvvm_fastscroll.di.module;

import android.content.Context;

import com.munki.android_java_mvvm_fastscroll.GlobalApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * [Dagger] AppComponent의 Module 제공
 * Module은 class에만 붙이며 Provides는 반드시 Module class 안에 선언된 메소드에만 사용
 * @author 나비이쁜이
 * @since 2020.09.23
 */
@Module
public class AppModule {

    /**
     * [필수] ApplicationComponent에 기본적으로 사용되는 application
     */
    @Provides
    @Singleton
    Context provideContext(GlobalApplication application) {
        return application;
    }
}