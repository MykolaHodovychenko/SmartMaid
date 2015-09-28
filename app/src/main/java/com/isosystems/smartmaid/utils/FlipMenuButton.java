package com.isosystems.smartmaid.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.isosystems.smartmaid.R;

public class FlipMenuButton extends ImageButton {

    Context mContext;

    int mDefaultImageResource;
    int mActiveImageResource;

    public Boolean buttonPressed = false;

    public void setImageResources(int def_image, int active_image) {
        mDefaultImageResource = def_image;
        mActiveImageResource  = active_image;
    }

    public FlipMenuButton(Context context) {
        super(context);
        mContext = context;
        setDrawingCacheEnabled(true);
        buildDrawingCache();
    }
    public FlipMenuButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setDrawingCacheEnabled(true);
        buildDrawingCache();
    }
    public FlipMenuButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        setDrawingCacheEnabled(true);
        buildDrawingCache();
    }

    public void changeButtonState() {
        setButtonState(!buttonPressed);
    }

    public void setButtonState (boolean state) {
        if (buttonPressed != state) {
            if (buttonPressed) {
                this.setImageResource(mDefaultImageResource);
            } else {
                this.setImageResource(mActiveImageResource);
            }
            buttonPressed = state;
        }
    }
}
