package gui.dong.scenestory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetDataCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gui.dong.scenestory.R;
import gui.dong.scenestory.RecorderService;
import gui.dong.scenestory.adapter.FavoriteStoryAdapter;
import gui.dong.scenestory.bean.Story;
import gui.dong.scenestory.utils.FileUtil;
import io.realm.Realm;


public class MyStoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavoriteStoryAdapter storyAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.normal_toolbar_container);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("我收藏的视频");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        recyclerView = findViewById(R.id.container_rv);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        initData();
    }

    private void initData() {
        AVQuery<AVObject> query = new AVQuery<>("Story");
        query.whereEqualTo("favorite", AVUser.getCurrentUser());
        query.include("creator");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    if (list == null || list.isEmpty()) {
                        Toast.makeText(MyStoryActivity.this, "你还没有收藏的视频", Toast.LENGTH_SHORT).show();
                    } else {
                        List<Story> stories = new ArrayList<>();
                        for (AVObject avObject : list) {
                            Story story = new Story();
                            story.setObjId(avObject.getObjectId());
                            story.setName(avObject.getString("name"));
                            story.setCreatedAt(avObject.getCreatedAt().getTime());
                            story.setCreatorId(avObject.getAVUser("creator").getUsername());
                            story.setId(avObject.getString("id"));
                            story.setLocalPath(avObject.getAVFile("video").getUrl());
                            stories.add(story);
                            if(Realm.getDefaultInstance().where(Story.class).equalTo("id",story.getId()).count()<=0){
                                downloadStory(story,avObject.getAVFile("video"));
                            }
                        }
                        storyAdapter = new FavoriteStoryAdapter(stories);
                        recyclerView.setAdapter(storyAdapter);
                        storyAdapter.setOnStoryCilck(new FavoriteStoryAdapter.OnStoryClick() {
                            @Override
                            public void onStoryClicked(Story story, int action) {
                                Intent intent = new Intent(MyStoryActivity.this, StoryPlayActivity.class);
                                intent.putExtra("storyId", story.getId());
                                startActivity(intent);
                            }
                        });
                    }
                } else {
                    Toast.makeText(MyStoryActivity.this, "获取收藏视频失败", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    private void downloadStory(final Story story, AVFile avFile){
        avFile.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, AVException e) {
                if(e == null){
                    String path = RecorderService.getSaveDirectory()+System.currentTimeMillis()+".MP4";
                    try {
                        story.setLocalPath(path);
                        FileUtil.createFile(bytes,path);
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        realm.copyToRealm(story);
                        realm.commitTransaction();
                        realm.close();
                        storyAdapter.update(story);
                    }catch (IOException e2){
                        e2.printStackTrace();
                    }
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
