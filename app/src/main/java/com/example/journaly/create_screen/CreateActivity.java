package com.example.journaly.create_screen;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.journaly.R;
import com.example.journaly.databinding.ActivityCreateBinding;
import com.example.journaly.login.LoginManager;
import com.example.journaly.model.CloudStorageManager;
import com.example.journaly.model.FirebaseJournalRepository;
import com.example.journaly.model.JournalEntry;
import com.example.journaly.model.JournalRepository;
import com.example.journaly.utils.BitmapUtils;
import com.example.journaly.utils.DateUtils;

import org.parceler.Parcels;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.MaybeEmitter;
import io.reactivex.rxjava3.core.MaybeObserver;
import io.reactivex.rxjava3.core.MaybeOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;

public class CreateActivity extends AppCompatActivity {

    private ActivityCreateBinding binding;
    private static final int REQUEST_IMAGE_CAPTURE_CODE = 1;
    private static final String TAG = "CreateActivity";
    private File photoFile = null;
    private JournalRepository journalRepository = FirebaseJournalRepository.getInstance();
    public static final String JOURNAL_ENTRY_INTENT_KEY = "entry";
    private boolean editMode = false; //false by default, may be changed to true in the future
    @Nullable
    private JournalEntry intentJournalEntry; //pased from an intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if (intent.hasExtra(JOURNAL_ENTRY_INTENT_KEY)){
            editMode = true;
            intentJournalEntry = Parcels.unwrap(intent.getParcelableExtra(JOURNAL_ENTRY_INTENT_KEY));
        }

        initViews();
    }

    private void initViews(){
        initToolbar();
        initDateViews();
        initCamera();

        if (editMode){
            populateViewsWithEditData();
        }
    }

    private void initToolbar() {
        setSupportActionBar(binding.createToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initDateViews(){
        Date date = Calendar.getInstance().getTime();
        binding.dayNumberTv.setText(DateUtils.dayOfMonth(date));
        binding.monthAndYearTv.setText(DateUtils.monthAndYear(date));
    }

    private void initCamera(){
        binding.cameraFab.setOnClickListener(v -> dispatchCameraIntent());
    }

    private void populateViewsWithEditData(){
        Map<Integer, Integer> moodToDrawable =  Map.of(
                JournalEntry.NEGATIVE_MOOD, R.drawable.icons8_sad_48,
                JournalEntry.NEUTRAL_MOOD, R.drawable.icons8_happy_48,
                JournalEntry.POSITIVE_MOOD, R.drawable.icons8_happy_48
        );

        binding.createMoodIcon.setImageResource(moodToDrawable.get(intentJournalEntry.getMood()));
        binding.dayNumberTv.setText(DateUtils.dayOfMonth(intentJournalEntry.getDate()));
        binding.monthAndYearTv.setText(DateUtils.monthAndYear(intentJournalEntry.getDate()));
        binding.titleEdittext.setText(intentJournalEntry.getTitle());
        binding.mainTextEdittext.setText(intentJournalEntry.getText());
        binding.publicSwitch.setChecked(intentJournalEntry.isPublic());

        if (intentJournalEntry.getContainsImage()){
            binding.journalImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(intentJournalEntry.getImageUri()).into(binding.journalImage);
        }
    }

    private void dispatchCameraIntent(){
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

    private void saveEntry(){
        if (!fieldsAreValid()){
            Toasty.error(this, "Missing fields", Toast.LENGTH_SHORT, true).show();
            return;
        }

        String title = binding.titleEdittext.getText().toString();
        String text = binding.mainTextEdittext.getText().toString();
        boolean isPublic = binding.publicSwitch.isChecked();
        int mood = JournalEntry.NEUTRAL_MOOD; //TODO: Sentiment Analysis API
        long unixTime = System.currentTimeMillis();
        String userId = LoginManager.getInstance().getCurrentUser().getUid();

        uploadPhotoIfNeeded().subscribe(
                (Uri uri) -> { //on success uploading image
                    Log.i(TAG, "Successfully uploaded image");
                    JournalEntry journalEntry = new JournalEntry(title, text, unixTime, isPublic, mood, userId, true, uri.toString());
                    pushToDatabase(journalEntry);
                    finish();
                },
                (Throwable throwable) -> { //on error uploading image
                    Log.w(TAG, throwable);
                    Toasty.error(CreateActivity.this, "There was an error uploading the image", Toast.LENGTH_SHORT, true).show();
                },
                () -> { //no image uploaded
                    JournalEntry journalEntry = new JournalEntry(title, text, unixTime, isPublic, mood, userId, false, null);
                    pushToDatabase(journalEntry);
                    finish();
                }
        );
    }

    private Maybe<Uri> uploadPhotoIfNeeded(){
        return Maybe.create(emitter -> {
            if (photoFile == null){
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

    private void pushToDatabase(JournalEntry entry){
        if (editMode){
            entry.setId(intentJournalEntry.getId());
        }
        journalRepository.addOrUpdate(entry);
    }

    private boolean fieldsAreValid() {
        String title = binding.titleEdittext.getText().toString();
        String text = binding.mainTextEdittext.getText().toString();
        return title.length() > 0 && text.length() > 0;
    }
//
//    private Bitmap compressImage(Bitmap original){
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        original.compress(Bitmap.CompressFormat.JPEG, 0, out);
//        return BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save){
            saveEntry();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}