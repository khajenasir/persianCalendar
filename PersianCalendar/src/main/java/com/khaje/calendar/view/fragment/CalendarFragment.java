package com.knst.calendar.view.fragment;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.knst.calendar.Constants;
import com.knst.calendar.R;
import com.knst.calendar.adapter.CalendarAdapter;
import com.knst.calendar.adapter.CardTabsAdapter;
import com.knst.calendar.adapter.TimeItemAdapter;
import com.knst.calendar.calendar.AbstractDate;
import com.knst.calendar.calendar.CivilDate;
import com.knst.calendar.databinding.EventsTabContentBinding;
import com.knst.calendar.databinding.FragmentCalendarBinding;
import com.knst.calendar.databinding.OwghatTabContentBinding;
import com.knst.calendar.di.dependencies.AppDependency;
import com.knst.calendar.di.dependencies.MainActivityDependency;
import com.knst.calendar.entity.AbstractEvent;
import com.knst.calendar.entity.DeviceCalendarEvent;
import com.knst.calendar.praytimes.Coordinate;
import com.knst.calendar.praytimes.PrayTimes;
import com.knst.calendar.praytimes.PrayTimesCalculator;
import com.knst.calendar.util.CalendarType;
import com.knst.calendar.util.Utils;
import com.knst.calendar.view.CalendarsView;
import com.knst.calendar.view.activity.MainActivity;
import com.knst.calendar.view.dialog.SelectDayDialog;
import com.knst.calendar.view.dialog.ShiftWorkDialog;
import com.cepmuvakkit.times.posAlgo.SunMoonPosition;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import dagger.android.support.DaggerFragment;

import static com.knst.calendar.Constants.CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE;
import static com.knst.calendar.Constants.PREF_HOLIDAY_TYPES;

public class CalendarFragment extends DaggerFragment {
    @Inject
    AppDependency appDependency; // same object from App
    @Inject
    MainActivityDependency mainActivityDependency; // same object from MainActivity
    boolean mFirstTime = true;
    private Calendar mCalendar = Calendar.getInstance();
    private Coordinate mCoordinate;
    private int mViewPagerPosition;
    private FragmentCalendarBinding mMainBinding;
    private CalendarsView mCalendarsView;
    private OwghatTabContentBinding mOwghatBinding;
    private EventsTabContentBinding mEventsBinding;
    private long mLastSelectedJdn = -1;
    private SearchView mSearchView;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private CalendarAdapter.CalendarAdapterHelper mCalendarAdapterHelper;
    private ViewPager.OnPageChangeListener mChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            sendBroadcastToMonthFragments(mCalendarAdapterHelper.positionToOffset(position), false);
            mMainBinding.todayButton.show();
        }

    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Context context = mainActivityDependency.getMainActivity();

        setHasOptionsMenu(true);

        mMainBinding = FragmentCalendarBinding.inflate(inflater, container, false);
        mViewPagerPosition = 0;

        List<String> titles = new ArrayList<>();
        List<View> tabs = new ArrayList<>();

        titles.add(getString(R.string.calendar));
        mCalendarsView = new CalendarsView(context);
        mCalendarsView.setOnCalendarsViewExpandListener(() -> mMainBinding.cardsViewPager.measureCurrentView(mCalendarsView));
        mCalendarsView.setOnShowHideTodayButton(show -> {
            if (show)
                mMainBinding.todayButton.show();
            else
                mMainBinding.todayButton.hide();
        });
        mMainBinding.todayButton.setOnClickListener(v -> bringTodayYearMonth());
        tabs.add(mCalendarsView);

        titles.add(getString(R.string.events));
        mEventsBinding = EventsTabContentBinding.inflate(inflater, container, false);
        tabs.add(mEventsBinding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            LayoutTransition layoutTransition = new LayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            mEventsBinding.eventsContent.setLayoutTransition(layoutTransition);
            // Don't do the same for others tabs, it is problematic
        }

        mCoordinate = Utils.getCoordinate(context);
        if (mCoordinate != null) {
            titles.add(getString(R.string.owghat));
            mOwghatBinding = OwghatTabContentBinding.inflate(inflater, container, false);
            tabs.add(mOwghatBinding.getRoot());
            mOwghatBinding.getRoot().setOnClickListener(this::onOwghatClick);
            mOwghatBinding.cityName.setOnClickListener(this::onOwghatClick);
            // Easter egg to test AthanActivity
            mOwghatBinding.cityName.setOnLongClickListener(v -> {
                Utils.startAthan(context, "FAJR");
                return true;
            });
            String cityName = Utils.getCityName(context, false);
            if (!TextUtils.isEmpty(cityName)) {
                mOwghatBinding.cityName.setText(cityName);
            }

            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
            layoutManager.setFlexWrap(FlexWrap.WRAP);
            layoutManager.setJustifyContent(JustifyContent.CENTER);
            mOwghatBinding.timesRecyclerView.setLayoutManager(layoutManager);
            mOwghatBinding.timesRecyclerView.setAdapter(new TimeItemAdapter());
        }

        mMainBinding.cardsViewPager.setAdapter(new CardTabsAdapter(getChildFragmentManager(),
                appDependency, tabs, titles));
        mMainBinding.tabLayout.setupWithViewPager(mMainBinding.cardsViewPager);

        mCalendarAdapterHelper = new CalendarAdapter.CalendarAdapterHelper(Utils.isRTL(context));
        mMainBinding.calendarViewPager.setAdapter(new CalendarAdapter(getChildFragmentManager(),
                mCalendarAdapterHelper));
        mCalendarAdapterHelper.gotoOffset(mMainBinding.calendarViewPager, 0);

        mMainBinding.calendarViewPager.addOnPageChangeListener(mChangeListener);

        int lastTab = appDependency.getSharedPreferences()
                .getInt(Constants.LAST_CHOSEN_TAB_KEY, Constants.CALENDARS_TAB);
        if (lastTab >= tabs.size()) {
            lastTab = Constants.CALENDARS_TAB;
        }

        mMainBinding.cardsViewPager.setCurrentItem(lastTab, false);

        AbstractDate today = Utils.getTodayOfCalendar(Utils.getMainCalendar());
        mainActivityDependency.getMainActivity().setTitleAndSubtitle(Utils.getMonthName(today),
                Utils.formatNumber(today.getYear()));

        return mMainBinding.getRoot();
    }

    void changeMonth(int position) {
        mMainBinding.calendarViewPager.setCurrentItem(
                mMainBinding.calendarViewPager.getCurrentItem() + position, true);
    }

    public void selectDay(long jdn) {
        mLastSelectedJdn = jdn;
        mCalendarsView.showCalendars(jdn, Utils.getMainCalendar(), Utils.getEnabledCalendarTypes());
        setOwghat(jdn, Utils.getTodayJdn() == jdn);
        showEvent(jdn);
    }

    public void addEventOnCalendar(long jdn) {
        MainActivity activity = mainActivityDependency.getMainActivity();

        CivilDate civil = new CivilDate(jdn);
        Calendar time = Calendar.getInstance();
        time.set(civil.getYear(), civil.getMonth() - 1, civil.getDayOfMonth());
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            Utils.askForCalendarPermission(activity);
        } else {
            try {
                startActivityForResult(
                        new Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.Events.DESCRIPTION, Utils.dayTitleSummary(
                                        Utils.getDateFromJdnOfCalendar(Utils.getMainCalendar(), jdn)))
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                        time.getTimeInMillis())
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                                        time.getTimeInMillis())
                                .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true),
                        CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE);
            } catch (Exception e) {
                Utils.createAndShowShortSnackbar(getView(), R.string.device_calendar_does_not_support);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        MainActivity activity = mainActivityDependency.getMainActivity();

        if (requestCode == CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE) {
            if (Utils.isShowDeviceCalendarEvents()) {
                sendBroadcastToMonthFragments(calculateViewPagerPositionFromJdn(mLastSelectedJdn), true);
            } else {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) {
                    Utils.askForCalendarPermission(activity);
                } else {
                    Utils.toggleShowDeviceCalendarOnPreference(activity, true);
                    activity.restartActivity();
                }
            }
        }
    }

    private void sendBroadcastToMonthFragments(int toWhich, boolean addOrModify) {
        appDependency.getLocalBroadcastManager().sendBroadcast(
                new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT)
                        .putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, toWhich)
                        .putExtra(Constants.BROADCAST_FIELD_EVENT_ADD_MODIFY, addOrModify)
                        .putExtra(Constants.BROADCAST_FIELD_SELECT_DAY_JDN, mLastSelectedJdn));
    }

    private SpannableString formatClickableEventTitle(DeviceCalendarEvent event) {
        String title = Utils.formatDeviceCalendarEventTitle(event);
        SpannableString ss = new SpannableString(title);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View textView) {
                try {
                    startActivityForResult(new Intent(Intent.ACTION_VIEW)
                                    .setData(ContentUris.withAppendedId(
                                            CalendarContract.Events.CONTENT_URI, event.getId())),
                            CALENDAR_EVENT_ADD_MODIFY_REQUEST_CODE);
                } catch (Exception e) { // Should be ActivityNotFoundException but we don't care really
                    Utils.createAndShowShortSnackbar(textView, R.string.device_calendar_does_not_support);
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                String color = event.getColor();
                if (!TextUtils.isEmpty(color)) {
                    try {
                        ds.setColor(Integer.parseInt(color));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        ss.setSpan(clickableSpan, 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    private SpannableStringBuilder getDeviceEventsTitle(List<AbstractEvent> dayEvents) {
        SpannableStringBuilder titles = new SpannableStringBuilder();
        boolean first = true;

        for (AbstractEvent event : dayEvents)
            if (event instanceof DeviceCalendarEvent) {
                if (first)
                    first = false;
                else
                    titles.append("\n");

                titles.append(formatClickableEventTitle((DeviceCalendarEvent) event));
            }

        return titles;
    }

    private void showEvent(long jdn) {
        mEventsBinding.shiftWorkTitle.setText(Utils.getShiftWorkTitle(jdn, false));

        List<AbstractEvent> events = Utils.getEvents(jdn,
                Utils.readDayDeviceEvents(mainActivityDependency.getMainActivity(), jdn));
        String holidays = Utils.getEventsTitle(events, true, false, false, false);
        String nonHolidays = Utils.getEventsTitle(events, false, false, false, false);
        SpannableStringBuilder deviceEvents = getDeviceEventsTitle(events);
        StringBuilder contentDescription = new StringBuilder();

        mEventsBinding.eventMessage.setVisibility(View.GONE);
        mEventsBinding.noEvent.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(holidays)) {
            mEventsBinding.noEvent.setVisibility(View.GONE);
            mEventsBinding.holidayTitle.setText(holidays);
            String holidayContent = getString(R.string.holiday_reason) + "\n" + holidays;
            mEventsBinding.holidayTitle.setContentDescription(holidayContent);
            contentDescription.append(holidayContent);
            mEventsBinding.holidayTitle.setVisibility(View.VISIBLE);
        } else {
            mEventsBinding.holidayTitle.setVisibility(View.GONE);
        }

        if (deviceEvents.length() != 0) {
            mEventsBinding.noEvent.setVisibility(View.GONE);
            mEventsBinding.deviceEventTitle.setText(deviceEvents);
            contentDescription.append("\n");
            contentDescription.append(getString(R.string.show_device_calendar_events));
            contentDescription.append("\n");
            contentDescription.append(deviceEvents);
            mEventsBinding.deviceEventTitle.setMovementMethod(LinkMovementMethod.getInstance());

            mEventsBinding.deviceEventTitle.setVisibility(View.VISIBLE);
        } else {
            mEventsBinding.deviceEventTitle.setVisibility(View.GONE);
        }


        if (!TextUtils.isEmpty(nonHolidays)) {
            mEventsBinding.noEvent.setVisibility(View.GONE);
            mEventsBinding.eventTitle.setText(nonHolidays);
            contentDescription.append("\n");
            contentDescription.append(getString(R.string.events));
            contentDescription.append("\n");
            contentDescription.append(nonHolidays);

            mEventsBinding.eventTitle.setVisibility(View.VISIBLE);
        } else {
            mEventsBinding.eventTitle.setVisibility(View.GONE);
        }

        SpannableStringBuilder messageToShow = new SpannableStringBuilder();

        Set<String> enabledTypes = appDependency.getSharedPreferences()
                .getStringSet(PREF_HOLIDAY_TYPES, new HashSet<>());
        if (enabledTypes == null || enabledTypes.size() == 0) {
            mEventsBinding.noEvent.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(messageToShow))
                messageToShow.append("\n");

            String title = getString(R.string.warn_if_events_not_set);
            SpannableString ss = new SpannableString(title);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View textView) {
                    mainActivityDependency.getMainActivity().navigateTo(R.id.settings);
                }
            };
            ss.setSpan(clickableSpan, 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            messageToShow.append(ss);

            contentDescription.append("\n");
            contentDescription.append(title);
        }

        if (!TextUtils.isEmpty(messageToShow)) {
            mEventsBinding.eventMessage.setText(messageToShow);
            mEventsBinding.eventMessage.setMovementMethod(LinkMovementMethod.getInstance());

            mEventsBinding.eventMessage.setVisibility(View.VISIBLE);
        }

        mEventsBinding.getRoot().setContentDescription(contentDescription);
    }

    private void setOwghat(long jdn, boolean isToday) {
        if (mCoordinate == null) {
            return;
        }

        CivilDate civilDate = new CivilDate(jdn);
        mCalendar.set(civilDate.getYear(), civilDate.getMonth() - 1, civilDate.getDayOfMonth());
        Date date = mCalendar.getTime();

        PrayTimes prayTimes = PrayTimesCalculator.calculate(Utils.getCalculationMethod(),
                date, mCoordinate);
        RecyclerView.Adapter adapter = mOwghatBinding.timesRecyclerView.getAdapter();
        if (adapter instanceof TimeItemAdapter) {
            ((TimeItemAdapter) adapter).setTimes(prayTimes);
        }

        double moonPhase = 1;
        try {
            moonPhase = new SunMoonPosition(Utils.getTodayJdn(), mCoordinate.getLatitude(),
                    mCoordinate.getLongitude(), 0, 0).getMoonPhase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mOwghatBinding.sunView.setSunriseSunsetMoonPhase(prayTimes, moonPhase);
        if (isToday) {
            mOwghatBinding.sunView.setVisibility(View.VISIBLE);
            if (mMainBinding.cardsViewPager.getCurrentItem() == Constants.OWGHAT_TAB) {
                mOwghatBinding.sunView.startAnimate(true);
            }
        } else {
            mOwghatBinding.sunView.setVisibility(View.GONE);
        }
    }

    private void onOwghatClick(View v) {
        RecyclerView.Adapter adapter = mOwghatBinding.timesRecyclerView.getAdapter();
        if (adapter instanceof TimeItemAdapter) {
            TimeItemAdapter timesAdapter = (TimeItemAdapter) adapter;
            boolean expanded = !timesAdapter.isExpanded();
            timesAdapter.setExpanded(expanded);
            mOwghatBinding.moreOwghat.setImageResource(expanded
                    ? R.drawable.ic_keyboard_arrow_up
                    : R.drawable.ic_keyboard_arrow_down);
        }
        mMainBinding.cardsViewPager.measureCurrentView(mOwghatBinding.getRoot());

        if (mLastSelectedJdn == -1)
            mLastSelectedJdn = Utils.getTodayJdn();
    }

    private void bringTodayYearMonth() {
        mLastSelectedJdn = -1;
        sendBroadcastToMonthFragments(Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY, false);

        mCalendarAdapterHelper.gotoOffset(mMainBinding.calendarViewPager, 0);

        selectDay(Utils.getTodayJdn());
    }

    public void afterShiftWorkChange() {
        Utils.updateStoredPreference(getContext());
        sendBroadcastToMonthFragments(calculateViewPagerPositionFromJdn(mLastSelectedJdn), true);
    }

    public void bringDate(long jdn) {
        Context context = getContext();
        if (context == null) return;

        mViewPagerPosition = calculateViewPagerPositionFromJdn(jdn);
        mCalendarAdapterHelper.gotoOffset(mMainBinding.calendarViewPager, mViewPagerPosition);

        selectDay(jdn);
        sendBroadcastToMonthFragments(mViewPagerPosition, false);

        if (Utils.isTalkBackEnabled()) {
            long todayJdn = Utils.getTodayJdn();
            if (jdn != todayJdn) {
                Utils.createAndShowShortSnackbar(getView(),
                        Utils.getA11yDaySummary(context, jdn,
                                false, null, true,
                                true, true));
            }
        }
    }

    private int calculateViewPagerPositionFromJdn(long jdn) {
        CalendarType mainCalendar = Utils.getMainCalendar();
        AbstractDate today = Utils.getTodayOfCalendar(mainCalendar);
        AbstractDate date = Utils.getDateFromJdnOfCalendar(mainCalendar, jdn);
        return (today.getYear() - date.getYear()) * 12 + today.getMonth() - date.getMonth();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.calendar_menu_buttons, menu);

        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setOnSearchClickListener(v -> {
            if (mSearchAutoComplete != null) mSearchAutoComplete.setOnItemClickListener(null);

            Context context = getContext();
            if (context == null) return;

            mSearchAutoComplete = mSearchView.findViewById(androidx.appcompat.R.id.search_src_text);
            mSearchAutoComplete.setHint(R.string.search_in_events);

            ArrayAdapter<AbstractEvent> eventsAdapter = new ArrayAdapter<>(context,
                    R.layout.suggestion, android.R.id.text1);
            eventsAdapter.addAll(Utils.getAllEnabledEvents());
            eventsAdapter.addAll(Utils.getAllEnabledAppointments(context));
            mSearchAutoComplete.setAdapter(eventsAdapter);
            mSearchAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
                AbstractEvent ev = (AbstractEvent) parent.getItemAtPosition(position);
                AbstractDate date = ev.getDate();
                CalendarType type = Utils.getCalendarTypeFromDate(date);
                AbstractDate today = Utils.getTodayOfCalendar(type);
                int year = date.getYear();
                if (year == -1) {
                    year = today.getYear() + (date.getMonth() < today.getMonth() ? 1 : 0);
                }
                bringDate(Utils.getDateOfCalendar(type, year, date.getMonth(), date.getDayOfMonth()).toJdn());
                mSearchView.onActionViewCollapsed();
            });
        });
    }

    private void destroySearchView() {
        if (mSearchView != null) {
            mSearchView.setOnSearchClickListener(null);
            mSearchView = null;
        }

        if (mSearchAutoComplete != null) {
            mSearchAutoComplete.setAdapter(null);
            mSearchAutoComplete.setOnItemClickListener(null);
            mSearchAutoComplete = null;
        }
    }

    @Override
    public void onDestroyOptionsMenu() {
        destroySearchView();
        super.onDestroyOptionsMenu();
    }

    @Override
    public void onDestroy() {
        destroySearchView();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.go_to:
                SelectDayDialog.newInstance(mLastSelectedJdn).show(getChildFragmentManager(),
                        SelectDayDialog.class.getName());
                break;
            case R.id.add_event:
                if (mLastSelectedJdn == -1)
                    mLastSelectedJdn = Utils.getTodayJdn();

                addEventOnCalendar(mLastSelectedJdn);
                break;
            case R.id.shift_work:
                ShiftWorkDialog.newInstance(mLastSelectedJdn).show(getChildFragmentManager(),
                        ShiftWorkDialog.class.getName());
                break;
            default:
                break;
        }
        return true;
    }

    int getViewPagerPosition() {
        return mViewPagerPosition;
    }

    public boolean closeSearch() {
        if (mSearchView != null && !mSearchView.isIconified()) {
            mSearchView.onActionViewCollapsed();
            return true;
        }
        return false;
    }

}
