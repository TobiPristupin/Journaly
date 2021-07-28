package com.example.journaly.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.example.journaly.R;
import com.example.journaly.databinding.ProfileListItemBinding;
import com.example.journaly.glide.SvgSoftwareLayerSetter;
import com.example.journaly.model.avatar.AvatarApiClient;
import com.example.journaly.model.users.User;
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

public class ProfileItemAdapter extends RecyclerView.Adapter<ProfileItemAdapter.ViewHolder> {

    public interface OnEntryClickListener {
        void onEntryClick(int position);
    }

    public static final String TAG = "ProfileItemAdapter";
    private List<User> users;
    private Activity context;
    private OnEntryClickListener clickListener;

    public ProfileItemAdapter(List<User> users, OnEntryClickListener clickListener, Activity context) {
        this.users = users;
        this.context = context;
        this.clickListener = clickListener;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_list_item, parent, false);
        return new ProfileItemAdapter.ViewHolder(view);
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

        private ProfileListItemBinding binding;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = ProfileListItemBinding.bind(itemView);
        }

        private void bind(User user) {
            if (user.getPhotoUri() != null){
                Glide.with(context).load(user.getPhotoUri()).fallback(R.drawable.default_profile).into(binding.profilePfp);
            } else {
                GlideToVectorYou.justLoadImage(context, AvatarApiClient.generateAvatarUri(user.getDisplayName()), binding.profilePfp);
            }


            binding.profileName.setText(user.getDisplayName());
            binding.profileBio.setText(user.getBio());
            binding.getRoot().setOnClickListener(v -> clickListener.onEntryClick(getAdapterPosition()));
        }
    }
}

