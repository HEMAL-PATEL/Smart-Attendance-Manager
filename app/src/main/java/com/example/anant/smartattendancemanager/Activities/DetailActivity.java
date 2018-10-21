package com.example.anant.smartattendancemanager.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.anant.smartattendancemanager.Adapters.DetailsAdapter;
import com.example.anant.smartattendancemanager.DatabaseHelper;
import com.example.anant.smartattendancemanager.Fragments.AttendanceDialogFragment;
import com.example.anant.smartattendancemanager.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class DetailActivity extends AppCompatActivity implements
        AttendanceDialogFragment.NoticeDialogListener, DatabaseHelper.OnDataFetchedListener {

    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private String UID;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    private DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Details");
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                startLoginActivity();
                return;
            }
        } else {
            startLoginActivity();
            return;
        }

        mRecyclerView = findViewById(R.id.recycler_view_details);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new

                LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        UID = user.getUid();

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference("/users/" + UID + "/subjects");

        helper = new DatabaseHelper(UID, this);
        helper.getSubjects();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }


    @Override
    public void onDialogPositiveClick(int classAttended, int totalClasses) {
        helper.getSubjects();
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataFetched(Map<String, Object> map, boolean isSuccessful) {
        if (map != null) {
            DetailsAdapter detailsAdapter = new DetailsAdapter(map, new DetailsAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String key) {
                    DialogFragment newFragment = new AttendanceDialogFragment();
                    ((AttendanceDialogFragment) newFragment).setArguments(ref, key);
                    Toast.makeText(getApplicationContext(), key, Toast.LENGTH_SHORT).show();
                    newFragment.show(getSupportFragmentManager(), "attendance");

                }
            });
            mRecyclerView.setAdapter(detailsAdapter);
        }
    }
}
