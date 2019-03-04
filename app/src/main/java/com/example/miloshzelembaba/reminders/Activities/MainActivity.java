package com.example.miloshzelembaba.reminders.Activities;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import com.example.miloshzelembaba.reminders.LocationReminderService;
import com.example.miloshzelembaba.reminders.Models.Reminder;
import com.example.miloshzelembaba.reminders.Models.User;
import com.example.miloshzelembaba.reminders.R;
import com.example.miloshzelembaba.reminders.RabbitMQ.DBService;
import com.example.miloshzelembaba.reminders.RabbitMQ.RabbitMQManager;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.ReminderService;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.UserService;
import com.example.miloshzelembaba.reminders.ReminderManager;
import com.example.miloshzelembaba.reminders.SharedPreferencesManager;
import com.example.miloshzelembaba.reminders.Utils.ApplicationUtil;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements ReminderService.RemindersListener, UserService.UserInfoListener {
    private DBService dbService = DBService.getInstance();
    private ReminderManager reminderManager = ReminderManager.getInstance();
    private SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance();
    GoogleMapFragment mapFragment = new GoogleMapFragment();
    ReminderFragment reminderFragment = new ReminderFragment();
    Intent locationServiceIntent;
    Intent rabbitMQService;

    private ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

    private ViewPager viewPager;
    private TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTasks();
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addReminder();
            }
        });

        viewPager = findViewById(R.id.mainViewPager);
        viewPager.setAdapter(pagerAdapter);
        tabLayout = findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {}
            @Override
            public void onPageSelected(int i) {
                if (i == 0) {
                    fab.show();
                    ScaleAnimation animation = new ScaleAnimation(0,1,0,1, fab.getWidth()/2, fab.getHeight()/2);
                    animation.setDuration(200);
                    fab.startAnimation(animation);
                    mapFragment.hideFabAnitmation();
                } else if (i == 1) {
                    ScaleAnimation animation = new ScaleAnimation(1,0,1,0, fab.getWidth()/2, fab.getHeight()/2);
                    animation.setDuration(200);
                    fab.startAnimation(animation);
                    fab.hide();
                    mapFragment.showFabAnitmation();
                }
            }
            @Override
            public void onPageScrollStateChanged(int i) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ApplicationUtil.getUser() != null) {
            dbService.getReminders();
            dbService.updateFCMToken();
        }
    }

    // used to start up important tasks/services and setsup the inital config
    private void initTasks() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (!(mWifi.isConnected() || mMobile.isConnected())) {
            Toast.makeText(this, "Please check your network connection", Toast.LENGTH_SHORT).show();
        }

        String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        rabbitMQService = new Intent(getBaseContext(), RabbitMQManager.class);
        rabbitMQService.putExtra("device_id", deviceID);
        startService(rabbitMQService);

        String[] LOCATION_PERMS = {
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        requestPermissions(LOCATION_PERMS, 1337); // todo: fix this

        locationServiceIntent = new Intent(getBaseContext(), LocationReminderService.class);
        if (!isMyServiceRunning(LocationReminderService.class)) {
            startService(locationServiceIntent);
        }

        dbService.addReminderListener(this);
        dbService.addReminderListener(reminderFragment);
        dbService.addAutocompleteListener(mapFragment);
        dbService.addReminderListener(mapFragment);
        dbService.addUserInfoListener(this);
        dbService.getUserInfo(deviceID);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReminderRefresh(ArrayList<Reminder> reminders, boolean error) {
        sharedPreferencesManager.saveReminders(this, reminders);
        reminderManager.updateReminders(reminders);
    }

    @Override
    public void onUserInfoReceived(User user, boolean error) {
        if (!error) {
            ApplicationUtil.setUser(user);
            dbService.getReminders();
            dbService.updateFCMToken();
        } else {
            Log.e("User", "error in getting user");
        }
    }

    @Override
    protected void onDestroy() {
        stopService(locationServiceIntent);
        stopService(rabbitMQService);
        super.onDestroy();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final int NUM_ITEMS = 2;

        ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return reminderFragment;
                case 1:
                    return mapFragment;
                default:
                    Log.e("getItem", "invalid position");
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            switch(position){
                case 0:
                    return getString(R.string.reminders);
                case 1:
                    return getString(R.string.location_reminders);
                default:
                    Log.e("getPageTitle", "invalid position");
                    return "";
            }
        }
    }

    private void addReminder() {
        Intent intent = new Intent(this, UpdateNormalReminderActivity.class);
        intent.putExtra("type", "normal");
        startActivity(intent);
    }
}
