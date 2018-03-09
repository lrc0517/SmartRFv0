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

public class CenterDialog extends Dialog implements View.OnClickListener {
    private Context mContext;

    public CenterDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.mContext = context;
        setContentView(R.layout.view_dialog);
        findViewById(R.id.button_ok).setOnClickListener(this);
        findViewById(R.id.button_failed).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (mOnCenterDialogSelected != null) {
            switch (view.getId()) {
                case R.id.button_ok:
                    mOnCenterDialogSelected.onSelected(true);
                    break;
                case R.id.button_failed:
                    mOnCenterDialogSelected.onSelected(false);
                    break;
                default:
                    mOnCenterDialogSelected.onSelected(false);
                    break;
            }
        }
    }

    private OnCenterDialogSelected mOnCenterDialogSelected;

    public void setOnCenterDialogSelected(OnCenterDialogSelected l) {
        this.mOnCenterDialogSelected = l;
    }

    public interface OnCenterDialogSelected {

        void onSelected(boolean isOk);
    }
}
