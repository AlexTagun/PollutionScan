package com.hse.pollution_scan;

import android.app.Application;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

public class MyApp extends Application {

    private static final String YANDEX_API_KEY = "4e944f95-485c-42e3-92f4-73735a13e55f";

    @Override
    public void onCreate() {
        super.onCreate();

        initYandexAppMetrica();
    }

    private void  initYandexAppMetrica(){
        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(YANDEX_API_KEY).build();
        // Initializing the AppMetrica SDK.
        YandexMetrica.activate(getApplicationContext(), config);
        // Automatic tracking of user activity.
        YandexMetrica.enableActivityAutoTracking(this);
    }
}
