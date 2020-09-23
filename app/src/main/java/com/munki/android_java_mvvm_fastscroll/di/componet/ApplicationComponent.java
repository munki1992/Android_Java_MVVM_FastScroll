package com.munki.android_java_mvvm_fastscroll.di.componet;

import com.munki.android_java_mvvm_fastscroll.GlobalApplication;
import com.munki.android_java_mvvm_fastscroll.di.builder.ActivityBuilder;
import com.munki.android_java_mvvm_fastscroll.di.module.AppModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * Dagger를 사용하기 위한 Application 최상단 init를 위한 Component
 * @author 나비이쁜이
 * @since 2020.09.23
 */
@Singleton
@Component(modules = {AndroidSupportInjectionModule.class, AppModule.class, ActivityBuilder.class})
public interface ApplicationComponent extends AndroidInjector<GlobalApplication> {

    /**
     * Application 단위에서 초기화하기 위한 Builder입니다.
     * 컴포넌트를 생성하기 위한 빌드용 Annotation
     */
    @Component.Builder
    interface Builder {

        /**
         * Componet에서 application을 관리하기 시작
         */
        @BindsInstance
        Builder application(GlobalApplication application);

        ApplicationComponent create();
    }

    void inject(GlobalApplication app);
}