//package com.knst.calendar.service;
//
//import com.knst.calendar.util.UpdateUtils;
//import com.knst.calendar.util.Utils;
//
//import androidx.work.Worker;
//
//public class UpdateWorker extends Worker {
//    @Override
//    public Worker.Result doWork() {
//        Utils.setChangeDateWorker();
//        Utils.updateStoredPreference(getApplicationContext());
//        UpdateUtils.update(getApplicationContext(), true);
//        return Result.SUCCESS;
//    }
//}