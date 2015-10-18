package com.mezmeraiz.vkontakteaudioplayer.behaviors;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
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
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FrameLayout child, View directTargetChild, View target, int nestedScrollAxes) {
        return target instanceof RecyclerView;
    }



    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FrameLayout child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        Log.d("myLogs", " " + dxConsumed + " " + dyConsumed + " " + dxUnconsumed + " " + dyUnconsumed);
        if (dyConsumed > 0){

            child.animate().translationY(83).setDuration(100);
        }else {
            child.animate().translationY(0).setDuration(100);
        }

    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FrameLayout child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FrameLayout child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }

}
