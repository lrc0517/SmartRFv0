package com.ruitai.aijiubao.activity.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruitai.aijiubao.R;
import com.ruitai.aijiubao.module.BltConn;
import com.ruitai.aijiubao.module.BltNeedle;
import com.ruitai.aijiubao.module.NeedleGridAdaptor;
import com.ruitai.aijiubao.utils.BltConstants;
import com.ruitai.aijiubao.utils.NeedleBean;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class BltActivity extends BaseActivity {


    public static final String TAG = "BltActivity";

    private NumberPickerView mPickerTime;
    private NumberPickerView mPickerTemp;
    private NumberPickerView mPickerInterval;
    private ImageView mIvSetting;
    private ImageView mIvRefalsh;
    private ImageView mIvBack;
    private TextView mTvTitle;
    private GridView mGvNeedle;
    private ArrayMap<Integer, BltNeedle> mNeedleDatas;
    private ArrayMap<String, Integer> mAddressDatas;
    private NeedleGridAdaptor mNeedleGridAdaptor;
    private int[] mTittleId;
    private BltNeedle mBltNeedle;
    private BltConn mBltConn;

    private Dialog mReflashDialog;
    private ImageView mDialogImg;
    private Animation mDialogAnimation;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BltConstants.MSG_HANDLER_ON_BLT_READ:
                    mNeedleGridAdaptor.notifyDataSetChanged();
                    break;
                case BltConstants.MSG_HANDLER_ON_BLT_STOP_SCAN:
                    mReflashDialog.dismiss();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blt);
        initView();
        initData();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //+==============================================================+//
    private void initView() {

        mIvSetting = (ImageView) findViewById(R.id.iv_setting);
        mIvRefalsh = (ImageView) findViewById(R.id.iv_reflash);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mTvTitle = (TextView) findViewById(R.id.action_title);

        mPickerTime = (NumberPickerView) findViewById(R.id.time_picker);
        mPickerTemp = (NumberPickerView) findViewById(R.id.temp_picker);
        mPickerInterval = (NumberPickerView) findViewById(R.id.interval_picker);

        String[] display_time = getResources().getStringArray(R.array.minute_display);
        //String[] display_temp = getResources().getStringArray(R.array.temperature_display);
        String[] display_gear_temp = getResources().getStringArray(R.array.temperature_gear_display);
        String[] display_interval = getResources().getStringArray(R.array.interval_display);

        mPickerTime.refreshByNewDisplayedValues(display_time);
       // mPickerTemp.refreshByNewDisplayedValues(display_temp);
        mPickerTemp.refreshByNewDisplayedValues(display_gear_temp);
        mPickerInterval.refreshByNewDisplayedValues(display_interval);

        mGvNeedle = (GridView) findViewById(R.id.gv_needle);

        mReflashDialog  =  new Dialog(this,R.style.dialog_custom);
        mDialogImg = new ImageView(this);
        mDialogImg.setImageResource(R.drawable.refresh);
        mDialogAnimation = AnimationUtils.loadAnimation(this, R.anim.reflesh_anim);

        mReflashDialog.setContentView(mDialogImg);
    }

    private void initData() {
        mTittleId = new int[]{
                R.string.needle_one,
                R.string.needle_two,
                R.string.needle_three,
                R.string.needle_four,
                R.string.needle_five,
                R.string.needle_six
        };

        mNeedleDatas = new ArrayMap<Integer, BltNeedle>();
        mAddressDatas = new ArrayMap<String, Integer>();
        mNeedleGridAdaptor = new NeedleGridAdaptor(this, mAddressDatas, mNeedleDatas);
        mGvNeedle.setAdapter(mNeedleGridAdaptor);

        mBltConn = new BltConn(this, mHandler, mBluetoothAdapter, mAddressDatas, mNeedleDatas);

        //mBltNeedle = new BltNeedle(this,mBluetoothAdapter);
        //mBltNeedle.setOnBlueToothNeedleStatusChanged(mOnBlueToothNeedleStatusChanged);
    }

    private void initListener() {
        mIvSetting.setOnClickListener(mOnClickListener);
        mIvRefalsh.setOnClickListener(mOnClickListener);
        mIvBack.setOnClickListener(mOnClickListener);
        mGvNeedle.setOnItemClickListener(mOnItemClickListener);
        mGvNeedle.setOnItemLongClickListener(mOnItemLongClick);
        mGvNeedle.setOnItemSelectedListener(mOnItemSelected);

        mPickerTime.setOnScrollListener(mPickViewOnScroll);
        mPickerTime.setOnValueChangedListener(mPickViewOnValueChange);
        mPickerTime.setOnValueChangeListenerInScrolling(mPickViewOnValueChangeScrolling);

        mPickerTemp.setOnScrollListener(mPickViewOnScroll);
        mPickerTemp.setOnValueChangedListener(mPickViewOnValueChange);
        mPickerTemp.setOnValueChangeListenerInScrolling(mPickViewOnValueChangeScrolling);

        mPickerInterval.setOnScrollListener(mPickViewOnScroll);
        mPickerInterval.setOnValueChangedListener(mPickViewOnValueChange);
        mPickerInterval.setOnValueChangeListenerInScrolling(mPickViewOnValueChangeScrolling);
    }

    //+=====================================================================+//

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_back:
                    mBltConn.clearData();
                    finish();
                    break;
                case R.id.iv_setting:
                    setMaxTemperature();
                    break;
                case R.id.iv_reflash:
                    reflashDevices();
                    break;
            }
        }
    };

    private void reflashDevices() {
        mItem = 10;
        mDialogImg.startAnimation(mDialogAnimation);//开始动画
        mReflashDialog.show();
        mBltConn.clearData();
        mNeedleGridAdaptor.notifyDataSetChanged();
        mBltConn.reflash();
    }

    private BltNeedle mCurrentNeedle;

    private void setMaxTemperature() {
        if (mCurrentNeedle != null) {
            Toast.makeText(this,"设置数据发送中！",Toast.LENGTH_LONG).show();
            mCurrentNeedle.setHope(mHopeTimeValue,mHopeTemperatureValue,mHopeIntervalValue);
        } else {
            Toast.makeText(this,"请长按选中设备,再进行设置！",Toast.LENGTH_LONG).show();
        }
    }

    private void upDateViews(int item){

        mTvTitle.setText(mTittleId[item]);
        BltNeedle needle = mNeedleDatas.get(item + 1);
        NeedleBean needleBean = needle.mNeedleBean;

        try {
            if (needleBean != null && needleBean.enable) {
                mPickerTime.setValue(needleBean.time/5 -1);
              //  mPickerTemp.setValue(needleBean.hopeTemperature <= 40 ? needleBean.hopeTemperature-5 : needleBean.hopeTemperature - 45);
                mPickerTemp.setValue(needleBean.hopeTemperature/5-9);
                mPickerInterval.setValue(needleBean.interval/5 -1);
            }
        }catch (Exception e){
            Log.e(TAG,"Set Tempareture Error e -->"+e);
        }

        changedColor(item);
    }

    private int mItem = 10;
    private void changedColor(int item){
        if (mItem == item){
            return;
        }else{
            for (String key:mAddressDatas.keySet()) {
              int i =  mAddressDatas.get(key);
                mGvNeedle.getChildAt(i-1).setBackground(null);
            }
            mGvNeedle.getChildAt(item).setBackgroundResource(R.drawable.bg_gv_item);
        }
        mItem = item;
    }


    private int mHopeTimeValue = 30;
    private int mHopeTemperatureValue = 45;
    private int mHopeIntervalValue = 5;

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            BltNeedle needle = mNeedleDatas.get(i + 1);
            needle.startWork();
        }
    };

    private AdapterView.OnItemLongClickListener mOnItemLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            Log.e(TAG,"OnItemLongClickListener onItemLongClick");
            upDateViews(i);
            BltNeedle needle = mNeedleDatas.get(i + 1);
            mCurrentNeedle = needle;
            return true;
        }
    };

    private AdapterView.OnItemSelectedListener mOnItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Log.e(TAG,"OnItemSelectedListener onItemSelected");
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            Log.e(TAG,"OnItemSelectedListener onNothingSelected");
        }
    };


    private NumberPickerView.OnScrollListener mPickViewOnScroll = new NumberPickerView.OnScrollListener() {

        @Override
        public void onScrollStateChange(NumberPickerView view, int scrollState) {

        }
    };

    private NumberPickerView.OnValueChangeListener mPickViewOnValueChange = new NumberPickerView.OnValueChangeListener() {


        @Override
        public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {

            Log.e(TAG, "onValueChange newVal= " + newVal);
            switch (picker.getId()) {
                case R.id.time_picker:
                    mHopeTimeValue = (newVal +1) * 5;
                    break;
                case R.id.temp_picker:
                    //mHopeTemperatureValue = newVal + 45;
                    mHopeTemperatureValue = (newVal + 9)*5;
                    break;
                case R.id.interval_picker:
                    mHopeIntervalValue = (newVal +1) * 5;
                    break;
            }
            Log.e(TAG, "mHopeTimeValue = " + mHopeTimeValue +",mHopeTemperatureValue = "+mHopeTemperatureValue +",mHopeIntervalValue = "+mHopeIntervalValue);
        }

    };

    private NumberPickerView.OnValueChangeListenerInScrolling mPickViewOnValueChangeScrolling = new NumberPickerView.OnValueChangeListenerInScrolling() {

        @Override
        public void onValueChangeInScrolling(NumberPickerView picker, int oldVal, int newVal) {

        }
    };

    //+=====================================================================+//

}

