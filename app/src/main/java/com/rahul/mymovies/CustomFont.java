package com.rahul.mymovies;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Rahul on 09 Apr 2016.
 */
public class CustomFont {

    public static void overrideFonts(final Context context, final View v, String fontFromAssets) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child, fontFromAssets);
                }
            } else if (v instanceof TextView) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), fontFromAssets));
            }
        } catch (Exception e) {
        }
    }
}
