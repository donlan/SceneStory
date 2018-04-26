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
 * 场景故事比方页面
 */
public class StoryPlayActivity extends AppCompatActivity {

    /**
     * 通过VideoView播放MP4文件，场景故事已MP4文件的形式保存在本地
     */
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
            //通过id从数据库找到对应的场景故事
            Story story = Realm.getDefaultInstance()
                    .where(Story.class)
                    .equalTo("id",getIntent().getStringExtra("storyId"))
                    .findFirst();
            //story中保存着故事的MP4文件在手机的存储地址
            if(story!=null){
                //videoview通过文件地址就可以播放该MP4文件
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

            //播放完毕，自动重新开始
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
