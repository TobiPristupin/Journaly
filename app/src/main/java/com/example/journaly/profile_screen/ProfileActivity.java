package com.example.journaly.profile_screen;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.journaly.R;
import com.example.journaly.databinding.ActivityProfileBinding;

/*
This class is nothing more than a wrapper for ProfileFragment. Sometimes it is nice to have an
activity instead of a fragment since an activity can handle back navigation and does not require
a fragment container nor a complicated fragment transaction, only an intent.
*/
public class ProfileActivity extends AppCompatActivity {

    public static final String INTENT_USER_ID_KEY = "userId";
    private ActivityProfileBinding binding;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getIntent().getStringExtra(INTENT_USER_ID_KEY);

        Fragment profileFragment = ProfileFragment.newInstance(userId, true);
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.profile_fragment_container, profileFragment).commit();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}