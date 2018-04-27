package gui.dong.scenestory.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.avos.avoscloud.AVObject;

import gui.dong.scenestory.R;
import gui.dong.scenestory.adapter.StoryAdapter;
import gui.dong.scenestory.bean.Story;
import gui.dong.scenestory.task.FetchStoryTask;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * 主页面，展示已经创建的场景故事
 */
public class MainActivity extends AppCompatActivity implements StoryAdapter.OnStoryClick {

    private RecyclerView storyRv;
    private RealmResults<Story> stories;
    private EditText searchEt;

    /*搜索框输入变更的回调，就是输入内容改变了，就搜索一次本地故事，对匹配的的进行展示*/
    private TextWatcher textWatcher= new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.length()<=0){
                adapter.setShowCache(false);
            }else{
                adapter.setCache(stories.where().like("name","*"+s+"*").findAll());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private StoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        storyRv = findViewById(R.id.story_rv);
        searchEt = findViewById(R.id.story_search_et);
        searchEt.addTextChangedListener(textWatcher);
        storyRv.setLayoutManager(new GridLayoutManager(this,1));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //点击添加按钮，跳转到添加场景故事的界面
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(new Intent(MainActivity.this,CreateStoryActivity.class));
            }
        });

        /**
         * 6.0以上的Android手机对某些危险权限需要在运行app的时候进行申请，用户同意后才能使用
         */
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, 2);
        }
        initData();
    }

    private void initData() {
        //初始化本地数据库
        Realm.init(getApplicationContext());
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(1)
                .name("story.realm")
                .build();
        Realm.setDefaultConfiguration(configuration);
        //查找创建了的所有场景故事
        stories = Realm.getDefaultInstance().where(Story.class)
                .findAllAsync();
        //数据库中的场景故事发生变更后，会进行回调，此时刷新列表就可显示最新的场景故事了
        stories.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Story>>() {
            @Override
            public void onChange(RealmResults<Story> stories, OrderedCollectionChangeSet changeSet) {
                if(stories.isEmpty()){
                    FetchStoryTask.start(MainActivity.this);
                }
                storyRv.getAdapter().notifyDataSetChanged();
            }
        });
        //将场景故事通过适配器加载到列表中，列表通过适配器进行显示
        adapter = new StoryAdapter(stories);
        adapter.setOnStoryCilck(this);
        storyRv.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStoryClicked(final Story story, int action) {
        if (action == 0) {
            //点击了列表中的场景故事，跳转到播放页面
            Intent intent = new Intent(this, StoryPlayActivity.class);
            intent.putExtra("storyId", story.getId());
            startActivity(intent);
        }else{
            //点击了场景故事的右上角删除按钮，提示是否删除该场景故事
            new AlertDialog.Builder(this)
                    .setMessage("确定要删除这个故事视频吗？")
                    .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    Story mStroy = realm.where(Story.class)
                                            .equalTo("id", story.getId())
                                            .findFirst();
                                    if (mStroy != null) {
                                        mStroy.deleteFromRealm();
                                        AVObject avObject = AVObject.createWithoutData("Story",mStroy.getObjId());
                                        avObject.deleteEventually();
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton("取消",null)
                    .show();
        }
    }
}
