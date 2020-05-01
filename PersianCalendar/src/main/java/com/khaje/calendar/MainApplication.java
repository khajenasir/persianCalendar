package com.knst.calendar;

import androidx.multidex.MultiDex;

import com.knst.calendar.di.AppComponent;
import com.knst.calendar.di.DaggerAppComponent;
import com.knst.calendar.util.Utils;
//import com.magnetadservices.sdk.MagnetSDK;
import com.pushpole.sdk.PushPole;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import ir.tapsell.sdk.Tapsell;
import me.cheshmak.android.sdk.advertise.CheshmakAds;
import me.cheshmak.android.sdk.core.Cheshmak;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class MainApplication extends DaggerApplication implements AppComponent {

    @Override
    public void onCreate() {
        super.onCreate();

        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/lcd.ttf");

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/vazir.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        Utils.initUtils(getApplicationContext());
        MultiDex.install(this);
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().create(this);
    }

    @Override
    public void inject(MainApplication instance) {

    }
}
