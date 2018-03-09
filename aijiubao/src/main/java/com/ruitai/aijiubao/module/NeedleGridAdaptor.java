package com.ruitai.aijiubao.module;

import android.content.Context;
import android.content.res.Resources;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruitai.aijiubao.R;
import com.ruitai.aijiubao.utils.NeedleBean;


/**
 * Created by static on 2017/9/26.
 */

public class NeedleGridAdaptor extends BaseAdapter {

    private final Resources mResource;
    private final int[] mIconDatas;
    private Context mContext;
    private ArrayMap<String, Integer> mAddressDatas;
    private ArrayMap<Integer, BltNeedle> mNeedleDatas;

    public NeedleGridAdaptor(Context mContext,ArrayMap<String, Integer> addressdata, ArrayMap<Integer, BltNeedle> datas) {
        this.mContext = mContext;
        this.mAddressDatas = addressdata;
        this.mNeedleDatas = datas;
        mResource = mContext.getResources();
        mIconDatas = new int[]{
                R.mipmap.ic_he,
                R.mipmap.ic_jin,
                R.mipmap.ic_mu,
                R.mipmap.ic_shui,
                R.mipmap.ic_huo,
                R.mipmap.ic_tu
        };
    }


    @Override
    public int getCount() {
        return mNeedleDatas == null ? 0 : mNeedleDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return mNeedleDatas == null ? null : mNeedleDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.item_gv_status, null);
            holder.icon = (ImageView) view.findViewById(R.id.iv_item_number);
            holder.status = (TextView) view.findViewById(R.id.tv_status);
            holder.temperature = (TextView) view.findViewById(R.id.tv_temperature);
            holder.time = (TextView) view.findViewById(R.id.tv_time);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

       // int iconId = 0;
        String workStatus = null;
        String workTemperature = null;
        String workTime = null;
        int deviceId = 0;
        NeedleBean needle;
        if (mNeedleDatas != null && !mNeedleDatas.isEmpty()) {
            needle = mNeedleDatas.get(i+1).mNeedleBean;

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

                holder.icon.setImageResource(mIconDatas[deviceId]);
                holder.status.setText(workStatus);
                holder.temperature.setText(workTemperature);
                holder.time.setText(workTime);
            }else{

            }
        }


        return view;
    }

    public class ViewHolder {
        ImageView icon;
        TextView status;
        TextView temperature;
        TextView time;
    }
}
