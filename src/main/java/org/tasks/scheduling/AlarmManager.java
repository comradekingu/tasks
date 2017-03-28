package org.tasks.scheduling;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;

import org.tasks.injection.ForApplication;
import org.tasks.preferences.Preferences;
import org.tasks.time.DateTime;

import javax.inject.Inject;

import static com.todoroo.andlib.utility.AndroidUtilities.atLeastKitKat;
import static com.todoroo.andlib.utility.AndroidUtilities.atLeastMarshmallow;

public class AlarmManager {

    private final android.app.AlarmManager alarmManager;
    private final Preferences preferences;

    @Inject
    public AlarmManager(@ForApplication Context context, Preferences preferences) {
        this.preferences = preferences;
        alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void cancel(PendingIntent pendingIntent) {
        alarmManager.cancel(pendingIntent);
    }

    public void wakeupAdjustingForQuietHours(long time, PendingIntent pendingIntent) {
        wakeup(adjustForQuietHours(time), pendingIntent);
    }

    @SuppressLint("NewApi")
    public void wakeup(long time, PendingIntent pendingIntent) {
        if (atLeastMarshmallow()) {
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else if (atLeastKitKat()) {
            alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else {
            alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
    }

    @SuppressLint("NewApi")
    public void noWakeup(long time, PendingIntent pendingIntent) {
        if (atLeastMarshmallow()) {
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC, time, pendingIntent);
        } else if (atLeastKitKat()) {
            alarmManager.setExact(android.app.AlarmManager.RTC, time, pendingIntent);
        } else {
            alarmManager.set(android.app.AlarmManager.RTC, time, pendingIntent);
        }
    }

    long adjustForQuietHours(long time) {
        if (preferences.quietHoursEnabled()) {
            DateTime dateTime = new DateTime(time);
            DateTime start = dateTime.withMillisOfDay(preferences.getQuietHoursStart());
            DateTime end = dateTime.withMillisOfDay(preferences.getQuietHoursEnd());
            if (start.isAfter(end)) {
                if (dateTime.isBefore(end)) {
                    return end.getMillis();
                } else if (dateTime.isAfter(start)) {
                    return end.plusDays(1).getMillis();
                }
            } else {
                if (dateTime.isAfter(start) && dateTime.isBefore(end)) {
                    return end.getMillis();
                }
            }
        }
        return time;
    }
}
