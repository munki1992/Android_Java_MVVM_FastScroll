package com.munki.android_java_mvvm_fastscroll.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.munki.android_java_mvvm_fastscroll.R;
import com.munki.android_java_mvvm_fastscroll.databinding.ActivityMainBinding;
import com.munki.android_java_mvvm_fastscroll.ui.base.BaseActivity;

import javax.inject.Inject;

import static com.munki.android_java_mvvm_fastscroll.BR.main;

/**
 * [MVVM] MainActivity
 * @author 나비이쁜이
 * @since 2020.09.23
 */
public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> implements MainNavigator {

    // - this.binding & this.viewmodel
    private ActivityMainBinding mBinding;
    @Inject MainViewModel mViewModel;

    /************************************************************************************************************************************************/

    /**
     * Binding variable
     */
    @Override
    public int getBindingVariable() {
        return main;
    }

    /**
     * Resoucres Layout
     */
    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    /**
     * ViewModel
     */
    @Override
    public MainViewModel getViewModel() {
        mViewModel.setNavigation(this);
        return mViewModel;
    }

    /************************************************************************************************************************************************/

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = getViewDataBinding();

        // observer init
        ObserverInit();
    }

    /**
     * Observer & Init
     */
    @Override
    protected void ObserverInit() {
        super.ObserverInit();

        // FastScrollView Setting
        mBinding.fastScrollView.setKeywordList(mViewModel.wordList);
        mBinding.fastScrollView.setRecyclerView(mBinding.rvWord);

        mBinding.rvWord.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mBinding.fastScrollView.showHandle(false);
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    mBinding.fastScrollView.showHandle(true);
                }
            }
        });
    }
}