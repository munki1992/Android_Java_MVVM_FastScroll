package com.munki.android_java_mvvm_fastscroll.ui.base;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import dagger.android.AndroidInjection;

/**
 * [MVVM] BaseActivity
 * B = databinding / M = viewModel
 * @author 나비이쁜이
 * @since 2020.09.23
 */
public abstract class BaseActivity<B extends ViewDataBinding, M> extends AppCompatActivity {

    /**
     * Context
     */
    protected Context mContext;

    /*************************************************************************************************************************************************/

    /**
     * Current DataBinding & ViewModel
     */
    private B mViewDataBinding;
    private M mViewModel;

    /**
     * 레이아웃으로 정의한 variable - name을 BR class에서 가져옵니다.
     * @return variable id
     */
    public abstract int getBindingVariable();

    /**
     * 상속받는 현재 resoucre id를 가져옵니다.
     * @return layout resource id
     */
    @LayoutRes
    public abstract int getLayoutId();

    /**
     * 상속받는 현재 ViewModel을 가져옵니다.
     */
    public abstract M getViewModel();

    /**
     * 현재 ui의 DataBinding을 가져옵니다.
     */
    public B getViewDataBinding() {
        return mViewDataBinding;
    }

    /**
     * 상속받는 현재 ui의 DataBinding을 적용합니다.
     */
    private void performDataBinding() {
        // Activity의 ViewDatabinding을 가져옵니다.
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId());

        /**
         * (true) View Model이 존재하는 경우 getViewModel()
         * (false) View Model이 존재하지 않는 경우 mViewModel
         */
        this.mViewModel = mViewModel == null ? getViewModel() : mViewModel;

        // Binding Model를 적용
        mViewDataBinding.setVariable(getBindingVariable(), mViewModel);

        // Binding을 즉시 실행
        mViewDataBinding.executePendingBindings();
    }

    /*************************************************************************************************************************************************/

    /**
     * onCreate
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Dagger Injection init
        AndroidInjection.inject(this);

        // super onCreate
        super.onCreate(savedInstanceState);

        // Databinding
        performDataBinding();

        // Context
        mContext = this;
    }

    /**
     * Observer Init
     */
    protected void ObserverInit() { }
}