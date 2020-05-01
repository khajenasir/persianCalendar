package com.knst.calendar.praytimes;

public class MinuteOrAngleDouble {
    private final boolean isMin;
    private final double value;

    MinuteOrAngleDouble(double value, boolean isMinute) {
        this.value = value;
        this.isMin = isMinute;
    }

    public boolean isMinute() {
        return isMin;
    }

    public double getValue() {
        return value;
    }
}
