package gui.dong.scenestory.adapter;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import gui.dong.scenestory.R;
import gui.dong.scenestory.bean.Story;

public class FavoriteStoryAdapter extends RecyclerView.Adapter<FavoriteStoryAdapter.ViewHolder> {

    private List<Story> stories;
    private OnStoryClick onStoryCilck;

    public void setOnStoryCilck(OnStoryClick onStoryCilck) {
        this.onStoryCilck = onStoryCilck;
    }

    public FavoriteStoryAdapter(List<Story> stories) {
        this.stories = stories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_story, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(stories.get(position).getLocalPath(), MediaStore.Images.Thumbnails.MINI_KIND);
        holder.setStoryImg(bitmap);
        holder.nameTv.setText(stories.get(position).getName());
    }



    @Override
    public int getItemCount() {
        return stories == null ? 0 : stories.size();
    }

    public void update(Story story) {
        for(int i = 0;i<getItemCount();i++){
            if(story.getId().equals(stories.get(i).getId())){
                stories.get(i).setLocalPath(story.getLocalPath());
                notifyItemChanged(i);
                break;
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView storyImg;
        private TextView nameTv;

        ViewHolder(View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.story_name_tv);
            storyImg = itemView.findViewById(R.id.story_img);
            if(onStoryCilck!=null){
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onStoryCilck.onStoryClicked(stories.get(getLayoutPosition()),0);
                    }
                });
                itemView.findViewById(R.id.story_del_bin).setVisibility(View.GONE);
                itemView.findViewById(R.id.story_collect).setVisibility(View.GONE);
            }
        }

        void setStoryImg(Bitmap bitmap) {
            storyImg.setImageBitmap(bitmap);
        }
    }

    public interface OnStoryClick {
        void onStoryClicked(Story story, int action);
    }
}
