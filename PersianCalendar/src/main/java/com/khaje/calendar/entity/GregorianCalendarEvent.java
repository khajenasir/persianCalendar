package com.knst.calendar.entity;

import com.knst.calendar.calendar.CivilDate;

public class GregorianCalendarEvent extends AbstractEvent<CivilDate> {
    public GregorianCalendarEvent(CivilDate date, String title, boolean holiday) {
        this.date = date;
        this.title = title;
        this.holiday = holiday;
    }
}