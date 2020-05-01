package com.knst.calendar.view.daypickerview;

import com.knst.calendar.util.CalendarType;

public interface DayPickerView {
    void setDayJdnOnView(long jdn);

    long getDayJdnFromView();

    CalendarType getSelectedCalendarType();

    void setOnSelectedDayChangedListener(OnSelectedDayChangedListener listener);

    interface OnSelectedDayChangedListener {
        void onSelectedDayChanged(long jdn);
    }
}
