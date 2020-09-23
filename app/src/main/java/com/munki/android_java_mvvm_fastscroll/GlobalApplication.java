package com.munki.android_java_mvvm_fastscroll;

import android.content.Context;

import androidx.multidex.MultiDex;

import com.munki.android_java_mvvm_fastscroll.di.componet.DaggerApplicationComponent;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import dagger.android.HasAndroidInjector;

/**
 * Activity에서 공통적으로 적용되는 상위 MultiDexApplication
 * @author 나비이쁜이
 * @since 2020.09.23
 */
public class GlobalApplication extends DaggerApplication implements HasAndroidInjector {

    /**
     * ContributesAndroidInjector를 사용하기 위한 Injector
     */
    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerApplicationComponent.builder().application(this).create();
    }

    /**
     * attachBaseContext
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // MultiDex init
        MultiDex.install(this);
    }
}