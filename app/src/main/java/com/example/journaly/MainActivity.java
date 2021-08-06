package com.example.journaly;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentManager;

import com.example.journaly.create_screen.CreateActivity;
import com.example.journaly.databinding.ActivityMainBinding;
import com.example.journaly.goals_screen.GoalsChecker;
import com.example.journaly.home_screen.HomeFragment;
import com.example.journaly.login.AuthManager;
import com.example.journaly.login.LoginActivity;
import com.example.journaly.model.journals.FirebaseJournalRepository;
import com.example.journaly.model.journals.JournalEntry;
import com.example.journaly.model.journals.JournalRepository;
import com.example.journaly.model.users.Contact;
import com.example.journaly.model.users.FirebaseUsersRepository;
import com.example.journaly.model.users.Goal;
import com.example.journaly.model.users.UsersRepository;
import com.example.journaly.notifications.NotificationSender;
import com.example.journaly.profile_screen.ProfileFragment;
import com.example.journaly.search_screen.SearchFragment;
import com.example.journaly.sms.SmsSender;
import com.example.journaly.users_in_need_screen.UsersInNeedFragment;
import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.ShakeDetector;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private HomeFragment homeFragment;
    private UsersInNeedFragment inNeedFragment;
    private ProfileFragment profileFragment;
    private SearchFragment searchFragment;
    private AlertDialog shakeDialog = null;
    private UsersRepository usersRepository;
    private JournalRepository journalRepository;
    private final int MY_PERMISSIONS_REQUEST_SEND_SMS = 120;
    private Contact contactToSendSms;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!AuthManager.getInstance().isLoggedIn()) {
            goToLogin();
            finish();
        }

        usersRepository = FirebaseUsersRepository.getInstance();
        journalRepository = FirebaseJournalRepository.getInstance();

        initShakeDetection();
        initViews();
        performGoalChecking();
        new NotificationSender(this).createNotificationChannel();

//        new NotificationSender(this).sendJournalReminderNotification();
    }

    private void initShakeDetection() {
        Sensey.getInstance().init(this);
        ShakeDetector.ShakeListener shakeListener = new ShakeDetector.ShakeListener() {
            @Override
            public void onShakeDetected() {
            }

            @Override
            public void onShakeStopped() {
                showShakeDetectedDialog();
            }
        };
        Sensey.getInstance().startShakeDetection(4, 500, shakeListener);
    }

    private void showShakeDetectedDialog() {
        if (shakeDialog != null && shakeDialog.isShowing()) {
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("It looks like you're shaking your phone. Would you like to write a journal entry?");
        builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel());
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Intent i = new Intent(MainActivity.this, CreateActivity.class);
            i.putExtra(CreateActivity.STATE_INTENT_KEY, CreateActivity.Mode.CREATE);
            startActivity(i);
        });
        shakeDialog = builder.create();
        shakeDialog.show();
    }

    private void initViews() {
        homeFragment = HomeFragment.newInstance();
        inNeedFragment = UsersInNeedFragment.newInstance();
        profileFragment = ProfileFragment.newInstance(AuthManager.getInstance().getLoggedInUserId(), false);
        searchFragment = SearchFragment.newInstance();

        initBottomNavigation();
        initFab();
    }

    private void initFab() {
        binding.createJournalFab.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, CreateActivity.class);
            i.putExtra(CreateActivity.STATE_INTENT_KEY, CreateActivity.Mode.CREATE);
            startActivity(i);
        });
    }

    private void initBottomNavigation() {
        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.getMenu().getItem(2).setEnabled(false);

        FragmentManager fm = getSupportFragmentManager();
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    fm.beginTransaction().replace(R.id.main_fragment_container_view, homeFragment).commit();
                    break;
                case R.id.menu_in_need:
                    fm.beginTransaction().replace(R.id.main_fragment_container_view, inNeedFragment).commit();
                    break;
                case R.id.menu_search:
                    fm.beginTransaction().replace(R.id.main_fragment_container_view, searchFragment).commit();
                    break;
                case R.id.menu_profile:
                    fm.beginTransaction().replace(R.id.main_fragment_container_view, profileFragment).commit();
                    break;
                default:
                    throw new RuntimeException("Unreachable");

            }

            return true;
        });

        binding.bottomNavigationView.setSelectedItemId(R.id.menu_home);
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        //Flags prevent user from returning to MainActivityView when pressing back button
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    private void performGoalChecking() {
        String loggedInId = AuthManager.getInstance().getLoggedInUserId();
        Observable<List<JournalEntry>> entriesObservable = journalRepository.fetch().take(1);
        Observable<Optional<Goal>> goalsObservable = usersRepository.fetchUserGoal().take(1);

        Observable
                .zip(entriesObservable, goalsObservable, (entries, optionalGoal) -> {
                    if (!optionalGoal.isPresent()) {
                        throw new NoSuchElementException();
                    }

                    entries = entries.stream().filter(journalEntry -> journalEntry.getUserId().equals(loggedInId)).collect(Collectors.toList());
                    contactToSendSms = optionalGoal.get().getContact();
                    return new Pair<>(optionalGoal.get(), entries);
                })
                .map(goalListPair -> new Pair<>(GoalsChecker.isGoalMet(goalListPair.first, goalListPair.second), goalListPair.first))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            boolean goalIsMet = result.first;
                            Goal goal = result.second;
                            if (!goalIsMet) {
                                System.out.println("goal not met");
                                contactToSendSms = goal.getContact();
                                sendSms();

                                goal.setLastFailTime(System.currentTimeMillis());
                                usersRepository.updateGoal(goal).subscribe();
                            }
                        }, throwable -> {
                            if (throwable instanceof NoSuchElementException) {
                                return;
                            }

                            Log.w(TAG, throwable);
                        });

    }

    private void sendSms() {
        requestSmsPermission();
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
                SmsSender.sendSms(contactToSendSms);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsSender.sendSms(contactToSendSms);
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sensey.getInstance().stop();
    }
}