package gui.dong.scenestory.ui;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.VideoView;

import gui.dong.scenestory.R;
import gui.dong.scenestory.bean.Story;
import io.realm.Realm;

/**
 * @author 梁桂栋
 * @version 1.0
 * @date 2018/4/8  20:09.
 * e-mail 760625325@qq.com
 * GitHub: https://github.com/donlan
 * description: gui.dong.scenestory.ui
 */
public class StoryPlayActivity extends AppCompatActivity {

    private VideoView videoView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_story_paly);

        videoView = findViewById(R.id.videoView);
        if(!getIntent().hasExtra("storyId")){
            finish();
        }else{
            Story story = Realm.getDefaultInstance()
                    .where(Story.class)
                    .equalTo("id",getIntent().getStringExtra("storyId"))
                    .findFirst();
            if(story!=null){
                videoView.setVideoPath(story.getLocalPath());
                videoView.start();
            }else{
                finish();
            }
            videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!videoView.isPlaying()){
                        videoView.seekTo(0);
                        videoView.start();
                    }
                }
            });

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    videoView.start();
                }
            });
        }
    }

    public void back(View view) {
        finish();
    }
}
