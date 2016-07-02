package com.mezmeraiz.vkontakteaudioplayer.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.mezmeraiz.vkontakteaudioplayer.R;


/**
 * Активность с выбором темы
 */
public class ChooseThemeActivity extends AppCompatActivity implements View.OnClickListener{

    public final static String THEME_PREFERENCES = "THEME_PREFERENCES";
    public final static String CURRENT_THEME_KEY = "CURRENT_THEME_KEY";
    public final static String PRESSED_KEY = "PRESSED_KEY";
    public final static int [] mThemes = {R.style.AppThemeColor0,
            R.style.AppThemeColor1,
            R.style.AppThemeColor2,
            R.style.AppThemeColor3,
            R.style.AppThemeColor4,
            R.style.AppThemeColor5,
            R.style.AppThemeColor6,
            R.style.AppThemeColor7,
            R.style.AppThemeColor8};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_theme);
        View view0 = findViewById(R.id.themeLayout_0);
        View view1 = findViewById(R.id.themeLayout_1);
        View view2 = findViewById(R.id.themeLayout_2);
        View view3 = findViewById(R.id.themeLayout_3);
        View view4 = findViewById(R.id.themeLayout_4);
        View view5 = findViewById(R.id.themeLayout_5);
        View view6 = findViewById(R.id.themeLayout_6);
        View view7 = findViewById(R.id.themeLayout_7);
        View view8 = findViewById(R.id.themeLayout_8);
        view0.setOnClickListener(this);
        view1.setOnClickListener(this);
        view2.setOnClickListener(this);
        view3.setOnClickListener(this);
        view4.setOnClickListener(this);
        view5.setOnClickListener(this);
        view6.setOnClickListener(this);
        view7.setOnClickListener(this);
        view8.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.themeLayout_0:
                setPreferences(0, R.color.blueGrayPrimaryLight);
                break;
            case R.id.themeLayout_1:
                setPreferences(1, R.color.blueGrayPrimaryLight);
                break;
            case R.id.themeLayout_2:
                setPreferences(2, R.color.lightBluePrimaryLight);
                break;
            case R.id.themeLayout_3:
                setPreferences(3, R.color.redPrimaryLight);
                break;
            case R.id.themeLayout_4:
                setPreferences(4, R.color.brownPrimaryLight);
                break;
            case R.id.themeLayout_5:
                setPreferences(5, R.color.greenPrimaryLight);
                break;
            case R.id.themeLayout_6:
                setPreferences(6, R.color.purplePrimaryLight);
                break;
            case R.id.themeLayout_7:
                setPreferences(7, R.color.orangePrimaryLight);
                break;
            case R.id.themeLayout_8:
                setPreferences(8, R.color.pinkPrimaryLight);
                break;
        }
        setResult(RESULT_OK);
        finish();
    }

    private void setPreferences(int themeId, int pressId){
        SharedPreferences sharedPreferences = getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(CURRENT_THEME_KEY, themeId);
        editor.putInt(PRESSED_KEY, pressId);
        editor.commit();
    }
}
