package com.example.anant.smartattendancemanager.Activities;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.anant.smartattendancemanager.Adapters.DaysViewPagerAdapter;
import com.example.anant.smartattendancemanager.Adapters.DetailsAdapter;
import com.example.anant.smartattendancemanager.AttendanceAppWidget;
import com.example.anant.smartattendancemanager.Days;
import com.example.anant.smartattendancemanager.Fragments.AttendanceDialogFragment;
import com.example.anant.smartattendancemanager.Model.AttendanceModel;
import com.example.anant.smartattendancemanager.Model.SubjectsModel;
import com.example.anant.smartattendancemanager.Model.TimeTableModel;
import com.example.anant.smartattendancemanager.Presenter.DetailActivityPresenter;
import com.example.anant.smartattendancemanager.R;
import com.example.anant.smartattendancemanager.View.DetailsView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class DetailActivity extends AppCompatActivity implements
        AttendanceDialogFragment.NoticeDialogListener,
        DetailsView, DetailsAdapter.OnItemClickListener {

    private LinearLayoutManager mLayoutManager;
    private String UID;
    private FirebaseAuth mAuth;
    private boolean isTimeTable;

    private boolean adapterSet;

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.no_subject_scroll_view)
    NestedScrollView nestedScrollView;
    @BindView(R.id.recycler_view_details)
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar_title)
    Toolbar toolbar;
    @BindView(R.id.days_pager)
    ViewPager daysViewPager;

    private DetailActivityPresenter detailActivityPresenter;
    private DetailsAdapter detailsAdapter;
    private SubjectsModel subjectsModel;
    private TimeTableModel timeTableModel;
    private String[] days;
    private int pagerItemValue;
    private int criteria;
    private FirebaseUser user;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        days = getNames(Days.class);

        DaysViewPagerAdapter daysViewPagerAdapter = new DaysViewPagerAdapter(this);
        daysViewPager.setAdapter(daysViewPagerAdapter);

        daysViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                pagerItemValue = i;
                try {
                    detailActivityPresenter.fetchTimeTable(timeTableModel, days[i]);
                    nestedScrollView.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                } catch (ArrayIndexOutOfBoundsException e) {
                    nestedScrollView.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        detailActivityPresenter = new DetailActivityPresenter(this);
        detailActivityPresenter.checkLoggedIn(mAuth);

        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new

                LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        adapterSet = false;

        user = mAuth.getCurrentUser();

        if (user != null) {
            UID = user.getUid();
        } else {
            startLoginActivity();
            return;
        }

        isTimeTable = true;

        subjectsModel = new SubjectsModel(UID);
        timeTableModel = new TimeTableModel(UID);

        AttendanceModel attendanceModel = new AttendanceModel(UID);
        detailActivityPresenter.fetchAttendanceCriteria(attendanceModel);

        //Time table view by default
        navigationView.getMenu().getItem(0).setChecked(true);

        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    // set item as selected to persist highlight
                    menuItem.setChecked(true);
                    switch (menuItem.getItemId()) {
                        case R.id.nav_time_table:
                            isTimeTable = true;
                            swipeRefreshLayout.setRefreshing(true);
                            detailActivityPresenter.fetchDayPosition();
                            daysViewPager.setOnTouchListener(null);
                            break;
                        case R.id.nav_logout:
                            new AlertDialog.Builder(this)
                                    .setTitle(R.string.logout)
                                    .setMessage(R.string.logout_message)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> startLoginActivity())
                                    .setNegativeButton(android.R.string.no, null).show();
                            break;
                        case R.id.nav_all_subjects:
                            swipeRefreshLayout.setRefreshing(true);
                            isTimeTable = false;
                            detailActivityPresenter.fetchSubjects(subjectsModel);
                            daysViewPager.setOnTouchListener((arg0, arg1) -> true);
                            break;
                        case R.id.attendance_criteria:
                            Intent intent = new Intent(this, AttendanceActivity.class);
                            intent.putExtra("criteria", criteria);
                            startActivity(intent);
                            break;
                        case R.id.settings:
                            Intent settingsIntent = new Intent(this, SettingsActivity.class);
                            startActivity(settingsIntent);
                            break;
                    }
                    // close drawer when item is tapped
                    mDrawerLayout.closeDrawers();

                    // Add code here to update the UI based on the item selected
                    // For example, swap UI fragments here

                    return true;
                });

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        swipeRefreshLayout.setRefreshing(true);

        setNavigationHeader();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            if (!isTimeTable)
                detailActivityPresenter.fetchSubjects(subjectsModel);
            else swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setNavigationHeader() {

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.name_text_view);
        TextView navEmail = headerView.findViewById(R.id.email_text_view);
        CircleImageView imageView = headerView.findViewById(R.id.profile_pic);
        navEmail.setText(user.getEmail());
        navUsername.setText(user.getDisplayName());
        Glide.with(this)
                .load(user.getPhotoUrl())
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        imageView.setImageDrawable(resource);
                    }
                });
    }

    public String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.toString(e.getEnumConstants()).replaceAll("^.|.$", "").split(", ");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.logout) {
            startLoginActivity();
            return true;
        }

        if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void updateAppWidget() {
        Intent intent = new Intent(this, AttendanceAppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), AttendanceAppWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterSet = false;
    }

    @Override
    public void onDayPositionFetched(int position) {
        pagerItemValue = position;
        daysViewPager.setCurrentItem(position);
        if (!isTimeTable)
            detailActivityPresenter.fetchSubjects(subjectsModel);
        else {
            detailActivityPresenter.fetchTimeTable(timeTableModel, days[position]);
        }
    }

    @Override
    public void onLoginFailed() {
        startLoginActivity();
        return;
    }

    @Override
    public void onSubjectsAttendanceFetched(ArrayList<String> subjects, ArrayList<Integer> attended, ArrayList<Integer> totalClass) {
        detailsAdapter.setDataset(subjects, attended, totalClass);
        if (!adapterSet) {
            adapterSet = true;
            mRecyclerView.setAdapter(detailsAdapter);
        }
        detailsAdapter.notifyDataSetChanged();
        updateAppWidget();
        nestedScrollView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void startMainActivity() {
        swipeRefreshLayout.setRefreshing(false);
        if (!isTimeTable) {
            Log.wtf("checkStart", "starting From DetailActivity");
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            nestedScrollView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttendanceFetched(int criteria) {
        this.criteria = criteria;
        detailsAdapter = new DetailsAdapter(this, criteria);
        detailActivityPresenter.fetchDayPosition();
    }

    private void startLoginActivity() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        return;
    }

    @Override
    public void onItemClick(String key) {
        DialogFragment newFragment = new AttendanceDialogFragment();
        ((AttendanceDialogFragment) newFragment).setArguments(key);
        newFragment.show(getSupportFragmentManager(), "attendance");
    }

    @Override
    public void onAttendanceMarked(HashMap<String, Object> result) {
        updateAttendance(result);
    }

    @Override
    public void onDialogPositiveClick(HashMap<String, Object> result) {
        updateAttendance(result);
    }

    private void updateAttendance(HashMap<String, Object> result) {
        detailActivityPresenter.updateAttendance(result, subjectsModel);
        if (!isTimeTable)
            detailActivityPresenter.fetchSubjects(subjectsModel);
        else detailActivityPresenter.fetchTimeTable(timeTableModel, days[pagerItemValue]);
    }
}
