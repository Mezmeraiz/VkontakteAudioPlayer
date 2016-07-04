package com.mezmeraiz.vkontakteaudioplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;
import com.mezmeraiz.vkontakteaudioplayer.services.PlayService;
import com.mezmeraiz.vkontakteaudioplayer.ui.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
public class PlayWidget extends AppWidgetProvider {

    public static final String DURATION_KEY = "DURATION_KEY";
    public static final String PROGRESS_KEY = "PROGRESS_KEY";
    public static final String ARTIST_KEY = "ARTIST_KEY";
    public static final String TITLE_KEY = "TITLE_KEY";
    public static final String PLAY_STATE_KEY = "PLAY_STATE_KEY";
    public static final String VISIBILITY_KEY = "VISIBILITY_KEY";
    public final static String WIDGET_PREF = "WIDGET_PREF";
    public final static String DESTROY_WIDGET_ACTION = "com.mezmeraiz.vkontakteaudioplayer.DESTROY_WIDGET_ACTION";
    public final static String CLICK_WIDGET_ACTION = "com.mezmeraiz.vkontakteaudioplayer.CLICK_WIDGET_ACTION";
    public final static String PLAY_BUTTON_KEY = "PLAY_BUTTON_KEY";
    public final static String PREV_BUTTON_KEY = "PREV_BUTTON_KEY";
    public final static String NEXT_BUTTON_KEY = "NEXT_BUTTON_KEY";
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Установка на виджет данных о проигрываемой композиции
        SharedPreferences sharedPreferences = context.getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.play_widget);
        views.setOnClickPendingIntent(R.id.imageViewWidgetPlay, createPendingIntent(context, PLAY_BUTTON_KEY));
        views.setOnClickPendingIntent(R.id.imageViewWidgetPrev, createPendingIntent(context, PREV_BUTTON_KEY));
        views.setOnClickPendingIntent(R.id.imageViewWidgetNext, createPendingIntent(context, NEXT_BUTTON_KEY));
        views.setTextViewText(R.id.textViewTitle, sharedPreferences.getString(TITLE_KEY, ""));
        views.setTextViewText(R.id.textViewArtist, sharedPreferences.getString(ARTIST_KEY, ""));
        views.setViewVisibility(R.id.imageViewWidgetNext, sharedPreferences.getInt(VISIBILITY_KEY, View.INVISIBLE));
        views.setViewVisibility(R.id.imageViewWidgetPlay, sharedPreferences.getInt(VISIBILITY_KEY, View.INVISIBLE));
        views.setViewVisibility(R.id.imageViewWidgetPrev, sharedPreferences.getInt(VISIBILITY_KEY, View.INVISIBLE));
        if(sharedPreferences.getBoolean(PLAY_STATE_KEY, false)){
            views.setImageViewResource(R.id.imageViewWidgetPlay, R.drawable.widget_pause);
        }else{
            views.setImageViewResource(R.id.imageViewWidgetPlay, R.drawable.widget_play);
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        mSharedPreferences = context.getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        switch (intent.getAction()){
            case Player.START_PLAYING_ACTION:
                // Сигнал о новой композиции
                mEditor.putString(TITLE_KEY, intent.getStringExtra(Player.TITLE_KEY));
                mEditor.putString(ARTIST_KEY, intent.getStringExtra(Player.ARTIST_KEY));
                mEditor.putInt(DURATION_KEY, intent.getIntExtra(Player.DURATION_KEY, 0));
                mEditor.putInt(PROGRESS_KEY, intent.getIntExtra(Player.PROGRESS_KEY, 0));
                mEditor.putInt(VISIBILITY_KEY, View.VISIBLE);
                mEditor.putBoolean(PLAY_STATE_KEY, intent.getBooleanExtra(Player.PLAY_STATE_KEY, false));
                mEditor.commit();
                updateWidgets(context);
                break;
            case Player.FAB_PRESSED_BACK_ACTION:
                // Сигнал о переключении иконки play/pause
                mEditor.putBoolean(PLAY_STATE_KEY, intent.getBooleanExtra(Player.FAB_STATE_KEY, false));
                mEditor.commit();
                updateWidgets(context);
                break;
            case DESTROY_WIDGET_ACTION:
                // Сигнал об отключении виджета после уничтожения PlayService
                mEditor.putString(TITLE_KEY, "");
                mEditor.putString(ARTIST_KEY, "");
                mEditor.putInt(DURATION_KEY, 0);
                mEditor.putInt(PROGRESS_KEY, 0);
                mEditor.putInt(VISIBILITY_KEY, View.INVISIBLE);
                mEditor.putBoolean(PLAY_STATE_KEY, false);
                mEditor.commit();
                updateWidgets(context);
                break;
            case CLICK_WIDGET_ACTION:
                // Обработка нажатий на кнопки виджета
                if (intent.hasCategory(PLAY_BUTTON_KEY)){
                    context.sendBroadcast(new Intent(MainActivity.FAB_PRESSED_SERVICE_ACTION));
                }else if(intent.hasCategory(PREV_BUTTON_KEY)){
                    context.sendBroadcast(new Intent(PlayService.PREV_SERVICE_ACTION));
                }else if(intent.hasCategory(NEXT_BUTTON_KEY)){
                    context.sendBroadcast(new Intent(PlayService.NEXT_SERVICE_ACTION));
                }
                break;
        }
    }

    private void updateWidgets(Context context){
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
        for (int i = 0; i < ids.length; i++){
            updateAppWidget(context, AppWidgetManager.getInstance(context), ids[i]);
        }
    }

    private static PendingIntent createPendingIntent(Context context, String category){
        Intent intent = new Intent(context, PlayWidget.class);
        intent.setAction(CLICK_WIDGET_ACTION);
        intent.addCategory(category);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pendingIntent;
    }



}
