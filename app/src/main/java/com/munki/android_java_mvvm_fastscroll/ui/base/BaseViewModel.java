package com.munki.android_java_mvvm_fastscroll.ui.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.lang.ref.WeakReference;

/**
 * Base View Model
 * Application의 Context를 사용하고 싶다면 AndroidViewModel, 전자의 사항이 아니라면 ViewModel
 * ViewModeld은 view or Context를 가지고 있어서는 안됩니다.
 * @author 나비이쁜이
 * @since 2020.09.23
 */
public abstract class BaseViewModel<N> extends AndroidViewModel {

    /**
     * Interfaces navigator
     */
    private WeakReference<N> navigator;

    public N getNavigation() {
        return navigator.get();
    }

    public void setNavigation(N navigator) {
        this.navigator = new WeakReference<>(navigator);
    }

    /*************************************************************************************************************************************************/

    /**
     * 생성자 - Context를 사용하기 위한 ViewModel
     */
    public BaseViewModel(@NonNull Application application) {
        super(application);
    }
}