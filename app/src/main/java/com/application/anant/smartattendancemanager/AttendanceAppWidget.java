package com.application.anant.smartattendancemanager;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.application.anant.smartattendancemanager.Activities.DetailActivity;
import com.application.anant.smartattendancemanager.Helper.WidgetHelper;

/**
 * Implementation of App Widget functionality.
 */
public class AttendanceAppWidget extends AppWidgetProvider {

    private static int[] drawables = {R.drawable.textview_design_green,
            R.drawable.textview_design_blue, R.drawable.textview_design_orange,
            R.drawable.textview_design_red};

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.attendance_app_widget);

        WidgetHelper widgetHelper = new WidgetHelper(context);
        String subject = widgetHelper.getLowAttendanceSubject();
        if (subject != null) {
            int classAttended = widgetHelper.getClassAttended();
            int classConducted = widgetHelper.getClassConducted();
            int percentage = widgetHelper.getLowestPercentage();
            int leave = widgetHelper.getCanLeaveClasses();

            views.setViewVisibility(R.id.subjects_layout, View.VISIBLE);
            views.setViewVisibility(R.id.login_add_sub_text_view, View.GONE);
            views.setTextViewText(R.id.subject_text_view, subject);
            views.setTextViewText(R.id.attendance_text_view, "Attendance : " + classAttended + "/" + classConducted);
            views.setTextViewText(R.id.percentage_text_view, Integer.toString(percentage));
            views.setTextViewText(R.id.leave_text_view, String.format(context.getString(R.string.leave_class), ((leave == 0) ? 0 : --leave)));
            views.setInt(R.id.percentage_text_view, "setBackgroundResource", getDrawable(percentage));

        } else {
            views.setViewVisibility(R.id.subjects_layout, View.GONE);
            views.setViewVisibility(R.id.login_add_sub_text_view, View.VISIBLE);
        }

        views.setOnClickPendingIntent(R.id.details_card_view, getPendingSelfIntent(context));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    public static PendingIntent getPendingSelfIntent(Context context) {
        Intent intent = new Intent(context, DetailActivity.class);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    private static int getDrawable(int percentageInt) {
        int drawable = 0;
        if (percentageInt >= 90) {
            drawable = drawables[0];
        } else if (percentageInt >= 75 && percentageInt < 90) {
            drawable = drawables[1];
        } else if (percentageInt >= 70 && percentageInt < 75) {
            drawable = drawables[2];
        } else if (percentageInt <= 70) {
            drawable = drawables[3];
        }
        return drawable;
    }
}

