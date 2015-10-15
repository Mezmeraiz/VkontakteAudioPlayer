package com.mezmeraiz.vkontakteaudioplayer.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;
import com.vk.sdk.dialogs.VKCaptchaDialog;
import com.vk.sdk.util.VKUtil;

/**
 * Created by pc on 14.10.2015.
 */
public class LoginActivity extends AppCompatActivity{

    private static final String[] scope = {VKScope.AUDIO};
    public static final String APP_ID = "5106321";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VKUIHelper.onCreate(this);
        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        Log.d("myLogs", fingerprints[0]);
        // Инициализация SDK - передаем слушатель и id standalone-приложения
        VKSdk.initialize(listener, APP_ID);
        // Если token в наличии - запускаем активность, если нет - авторизацию
        if (VKSdk.wakeUpSession()) {
            startMainActivity();
        }else{
            authorize();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Если получен ответ от VKOpenAuthActivity - вызывается один из методов слушателя listener
        super.onActivityResult(requestCode, resultCode, data);
        VKUIHelper.onActivityResult(this,requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
    }

    // Слушатель для VKSdk
    VKSdkListener listener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            super.onReceiveNewToken(newToken);
            // Получение нового токена после авторицации и запуск активности
            startMainActivity();

        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            super.onAcceptUserToken(token);
            // Точно не понял, но вроде что о хорошее - запускаем активность
            startMainActivity();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            // Токен просрочен, запуск авторизации
            authorize();
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            new AlertDialog.Builder(VKUIHelper.getTopActivity())
                    .setMessage(authorizationError.toString())
                    .show();
        }
    };

    private void authorize(){
        // Запускаем авторизацию со списком разрешений. Результат - запуск VKOpenAuthActivity
        VKSdk.authorize(scope,true,false);
    }


    private void startMainActivity(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}
