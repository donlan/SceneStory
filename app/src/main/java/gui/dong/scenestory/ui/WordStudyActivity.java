package gui.dong.scenestory.ui;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;
import com.bumptech.glide.Glide;

import java.io.IOException;

import gui.dong.scenestory.R;
import gui.dong.scenestory.bean.Word;
import io.realm.Realm;

/**
 * 单词学习页面
 */
public class WordStudyActivity extends AppCompatActivity implements LearnCommitFragment.OnCommitClickListener {

    private Realm realm;
    private Word word;
    private ImageView icon;
    private TextView nameTv;
    private TextView pinyinTv;
    private TextView enNameTv;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_study);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        icon = findViewById(R.id.word_icon);
        nameTv = findViewById(R.id.word_cn_name);
        enNameTv = findViewById(R.id.word_en);
        pinyinTv = findViewById(R.id.word_cn_pingyin);
        //点击喇叭图标，播放英文发音
        findViewById(R.id.word_en_sound).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (word != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
        });

        //弹出时候学会的选择页面
        findViewById(R.id.learned_ok_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LearnCommitFragment.show(getSupportFragmentManager(),WordStudyActivity.this);
            }
        });

        String id = getIntent().getStringExtra("id");
        realm = Realm.getDefaultInstance();
        word = realm.where(Word.class).equalTo("id", id).findFirst();
        if (word != null) {
            mediaPlayer = new MediaPlayer();
            try {
                //发音是一个MP3文件的链接
                mediaPlayer.setDataSource(word.getEnSoundUrl());
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //加载单词图片
            Glide.with(this)
                    .load(word.getIconUrl())
                    .into(icon);
            nameTv.setText(word.getName());
            pinyinTv.setText(word.getPinyin());
            enNameTv.setText(word.getEnName());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    /**
     * 弹出是否学会页面的回调
     * @param yes true说明是点击了学会
     */
    @Override
    public void onCommitClick(boolean yes) {
        if(yes){
            realm.beginTransaction();
            word.setLearned(true);
            realm.commitTransaction();
            AVUser avUser = AVUser.getCurrentUser();
            avUser.put("creative",avUser.getInt("creative")+1);
            avUser.saveInBackground();
            finish();
        }

    }
}
