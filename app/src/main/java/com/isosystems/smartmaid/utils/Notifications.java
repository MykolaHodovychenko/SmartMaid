package com.isosystems.smartmaid.utils;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.isosystems.smartmaid.R;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class Notifications {

    public static void showConnectionErrorMessage(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message,Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#4d4032"));
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#d4bca2"));
        textView.setTextSize(20.0f);
        snackbar.show();
    }

    public static void showErrorCrouton (Activity activity, String message) {

        Configuration configuration = new Configuration.Builder().setDuration(1200).build();

        Style style = new Style.Builder()
                .setTextSize(25)
                .setImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
                .setBackgroundColorValue(0xffff4444)
                .setConfiguration(configuration)
                .setImageResource(R.drawable.crouton_alert)
                .setHeight(70)
                .setPaddingInPixels(0)
                .setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL)
                .build();

        Crouton.makeText(activity, message, style).show();
    }

}
