package com.example.journaly.common;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.journaly.R;
import com.example.journaly.databinding.JournalEntryItemBinding;
import com.example.journaly.model.JournalEntry;
import com.example.journaly.model.UsersRepository;
import com.example.journaly.utils.DateUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.ViewHolder> {

    public interface OnEntryClickListener {
        void onUsernameClick(int position); //triggered when profile picture or username clicked

        void onEntryClick(int position); //triggered when anywhere else is clicked
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
        private final Map<Integer, Integer> moodToDrawable = Map.of(
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

            if (entry.getContainsImage()) {
                binding.postImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(entry.getImageUri()).into(binding.postImage);
            } else {
                binding.postImage.setVisibility(View.GONE);
            }

            View.OnClickListener onUsernameClick = v -> clickListener.onUsernameClick(getAdapterPosition());
            binding.entryPfp.setOnClickListener(onUsernameClick);
            binding.entryUsername.setOnClickListener(onUsernameClick);

            binding.getRoot().setOnClickListener(v -> {
                clickListener.onEntryClick(getAdapterPosition());
            });
        }

        private void initViewsDependentOnUser(JournalEntry entry) {
            usersRepository.fetchUserFromId(entry.getUserId()).take(1).subscribe(
                    user -> {
                        Glide.with(context).load(user.getPhotoUri()).fallback(R.drawable.default_profile).into(binding.entryPfp);
                        binding.entryUsername.setText(user.getDisplayName());
                    },
                    throwable -> {
                        Log.w(TAG, throwable);
                    });
        }


    }
}

