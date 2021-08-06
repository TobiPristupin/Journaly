package com.example.journaly.profile_screen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.journaly.databinding.FragmentProfileGoalsBinding;
import com.example.journaly.goals_screen.CreateGoalActivity;
import com.example.journaly.model.users.FirebaseUsersRepository;
import com.example.journaly.model.users.Goal;
import com.example.journaly.model.users.User;
import com.example.journaly.model.users.UsersRepository;
import com.example.journaly.notifications.AlarmSender;
import com.example.journaly.utils.AnimationUtils;

import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class ProfileGoalsFragment extends Fragment {

    public static final String TAG = "ProfileGoalsFragment";
    private FragmentProfileGoalsBinding binding;
    private UsersRepository usersRepository;

    private CompositeDisposable disposable;


    public ProfileGoalsFragment() {
        // Required empty public constructor
    }

    public static ProfileGoalsFragment newInstance(User user) {
        return new ProfileGoalsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usersRepository = FirebaseUsersRepository.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileGoalsBinding.inflate(inflater, container, false);

        initViews();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        disposable = new CompositeDisposable();
        subscribeToGoalData();
    }

    @Override
    public void onStop() {
        super.onStop();
//        disposable.dispose(); //TODO: FIX NPE
    }

    private void initViews() {
        initNoGoalUI();
        binding.noGoalButton.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), CreateGoalActivity.class);
            startActivity(i);
        });

        binding.deleteGoalIcon.setOnClickListener(v -> {
            usersRepository.deleteGoal().subscribe();
            new AlarmSender(getContext()).cancelAlarm();
        });
    }

    private void subscribeToGoalData(){
        disposable.add(usersRepository.fetchUserGoal().subscribe(goal -> {
            updateGoalUI(goal);
        }, throwable -> {
            Log.w(TAG, throwable);
        }));
    }

    private void updateGoalUI(Optional<Goal> optionalGoal){
        if (!optionalGoal.isPresent()){
            initNoGoalUI();
            return;
        }


        Goal goal = optionalGoal.get();
        initGoalUI();

        String frequencyText = goal.getTimesFrequency() + " time(s) every " + goal.getDaysFrequency() + " day(s)";
        binding.goalFrequencyText.setText(frequencyText);

        String reminderText = "Not set";
        if (goal.remindersEnabled()){
            String reminderTime = String.format("%02d", goal.getReminderHour()) + ":" + String.format("%02d", goal.getReminderMinute());
            reminderText = goal.getReminderDays().stream().collect(Collectors.joining(", ")) + " at " + reminderTime;
        }
        binding.goalReminderText.setText(reminderText);

        if (goal.contactMessageEnabled()){
            binding.goalMessageText.setVisibility(View.VISIBLE);
            binding.goalContactSelectedText.setText(goal.getContact().getName());
            binding.goalMessageText.setText(goal.getContact().getMessageToSend());
        } else {
            binding.goalMessageText.setVisibility(View.GONE);
            binding.goalContactSelectedText.setText("No Contact Selected");
        }
    }

    private void initNoGoalUI(){
        binding.goalMainContainer.setVisibility(View.GONE);
        AnimationUtils.fadeIn(500, 500, binding.noGoalImage, binding.noGoalButton, binding.noGoalText);
    }

    private void initGoalUI(){
        binding.goalMainContainer.setVisibility(View.VISIBLE);
        AnimationUtils.fadeOut(300, () -> {
            binding.goalMainContainer.setVisibility(View.VISIBLE);
        }, binding.noGoalImage, binding.noGoalButton, binding.noGoalText);
    }

}