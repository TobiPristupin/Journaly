package com.example.journaly.common;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.journaly.R;
import com.example.journaly.databinding.UserInNeedItemBinding;
import com.example.journaly.model.avatar.AvatarApiClient;
import com.example.journaly.model.users.User;
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserInNeedItemAdapter extends RecyclerView.Adapter<UserInNeedItemAdapter.ViewHolder> {

    public interface OnEntryClickListener {
        void onReachOutButtonClick(int position);
        void onUsernameClick(int position); //triggered when profile picture or username clicked
    }

    public static final String TAG = "JournalEntryAdapter";
    private List<User> users;
    private Activity activity;
    private OnEntryClickListener clickListener;

    public UserInNeedItemAdapter(List<User> users, OnEntryClickListener clickListener, Activity activity) {
        this.users = users;
        this.activity = activity;
        this.clickListener = clickListener;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_in_need_item, parent, false);
        return new UserInNeedItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private UserInNeedItemBinding binding;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = UserInNeedItemBinding.bind(itemView);
        }

        private void bind(User user) {
            if (user.getPhotoUri() != null){
                Glide.with(activity).load(user.getPhotoUri()).fallback(R.drawable.default_profile).into(binding.userInNeedPfp);
            } else {
                GlideToVectorYou.justLoadImage(activity, AvatarApiClient.generateAvatarUri(user.getDisplayName()), binding.userInNeedPfp);
            }
            binding.userInNeedProfileName.setText(user.getDisplayName());

            String userBio = user.getBio();
            String bioText = userBio == null || userBio.length() == 0 ? "This user has no bio :(" : userBio;
            binding.userInNeedAbout.setText(bioText);

            String userContact = user.getContactInfo();
            String contactText = userContact == null || userContact.length() == 0 ? "This user has no contact info :(" : userContact;
            binding.userInNeedContactInfo.setText(contactText);

            binding.reachOutButton.setOnClickListener(v -> {
                clickListener.onReachOutButtonClick(getAdapterPosition());
            });

            View.OnClickListener onUserClick = v -> clickListener.onUsernameClick(getAdapterPosition());
            binding.userInNeedProfileName.setOnClickListener(onUserClick);
            binding.userInNeedPfp.setOnClickListener(onUserClick);
        }

    }
}