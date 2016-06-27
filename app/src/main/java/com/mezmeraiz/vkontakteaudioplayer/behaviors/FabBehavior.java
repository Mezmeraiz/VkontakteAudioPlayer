package com.mezmeraiz.vkontakteaudioplayer.behaviors;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;

/**
 * Behavior для fab
 */
public class FabBehavior  extends CoordinatorLayout.Behavior<FloatingActionButton>{

    // На первом запуске активности FAB почему то мигает, перед тем, как стать невидимой,
    // поэтому пришлось прикрутить сей костыль - на на первом запуске FAB убираем на 300 вниз
    // и делаем firstLaunch true
    private boolean firstLaunch;

    public FabBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return dependency instanceof AppBarLayout || dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        // Мониторим положение Snackbar и AppBarLayout и меняем соответствующе положение FAB

        if (dependency instanceof Snackbar.SnackbarLayout){
            float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
            child.setTranslationY(translationY);
        }else if (dependency instanceof AppBarLayout){

            if(!firstLaunch){
                firstLaunch = true;
                child.setTranslationY(300);
                return true;
            }

            child.setTranslationY(dependency.getY() * -1);
        }
        return true;
    }

}
