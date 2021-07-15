package com.example.journaly.common;

import android.content.Context;
import android.net.Uri;
import android.nfc.Tag;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.gifdecoder.StandardGifDecoder;
import com.example.journaly.R;
import com.example.journaly.databinding.JournalEntryItemBinding;
import com.example.journaly.model.JournalEntry;
import com.example.journaly.model.User;
import com.example.journaly.model.UsersRepository;
import com.example.journaly.utils.DateUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.ViewHolder> {

    public interface OnEntryClickListener {
        void onEntryClick(int position);
    }

    public static final String TAG = "JournalEntryAdapter";
    private List<JournalEntry> entries;
    private Context context;
    private OnEntryClickListener clickListener;
    private UsersRepository usersRepository;


    public JournalEntryAdapter(List<JournalEntry> entries, UsersRepository usersRepository, OnEntryClickListener clickListener, Context context) {
        this.entries = entries;
        this.context = context;
        this.clickListener = clickListener;
        this.usersRepository = usersRepository;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.journal_entry_item, parent, false);
        return new JournalEntryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.bind(entries.get(position));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private JournalEntryItemBinding binding;
        private final Map<Integer, Integer> moodToDrawable =  Map.of(
                JournalEntry.NEGATIVE_MOOD, R.drawable.icons8_sad_48,
                JournalEntry.NEUTRAL_MOOD, R.drawable.icons8_happy_48,
                JournalEntry.POSITIVE_MOOD, R.drawable.icons8_happy_48
        );

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = JournalEntryItemBinding.bind(itemView);
        }

        private void bind(JournalEntry entry) {
            initViewsDependentOnUser(entry);
            binding.moodIcon.setImageResource(moodToDrawable.get(entry.getMood()));
            binding.entryDayNumber.setText(DateUtils.dayOfMonth(entry.getDate()));
            binding.entryMonthYear.setText(DateUtils.monthAndYear(entry.getDate()));
            binding.entryTitle.setText(entry.getTitle());
            binding.entryText.setText(entry.getText());
            binding.publicIcon.setVisibility(entry.isPublic() ? View.VISIBLE : View.GONE);

            if (entry.getContainsImage()){
                binding.postImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(entry.getImageUri()).into(binding.postImage);
            } else {
                binding.postImage.setVisibility(View.INVISIBLE);
            }

            binding.getRoot().setOnClickListener(v -> {
                clickListener.onEntryClick(getAdapterPosition());
            });
        }

        private void initViewsDependentOnUser(JournalEntry entry) {
            usersRepository.userFromId(entry.getUserId()).subscribe(new SingleObserver<User>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                }

                @Override
                public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull User user) {
                    Glide.with(context).load(user.getPhotoUri()).fallback(R.drawable.default_profile).into(binding.entryPfp);
                    binding.entryUsername.setText(user.getDisplayName());
                }

                @Override
                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                    Log.w(TAG, e);
                }
            });
        }
    }
}

