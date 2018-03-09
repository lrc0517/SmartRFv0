package com.aijiubao.factorytests.module;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aijiubao.factorytests.R;
import com.aijiubao.factorytests.utils.NeedleBean;


/**
 * Created by static on 2017/9/26.
 */

public class NeedleGridAdaptor extends RecyclerView.Adapter {

    private final Resources mResource;
    private Context mContext;
    private ArrayMap<String, Integer> mAddressDatas;
    private ArrayMap<Integer, BltNeedle> mNeedleDatas;

    public NeedleGridAdaptor(Context context,ArrayMap<String, Integer> addressdata, ArrayMap<Integer, BltNeedle> datas) {
        this.mContext = context;
        this.mAddressDatas = addressdata;
        this.mNeedleDatas = datas;
        mResource = mContext.getResources();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.view_recyclerview_item, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewholder = (ViewHolder) holder;
        String workStatus = null;
        String workTemperature = null;
        String workTime = null;
        int deviceId = 0;
        NeedleBean needle;
        if (mNeedleDatas != null && !mNeedleDatas.isEmpty()) {
            needle = mNeedleDatas.get(position+1).mNeedleBean;

            if (needle!=null && needle.enable) {
                if (needle.isWorking) {
                    //    iconId = R.drawable.ic_needle_one_on;
                    workStatus = mResource.getString(R.string.str_needle_work_status) + mResource.getString(R.string.str_needle_work_status_working);
                } else {
                    //    iconId = R.drawable.ic_needle_one_on;
                    workStatus = mResource.getString(R.string.str_needle_work_status) + mResource.getString(R.string.str_needle_work_status_stop);
                }
                workTemperature = mResource.getString(R.string.str_needle_work_temp) + needle.temperature + mResource.getString(R.string.str_needle_work_temp_unit);
                workTime = mResource.getString(R.string.str_needle_work_time) + needle.time + mResource.getString(R.string.str_needle_work_time_unit);
                deviceId = needle.deviceId;

                viewholder.status.setText(workStatus);
                viewholder.temperature.setText(workTemperature);
                viewholder.time.setText(workTime);
            }else{

            }
        }
    }

    @Override
    public int getItemCount() {
        return mNeedleDatas == null ? 0 : mNeedleDatas.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView status;
        TextView temperature;
        TextView time;

        public ViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.iv_item_number);
            status = (TextView) view.findViewById(R.id.tv_status);
            temperature = (TextView) view.findViewById(R.id.tv_temperature);
            time = (TextView) view.findViewById(R.id.tv_time);
        }
    }
}
