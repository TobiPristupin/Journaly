package com.example.journaly.create_screen;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.journaly.R;
import com.example.journaly.databinding.ActivityCreateBinding;
import com.example.journaly.login.AuthManager;
import com.example.journaly.model.cloud_storage.CloudStorageManager;
import com.example.journaly.model.journals.FirebaseJournalRepository;
import com.example.journaly.model.journals.JournalEntry;
import com.example.journaly.model.journals.JournalRepository;
import com.example.journaly.model.journals.Mood;
import com.example.journaly.model.nlp.CloudNlpClient;
import com.example.journaly.model.nlp.NlpRepository;
import com.example.journaly.utils.BitmapUtils;
import com.example.journaly.utils.DateUtils;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import io.reactivex.rxjava3.core.Maybe;

public class CreateActivity extends AppCompatActivity {

    //a state for this activity must be passed by the calling intent.
    public enum Mode {
        CREATE, //creating a new entry
        EDIT, //editing a new entry (assumes current user is owner of entry being edited)
        VIEW //viewing an entry, when current user != owner of entry
    }

    private ActivityCreateBinding binding;
    private static final int REQUEST_IMAGE_CAPTURE_CODE = 1;
    private static final String TAG = "CreateActivity";
    private File photoFile = null;
    private JournalRepository journalRepository = FirebaseJournalRepository.getInstance();
    private NlpRepository nlpRepository = CloudNlpClient.getInstance();
    public static final String STATE_INTENT_KEY = "state";
    public static final String JOURNAL_ENTRY_INTENT_KEY = "entry";
    @Nullable
    private JournalEntry intentJournalEntry; //pased from intent
    @NotNull
    private CreateActivity.Mode mode; //passed from intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractDataFromIntent();
        initViews();
    }

    private void extractDataFromIntent() {
        Intent intent = getIntent();
        mode = (Mode) intent.getSerializableExtra(STATE_INTENT_KEY);
        if (mode == Mode.EDIT || mode == Mode.VIEW) {
            intentJournalEntry = Parcels.unwrap(intent.getParcelableExtra(JOURNAL_ENTRY_INTENT_KEY));
        }
    }


    private void initViews() {
        initToolbar();
        initDateViews();
        initCamera();

        if (mode == Mode.EDIT || mode == Mode.VIEW) {
            populateViewsWithIntentData();
        }
    }

    private void initToolbar() {
        setSupportActionBar(binding.createToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initDateViews() {
        Date date = Calendar.getInstance().getTime();
        binding.dayNumberTv.setText(DateUtils.dayOfMonth(date));
        binding.monthAndYearTv.setText(DateUtils.monthAndYear(date));
    }

    private void initCamera() {
        binding.cameraFab.setOnClickListener(v -> dispatchCameraIntent());
    }

    private void populateViewsWithIntentData() {
        Map<Mood, Integer> moodToDrawable = Map.of(
                Mood.NEGATIVE, R.drawable.icons8_sad_48,
                Mood.NEUTRAL, R.drawable.icons8_neutral_48,
                Mood.POSITIVE, R.drawable.icons8_happy_48
        );

        binding.createMoodIcon.setVisibility(View.VISIBLE);
        binding.createMoodIcon.setImageResource(moodToDrawable.get(intentJournalEntry.getMood()));
        binding.dayNumberTv.setText(DateUtils.dayOfMonth(intentJournalEntry.getCreatedAt()));
        binding.monthAndYearTv.setText(DateUtils.monthAndYear(intentJournalEntry.getCreatedAt()));
        binding.titleEdittext.setText(intentJournalEntry.getTitle());
        binding.mainTextEdittext.setText(intentJournalEntry.getText());
        binding.publicSwitch.setChecked(intentJournalEntry.isPublic());

        if (intentJournalEntry.getContainsImage()) {
            binding.journalImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(intentJournalEntry.getImageUri()).into(binding.journalImage);
        }

        if (mode == Mode.VIEW) {
            //Prevent any type of input, only display data
            binding.mainTextEdittext.setFocusable(false);
            binding.titleEdittext.setFocusable(false);
            binding.publicSwitch.setEnabled(false);
            binding.cameraFab.setVisibility(View.GONE);
        }
    }

    private void dispatchCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri("photo.jpg");

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(this, "com.journaly.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE_CODE);
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename

        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            Bitmap rotated = BitmapUtils.rotateBitmapOrientation(photoFile.getAbsolutePath());
            binding.journalImage.setImageBitmap(rotated);
            binding.journalImage.setVisibility(View.VISIBLE);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveEntry() {
        //saving functionality shouldn't be enabled if we're just viewing
        assert mode == Mode.CREATE || mode == Mode.EDIT;

        if (!fieldsAreValid()) {
            Toasty.error(this, "Missing fields", Toast.LENGTH_SHORT, true).show();
            return;
        }

        binding.createProgressBar.setVisibility(View.VISIBLE);

        String title = binding.titleEdittext.getText().toString();
        String text = binding.mainTextEdittext.getText().toString();
        boolean isPublic = binding.publicSwitch.isChecked();
        long unixTime = System.currentTimeMillis();
        String userId = AuthManager.getInstance().getLoggedInUserId();

        nlpRepository.performSentimentAnalysis(text)
                //multiply score * magnitude to get a single sentiment value
                .map(sentimentAnalysis -> sentimentAnalysis.getScore() * sentimentAnalysis.getMagnitude())
                //subscribe to the sentimentAnalysis Task
                .subscribe(sentiment -> uploadPhotoIfNeeded().subscribe( //Once we receive our sentiment, upload photo (if needed)
                        (Uri uri) -> { //on success uploading image
                            Log.i(TAG, "Successfully uploaded image");
                            JournalEntry journalEntry = new JournalEntry(title, text, unixTime, isPublic, sentiment, userId, true, uri.toString());
                            pushToDatabase(journalEntry);
                            hideProgressBar();
                            finish();
                        },
                        (Throwable throwable) -> { //on error
                            Log.w(TAG, throwable);
                            Toasty.error(CreateActivity.this, "There was an error uploading the image", Toast.LENGTH_SHORT, true).show();
                            hideProgressBar();
                        },
                         () -> { //no image uploaded
                            JournalEntry journalEntry = new JournalEntry(title, text, unixTime, isPublic, sentiment, userId, false, null);
                            pushToDatabase(journalEntry);
                            hideProgressBar();
                            finish();
                        }
                ));
    }

    private Maybe<Uri> uploadPhotoIfNeeded() {
        return Maybe.create(emitter -> {
            if (photoFile == null) {
                emitter.onComplete();
                return;
            }

            CloudStorageManager cloudStorageManager = CloudStorageManager.getInstance();
            cloudStorageManager.upload(photoFile, this).subscribe(uri -> {
                emitter.onSuccess(uri);
            }, throwable -> {
                emitter.onError(throwable);
            });
        });
    }

    private void pushToDatabase(JournalEntry entry) {
        if (mode == Mode.EDIT) {
            entry.setId(intentJournalEntry.getId());
        }
        journalRepository.addOrUpdate(entry);
    }

    private void deleteEntry() {
        assert (mode == Mode.EDIT); //delete functionality should not even be enabled if we're not in edit mode
        showConfirmDeleteDialog((dialog, which) -> {
            journalRepository.delete(intentJournalEntry);
            finish();
        });

    }

    private boolean fieldsAreValid() {
        String title = binding.titleEdittext.getText().toString();
        String text = binding.mainTextEdittext.getText().toString();
        return title.length() > 0 && text.length() > 0;
    }

    private void showConfirmDeleteDialog(DialogInterface.OnClickListener onConfirm) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this journal entry?");
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        builder.setPositiveButton("Delete", onConfirm);
        builder.create().show();
    }

    private void hideProgressBar(){
        //hiding the progress bar is done at the end of async operations (uploading an entry to the database)
        //which run on a separate thread. Changes to the UI must run on the UI thread. Thus we force
        //the change to run on the UI thread.
        runOnUiThread(() -> binding.createProgressBar.setVisibility(View.INVISIBLE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem delete = menu.findItem(R.id.action_delete);
        MenuItem save = menu.findItem(R.id.action_save);

        delete.setVisible(mode == Mode.EDIT);
        save.setVisible(mode == Mode.CREATE || mode == Mode.EDIT);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            saveEntry();
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            deleteEntry();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}