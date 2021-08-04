package com.example.journaly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.journaly.create_screen.CreateActivity;
import com.example.journaly.databinding.ActivityMainBinding;
import com.example.journaly.goals_screen.GoalsChecker;
import com.example.journaly.home_screen.HomeFragment;
import com.example.journaly.login.AuthManager;
import com.example.journaly.login.LoginActivity;
import com.example.journaly.model.journals.FirebaseJournalRepository;
import com.example.journaly.model.journals.JournalRepository;
import com.example.journaly.model.users.FirebaseUsersRepository;
import com.example.journaly.model.users.Goal;
import com.example.journaly.model.users.UsersRepository;
import com.example.journaly.profile_screen.ProfileFragment;
import com.example.journaly.search_screen.SearchFragment;
import com.example.journaly.users_in_need_screen.UsersInNeedFragment;
import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.ShakeDetector;

import java.util.Optional;

import io.reactivex.rxjava3.functions.Consumer;


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


    private void performGoalChecking(){
        usersRepository.fetchUserGoal().take(1).subscribe(optionalGoal -> {
            if (!optionalGoal.isPresent()){
                return;
            }

            Goal goal = optionalGoal.get();
            String loggedInId = AuthManager.getInstance().getLoggedInUserId();
            GoalsChecker.isGoalMet(goal, loggedInId, journalRepository).subscribe(goalIsMet -> {
                if (!goalIsMet){
                    System.out.println("Goal is not met!!!"); //TODO: Send SMS
                }
            });

        }, throwable -> {
            Log.w(TAG, throwable);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sensey.getInstance().stop();
    }
}