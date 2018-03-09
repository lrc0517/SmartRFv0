package com.AmoSmartRF.bluetooth.le;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.View;

import com.ruitai.qab.bl.R;

/**
 * Created by static on 2017/8/11.
 */

public class WirteNumberDialog extends Dialog implements View.OnClickListener {
    private Context mContext;

    public WirteNumberDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.mContext = context;
        setContentView(R.layout.view_writenumber_dialog);
        findViewById(R.id.button_confirm).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (mOnWirteNumberDialogClick != null) {
            switch (view.getId()) {
                case R.id.button_confirm:
                    mOnWirteNumberDialogClick.onConfirm();
                    break;
                case R.id.button_cancel:
                    mOnWirteNumberDialogClick.onCancel();
                    break;
            }
        }
        dismiss();
    }

    private OnWirteNumberDialogClick mOnWirteNumberDialogClick;

    public void setOnWirteNumberDialogClick(OnWirteNumberDialogClick l) {
        this.mOnWirteNumberDialogClick = l;
    }

    public interface OnWirteNumberDialogClick {

        void onConfirm();
        void onCancel();
    }
}
