package com.knst.calendar.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.knst.calendar.R;
import com.knst.calendar.databinding.FragmentConverterBinding;
import com.knst.calendar.di.dependencies.MainActivityDependency;
import com.knst.calendar.util.CalendarType;
import com.knst.calendar.util.Utils;
import com.knst.calendar.view.daypickerview.DayPickerView;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dagger.android.support.DaggerFragment;

public class ConverterFragment extends DaggerFragment {
    @Inject
    MainActivityDependency mainActivityDependency;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mainActivityDependency.getMainActivity().setTitleAndSubtitle(getString(R.string.date_converter), "");

        FragmentConverterBinding binding = FragmentConverterBinding.inflate(inflater,
                container, false);
        DayPickerView dayPickerView = binding.dayPickerView;

        binding.calendarsView.expand(true);
        binding.calendarsView.hideMoreIcon();
        binding.todayButton.setOnClickListener(v -> dayPickerView.setDayJdnOnView(Utils.getTodayJdn()));
        binding.calendarsView.setOnShowHideTodayButton(show -> {
            if (show)
                binding.todayButton.show();
            else
                binding.todayButton.hide();
        });

        dayPickerView.setOnSelectedDayChangedListener(jdn -> {
            if (jdn == -1) {
                binding.calendarsView.setVisibility(View.GONE);
            } else {
                binding.calendarsView.setVisibility(View.VISIBLE);
                CalendarType selectedCalendarType = dayPickerView.getSelectedCalendarType();
                List<CalendarType> orderedCalendarTypes = Utils.getOrderedCalendarTypes();
                orderedCalendarTypes.remove(selectedCalendarType);
                binding.calendarsView.showCalendars(jdn, selectedCalendarType, orderedCalendarTypes);
            }
        });
        dayPickerView.setDayJdnOnView(Utils.getTodayJdn());

        return binding.getRoot();
    }
}
