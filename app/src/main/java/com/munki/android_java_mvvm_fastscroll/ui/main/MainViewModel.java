package com.munki.android_java_mvvm_fastscroll.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableArrayList;
import androidx.recyclerview.widget.RecyclerView;

import com.munki.android_java_mvvm_fastscroll.ui.base.BaseViewModel;

/**
 * [MainActivity] View Model
 * @author 나비이쁜이
 * @since 2020.09.23
 */
public class MainViewModel extends BaseViewModel<MainNavigator> {

    // - Word
    public MainAdapter adapter;
    public ObservableArrayList<String> wordList;

    /**
     * 생성자
     */
    MainViewModel(@NonNull Application application, ObservableArrayList<String> word) {
        super(application);

        adapter = new MainAdapter(application);
        wordList = word;
    }

    /************************************************************************************************************************************************/

    /* Listener Databinding */

    // [Binding] setAdapter
    @BindingAdapter("setWordListAdapter")
    public static void bindNoticeListAdapter(RecyclerView recyclerView, RecyclerView.Adapter<?> adapter) {
        recyclerView.setAdapter(adapter);
    }

    // [Binding] setItem
    @BindingAdapter("setWordListItem")
    public static void bindNoticeListItem(RecyclerView recyclerView, ObservableArrayList<String> dataList) {
        MainAdapter adapter = (MainAdapter) recyclerView.getAdapter();

        if (adapter != null)
            adapter.setItem(dataList);
    }
}