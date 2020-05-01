package com.knst.calendar.di.dependencies;

import com.knst.calendar.di.scopes.PerActivity;
import com.knst.calendar.view.activity.MainActivity;

import javax.inject.Inject;

@PerActivity
public final class MainActivityDependency {
    @Inject
    MainActivity activity;

    @Inject
    public MainActivityDependency() {
    }

    public MainActivity getMainActivity() {
        return activity;
    }
}
