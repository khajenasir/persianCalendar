package com.knst.calendar.di.modules;

import com.knst.calendar.di.scopes.PerChildFragment;
import com.knst.calendar.view.dialog.SelectDayDialog;
import com.knst.calendar.view.dialog.ShiftWorkDialog;
import com.knst.calendar.view.fragment.MonthFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class CalendarFragmentModule {
    @PerChildFragment
    @ContributesAndroidInjector(modules = MainChildFragmentModule.class)
    abstract MonthFragment monthFragmentInjector();

    @PerChildFragment
    @ContributesAndroidInjector
    abstract SelectDayDialog selectDayDialogInjector();

    @PerChildFragment
    @ContributesAndroidInjector
    abstract ShiftWorkDialog shiftWorkDialogInjector();
}
