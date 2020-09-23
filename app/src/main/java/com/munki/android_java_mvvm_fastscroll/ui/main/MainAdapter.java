package com.munki.android_java_mvvm_fastscroll.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableArrayList;

import com.munki.android_java_mvvm_fastscroll.R;
import com.munki.android_java_mvvm_fastscroll.databinding.ItemRecyclerviewBinding;
import com.munki.android_java_mvvm_fastscroll.ui.base.BaseViewHolder;
import com.munki.android_java_mvvm_fastscroll.ui.custom.RecyclerFastScroller;

/**
 * BaseViewHolder
 * @author 나비이쁜이
 * @since 2020.09.23
 */
public class MainAdapter extends RecyclerFastScroller.KoreanIndexerRecyclerAdapter<BaseViewHolder<String>> implements RecyclerFastScroller.FastScrollable {

    /**
     * Context & Word
     */
    private Context mContext;
    private ObservableArrayList<String> dataList;

    /**
     * 생성자
     */
    public MainAdapter(Context context) {
        this.mContext = context;
    }

    /**
     * onCreateViewHolder
     */
    @NonNull
    @Override
    public BaseViewHolder<String> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview, parent, false));
    }

    /**
     * onBindViewHolder
     */
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<String> holder, int position) {
        holder.bind(dataList.get(holder.getLayoutPosition()), position);
    }

    /**
     * getItemCount
     */
    @Override
    public int getItemCount() {
        return dataList.size();
    }

    /************************************************************************************************************************************************/

    /**
     * setItem
     */
    public void setItem(ObservableArrayList<String> dataList) {
        if (dataList == null)
            return;

        this.dataList = dataList;
        notifyDataSetChanged();
    }

    /************************************************************************************************************************************************/

    /**
     * setBubbleText
     */
    @Override
    public String setBubbleText(int position) {
        return dataList.get(position);
    }

    /************************************************************************************************************************************************/

    /**
     * 뷰 활용을 위한 Viewholder
     */
    public static class ViewHolder extends BaseViewHolder<String> {

        /**
         * itemView Databinding
         */
        ItemRecyclerviewBinding mBinding;

        /**
         * 생성자
         */
        ViewHolder(@NonNull View view) {
            super(view);
            mBinding = DataBindingUtil.bind(view);
        }

        /**
         * Bind
         */
        @Override
        public void bind(String itemVo, Integer position) {
            mBinding.textView.setText(itemVo);
        }
    }
}
