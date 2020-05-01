package com.knst.calendar;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.knst.calendar.util.UpdateUtils;
import com.knst.calendar.util.Utils;

public class Widget2x2 extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.startEitherServiceOrWorker(context);
        UpdateUtils.update(context, false);
    }
}
