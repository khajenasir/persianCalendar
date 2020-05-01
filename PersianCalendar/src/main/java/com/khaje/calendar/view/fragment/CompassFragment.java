package com.knst.calendar.view.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.knst.calendar.R;
import com.knst.calendar.databinding.FragmentCompassBinding;
import com.knst.calendar.di.dependencies.MainActivityDependency;
import com.knst.calendar.praytimes.Coordinate;
import com.knst.calendar.util.Utils;
import com.knst.calendar.view.activity.MainActivity;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dagger.android.support.DaggerFragment;

/**
 * Compass/Qibla activity
 *
 * @author ebraminio
 */
public class CompassFragment extends DaggerFragment {
    public boolean stop = false;
    @Inject
    MainActivityDependency mainActivityDependency;
    private SensorManager sensorManager;
    private Sensor sensor;
    private float orientation = 0;
    private FragmentCompassBinding binding;
    private SensorEventListener compassListener = new SensorEventListener() {
        /*
         * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
         * value basically means more smoothing See:
         * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
         */
        static final float ALPHA = 0.15f;
        float azimuth;

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // angle between the magnetic north direction
            // 0=North, 90=East, 180=South, 270=West
            float angle = event.values[0] + orientation;
            if (stop) angle = 0;
            else binding.compassView.isOnDirectionAction();

            azimuth = lowPass(angle, azimuth);
            binding.compassView.setBearing(azimuth);
        }

        /**
         * https://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
         * http://developer.android.com/reference/android/hardware/SensorEvent.html#values
         */
        private float lowPass(float input, float output) {
            if (Math.abs(180 - input) > 170) {
                return input;
            }
            return output + ALPHA * (input - output);
        }
    };
    private boolean sensorNotFound = false;
    private Coordinate coordinate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCompassBinding.inflate(inflater, container, false);

        coordinate = Utils.getCoordinate(mainActivityDependency.getMainActivity());

        mainActivityDependency.getMainActivity().setTitleAndSubtitle(getString(R.string.compass),
                Utils.getCityName(mainActivityDependency.getMainActivity(), true));
        setCompassMetrics();

        if (coordinate != null) {
            binding.compassView.setLongitude(coordinate.getLongitude());
            binding.compassView.setLatitude(coordinate.getLatitude());
            binding.compassView.initCompassView();
        }

        binding.bottomAppbar.replaceMenu(R.menu.compass_menu_buttons);
        binding.bottomAppbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.level:
                    mainActivityDependency.getMainActivity().navigateTo(R.id.level);
                    break;
                case R.id.help:
                    Utils.createAndShowSnackbar(getView(), mainActivityDependency.getMainActivity()
                                    .getString(sensorNotFound
                                            ? R.string.compass_not_found : R.string.calibrate_compass_summary),
                            5000);
                default:
                    break;
            }
            return true;
        });
        binding.fab.setOnClickListener(v -> {
            stop = !stop;
            binding.fab.setImageResource(stop ? R.drawable.ic_play : R.drawable.ic_stop);
            binding.fab.setContentDescription(mainActivityDependency.getMainActivity()
                    .getString(stop ? R.string.resume : R.string.stop));
        });

        return binding.getRoot();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCompassMetrics();
    }

    private void setCompassMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();

        mainActivityDependency.getMainActivity().getWindowManager()
                .getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        binding.compassView.setScreenResolution(width, height - 2 * height / 8);

        WindowManager wm = (WindowManager) mainActivityDependency.getMainActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return;
        }

        switch (wm.getDefaultDisplay().getOrientation()) {
            case Surface.ROTATION_0:
                orientation = 0;
                break;
            case Surface.ROTATION_90:
                orientation = 90;
                break;
            case Surface.ROTATION_180:
                orientation = 180;
                break;
            case Surface.ROTATION_270:
                orientation = 270;
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity mainActivity = mainActivityDependency.getMainActivity();
        sensorManager = (SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            if (sensor != null) {
                sensorManager.registerListener(compassListener, sensor,
                        SensorManager.SENSOR_DELAY_FASTEST);
                if (coordinate == null) {
                    Utils.createAndShowShortSnackbar(mainActivity.getCoordinator(), R.string.set_location);
                }
            } else {
                Utils.createAndShowShortSnackbar(getView(), R.string.compass_not_found);
                sensorNotFound = true;
            }
        }
    }

    @Override
    public void onPause() {
        if (sensor != null) {
            sensorManager.unregisterListener(compassListener);
        }
        super.onPause();
    }
}
