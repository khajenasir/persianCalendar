package com.knst.calendar.di.modules;

import com.knst.calendar.di.scopes.PerFragment;
import com.knst.calendar.view.dialog.preferredcalendars.CalendarPreferenceDialog;
import com.knst.calendar.view.fragment.AboutFragment;
import com.knst.calendar.view.fragment.CalendarFragment;
import com.knst.calendar.view.fragment.CompassFragment;
import com.knst.calendar.view.fragment.ConverterFragment;
import com.knst.calendar.view.fragment.DeviceInfoFragment;
import com.knst.calendar.view.preferences.FragmentInterfaceCalendar;
import com.knst.calendar.view.preferences.FragmentLocationAthan;
import com.knst.calendar.view.preferences.GPSLocationDialog;
import com.knst.calendar.view.preferences.SettingsFragment;

import net.androgames.level.LevelFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainActivityModule {

    @PerFragment
    @ContributesAndroidInjector(modules = CalendarFragmentModule.class)
    abstract CalendarFragment calendarFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract SettingsFragment settingsFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract CompassFragment compassFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract LevelFragment levelFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract AboutFragment aboutFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract DeviceInfoFragment deviceInfoFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract ConverterFragment converterFragmentInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract FragmentLocationAthan fragmentLocationAthanInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract FragmentInterfaceCalendar fragmentInterfaceCalendarInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract CalendarPreferenceDialog calendarPreferenceDialogInjector();

    @PerFragment
    @ContributesAndroidInjector
    abstract GPSLocationDialog gpsLocationDialogInjector();
}
