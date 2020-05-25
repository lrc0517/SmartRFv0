package com.aijiubao.factorytests;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.ArrayMap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.aijiubao.factorytests.module.BltConn;
import com.aijiubao.factorytests.module.BltNeedle;
import com.aijiubao.factorytests.module.NeedleGridAdaptor;
import com.aijiubao.factorytests.utils.BltConstants;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TEST/MainActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private FloatingActionButton mFab;
    private Animation mDialogAnimation;
    private ImageView mDialogImg;
    private Dialog mReflashDialog;
    private Dialog mWriteDialog;
    private RecyclerView mRecyclerView;
    private NeedleGridAdaptor mNeedleGridAdaptor;
    private GridLayoutManager mGridLayoutManager;
    private ArrayMap<Integer, BltNeedle> mNeedleDatas;
    private ArrayMap<String, Integer> mAddressDatas;
    private BltConn mBltConn;
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
        setContentView(R.layout.activity_main);
        initView();
        checkBLT();
        initData();
        initListener();
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            if (mBluetoothAdapter.enable()) {
                Toast.makeText(this, R.string.do_open_bluetooth, Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                turnOffDevices();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //=================================

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_aijiubao);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mReflashDialog = new Dialog(this, R.style.dialog_custom);
        mDialogImg = new ImageView(this);
        mDialogImg.setImageResource(R.mipmap.ic_refresh);
        mDialogAnimation = AnimationUtils.loadAnimation(this, R.anim.reflesh_anim);

        mReflashDialog.setContentView(mDialogImg);

        mWriteDialog = new Dialog(this, R.style.dialog_custom);
        //mWriteDialog.setc

    }

    protected void checkBLT() {
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
                    .show();
            finish();
        } else {
            Log.i(TAG, "initialize Bluetooth, has BLE system");
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            Log.i(TAG, "mBluetoothAdapter = " + mBluetoothAdapter);
        }
    }

    private void initData() {
        mNeedleDatas = new ArrayMap<Integer, BltNeedle>();
        mAddressDatas = new ArrayMap<String, Integer>();
        mGridLayoutManager = new GridLayoutManager(this, 2);
       // mGridLayoutManager.setSpanSizeLookup(mSpanSizeLookup);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mNeedleGridAdaptor = new NeedleGridAdaptor(this, mAddressDatas, mNeedleDatas);
        mRecyclerView.setAdapter(mNeedleGridAdaptor);

        mBltConn = new BltConn(this, mHandler, mBluetoothAdapter, mAddressDatas, mNeedleDatas);
    }

    private void initListener() {
        mFab.setOnClickListener(mOnClickListener);
        mNeedleGridAdaptor.setItemClickListener(mOnItemClickListener);
        mNeedleGridAdaptor.setItemLongClickListener(mOnItemLongClickListener);
    }


    private void reflashDevices() {
        mDialogImg.startAnimation(mDialogAnimation);//开始动画
        mReflashDialog.show();
        mBltConn.clearData();
        mNeedleGridAdaptor.notifyDataSetChanged();
        mBltConn.reflash();
    }


    private void turnOffDevices() {
        mBltConn.turnOff();
        mNeedleGridAdaptor.notifyDataSetChanged();
    }




    //=====================================================
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fab:
                    reflashDevices();
                    break;
            }
        }
    };

    private GridLayoutManager.SpanSizeLookup mSpanSizeLookup = new GridLayoutManager.SpanSizeLookup(){

        @Override
        public int getSpanSize(int position) {
            Log.i(TAG,"position = "+position);
            return 2;
        }
    };

    private NeedleGridAdaptor.OnItemClickListener mOnItemClickListener = new NeedleGridAdaptor.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            Log.e(TAG,"OnItemClickListener  position = "+position);

            BltNeedle bltNeedle = mNeedleDatas.get(position+1);
            bltNeedle.stopWork();


        }
    };

    private NeedleGridAdaptor.OnItemLongClickListener mOnItemLongClickListener = new NeedleGridAdaptor.OnItemLongClickListener() {
        @Override
        public void onItemLongClick(int position) {
            Log.e(TAG,"OnItemLongClickListener  position = "+position);

        }
    };
}
