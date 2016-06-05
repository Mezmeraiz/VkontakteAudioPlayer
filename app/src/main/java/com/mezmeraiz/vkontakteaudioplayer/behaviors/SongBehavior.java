package com.mezmeraiz.vkontakteaudioplayer.behaviors;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Behavior для FrameLayout с названием композиции
 */
public class SongBehavior extends CoordinatorLayout.Behavior<FrameLayout> {

    public SongBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FrameLayout child, View dependency) {
        return dependency instanceof AppBarLayout || dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FrameLayout child, View dependency) {
        if (dependency instanceof Snackbar.SnackbarLayout){
            float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
            child.setTranslationY(translationY);
        }else if (dependency instanceof AppBarLayout){
            child.setTranslationY(dependency.getY() * -1);
        }
        return true;
    }

}
