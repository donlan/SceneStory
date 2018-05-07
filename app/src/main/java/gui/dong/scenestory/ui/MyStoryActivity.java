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

import gui.dong.scenestory.R;
import gui.dong.scenestory.adapter.StoryAdapter;
import gui.dong.scenestory.bean.Story;
import io.realm.Realm;
import io.realm.RealmResults;


public class MyStoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Realm realm;
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
        recyclerView.setLayoutManager(new GridLayoutManager(this,1));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        initData();
    }

    private void initData() {
        realm = Realm.getDefaultInstance();
        RealmResults<Story> stories = realm.where(Story.class)
                .findAll();
        StoryAdapter storyAdapter = new StoryAdapter(stories);
        recyclerView.setAdapter(storyAdapter);

        if(stories.isEmpty()){
            Toast.makeText(this,"你还没有收藏的视频",Toast.LENGTH_SHORT).show();
        }
        storyAdapter.setOnStoryCilck(new StoryAdapter.OnStoryClick() {
            @Override
            public void onStoryClicked(Story story, int action) {
                Intent intent = new Intent(MyStoryActivity.this, StoryPlayActivity.class);
                intent.putExtra("storyId", story.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
