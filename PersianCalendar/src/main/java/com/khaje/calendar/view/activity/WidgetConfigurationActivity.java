package com.knst.calendar.view.activity;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.knst.calendar.R;
import com.knst.calendar.databinding.WidgetPreferenceLayoutBinding;
import com.knst.calendar.util.UpdateUtils;
import com.knst.calendar.util.Utils;
import com.knst.calendar.view.preferences.FragmentWidgetNotification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class WidgetConfigurationActivity extends AppCompatActivity {
    protected void finishAndSuccess() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int appwidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
            setResult(RESULT_OK, new Intent()
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appwidgetId));
        }
        Utils.updateStoredPreference(this);
        UpdateUtils.update(this, false);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Don't replace below with appDependency.getSharedPreferences() ever
        // as the injection won't happen at the right time
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(Utils.getThemeFromName(Utils.getThemeFromPreference(prefs)));

        Utils.applyAppLanguage(this);
        super.onCreate(savedInstanceState);
        WidgetPreferenceLayoutBinding binding =
                DataBindingUtil.setContentView(this, R.layout.widget_preference_layout);

        getSupportFragmentManager().beginTransaction().add(
                R.id.preference_fragment_holder,
                new FragmentWidgetNotification(), "TAG").commit();

        binding.addWidgetButton.setOnClickListener(v -> finishAndSuccess());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
