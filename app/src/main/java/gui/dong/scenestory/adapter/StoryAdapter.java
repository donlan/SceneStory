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

import gui.dong.scenestory.R;
import gui.dong.scenestory.bean.Story;
import io.realm.RealmResults;

/**
 * @author 梁桂栋
 * @version 1.0
 * @date 2018/4/8  19:48.
 * e-mail 760625325@qq.com
 * GitHub: https://github.com/donlan
 * description: gui.dong.scenestory.adapter
 */
public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {

    private boolean isShowCache;
    private RealmResults<Story> cache;
    private RealmResults<Story> stories;
    private OnStoryClick onStoryCilck;

    public void setOnStoryCilck(OnStoryClick onStoryCilck) {
        this.onStoryCilck = onStoryCilck;
    }

    public StoryAdapter(RealmResults<Story> stories) {
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

    public void setCache(RealmResults<Story> stories){
        cache = stories;
        setShowCache(true);
    }

    public boolean isShowCache() {
        return isShowCache;
    }

    public void setShowCache(boolean showCache) {
        isShowCache = showCache;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(isShowCache){
            return cache == null ? 0 : cache.size();
        }
        return stories == null ? 0 : stories.size();
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
                itemView.findViewById(R.id.story_del_bin).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onStoryCilck.onStoryClicked(stories.get(getLayoutPosition()),1);

                    }
                });
            }
        }

        void setStoryImg(Bitmap bitmap) {
            storyImg.setImageBitmap(bitmap);
        }
    }

    public interface OnStoryClick {
        void onStoryClicked(Story story,int action);
    }
}
