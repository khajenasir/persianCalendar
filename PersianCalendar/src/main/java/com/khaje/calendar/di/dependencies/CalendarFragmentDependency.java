package com.knst.calendar.di.dependencies;

import com.knst.calendar.di.scopes.PerFragment;
import com.knst.calendar.view.activity.MainActivity;
import com.knst.calendar.view.fragment.CalendarFragment;
import com.knst.calendar.view.itemdayview.DaysPaintResources;

import javax.inject.Inject;

@PerFragment
public final class CalendarFragmentDependency {
    private final DaysPaintResources daysPaintResources;

    @Inject
    CalendarFragment calendarFragment;

    @Inject
    public CalendarFragmentDependency(MainActivity activity) {
        daysPaintResources = new DaysPaintResources(activity);
    }

    public CalendarFragment getCalendarFragment() {
        return calendarFragment;
    }

    public DaysPaintResources getDaysPaintResources() {
        return daysPaintResources;
    }
}
