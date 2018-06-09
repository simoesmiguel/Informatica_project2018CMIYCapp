package com.example.currentplacedetailsonmap;


import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.SystemClock;


public class NewAlarmReceiver implements Timer {
    private long mTimeOut;
    private Intent mAlarmIntent;
    private Context mContext;
    private boolean mExact;
    private int mRequestNumber;

    private static int sTargetSdkVersion = -1;
    private static Handler sUiHandler = new Handler();

    private static final String EXTRA_OBJECT = "object";
    private static final String EXTRA_TIMEOUT = "timeout";
    private static final String EXTRA_EXACT = "exact";
    private static final String EXTRA_REQUEST_NUM = "requestNum";

    public static class AlarmReceiver extends BroadcastReceiver {

        public AlarmReceiver() {

        }

        @Override
        public void onReceive(Context ctx, Intent intent) {
            PersistentTimerCallback callback = (PersistentTimerCallback)intent.getParcelableExtra(EXTRA_OBJECT);
            boolean again = callback.tick();
            if(again) {
                long timeout = intent.getLongExtra(EXTRA_TIMEOUT, 0);
                boolean exact = intent.getBooleanExtra(EXTRA_EXACT, false);
                int requestNumber = intent.getIntExtra(EXTRA_REQUEST_NUM, 0);
                setSingleTimer(ctx, intent, timeout, exact, requestNumber);
            }
        }
    }

    public interface PersistentTimerCallback extends Timer.TimerCallback, Parcelable {

    }

    public NewAlarmReceiver(Context ctx, long ms, PersistentTimerCallback callback, int requestCode, boolean exact) {
        mTimeOut = ms;
        mContext = ctx;
        mExact = exact;
        mRequestNumber = requestCode;
        mAlarmIntent = new Intent(ctx, AlarmReceiver.class);
        mAlarmIntent.putExtra(EXTRA_OBJECT, callback);
        mAlarmIntent.putExtra(EXTRA_TIMEOUT, mTimeOut);
        mAlarmIntent.putExtra(EXTRA_REQUEST_NUM, mRequestNumber);
        PackageManager pm = ctx.getPackageManager();
        if(sTargetSdkVersion == -1) {
            try {
                ApplicationInfo applicationInfo = pm.getApplicationInfo(ctx.getPackageName(), 0);
                if (applicationInfo != null) {
                    sTargetSdkVersion = applicationInfo.targetSdkVersion;
                }
            } catch (PackageManager.NameNotFoundException ex) {
                sTargetSdkVersion = 1; //default to simplest
            }
        }
    }

    @TargetApi(23)
    private static void setSingleTimer(Context ctx, Intent intent, long timeout, boolean exact, int requestNumber) {
        PendingIntent pi = PendingIntent.getBroadcast(ctx, requestNumber, intent, 0);
        AlarmManager am =( AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        if(exact && sTargetSdkVersion  >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + timeout, pi);
        }
        else if (exact && sTargetSdkVersion >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + timeout, pi);
        }
        else {
            am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + timeout, pi);
        }
    }

    @Override
    public void start() {
        sUiHandler.post(new Runnable() {
            @Override
            public void run() {
                setSingleTimer(mContext, mAlarmIntent, mTimeOut, mExact, mRequestNumber);
            }
        });
    }

    @Override
    public void stop() {
        stop(mContext, mRequestNumber);
    }

    private static void doStop(Context context, final int requestCode) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, new Intent(context, AlarmReceiver.class), 0);
        am.cancel(pi);
    }

    static public void stop(final Context context, final int requestCode) {
        if(Looper.myLooper() != Looper.getMainLooper()) {
            sUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    doStop(context, requestCode);
                }
            });
        }
        else {
            doStop(context, requestCode);
        }
    }
}


