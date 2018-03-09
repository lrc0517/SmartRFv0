package com.AmoSmartRF.bluetooth.le;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeView extends TextView {
	public MarqueeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MarqueeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MarqueeView(Context context) {
		super(context);
	}

	@Override
	public boolean isFocused() {
		return true;
	}
}