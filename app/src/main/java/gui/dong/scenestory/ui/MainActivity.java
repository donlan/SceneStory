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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import gui.dong.scenestory.R;
import gui.dong.scenestory.adapter.StoryAdapter;
import gui.dong.scenestory.bean.Story;
import gui.dong.scenestory.task.FetchStoryTask;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements StoryAdapter.OnStoryClick {

    private RecyclerView storyRv;
    private RealmResults<Story> stories;
    private EditText searchEt;

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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(new Intent(MainActivity.this,CreateStoryActivity.class));
            }
        });
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
        Realm.init(getApplicationContext());
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(1)
                .name("story.realm")
                .build();
        Realm.setDefaultConfiguration(configuration);
        stories = Realm.getDefaultInstance().where(Story.class)
                .findAllAsync();
        stories.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Story>>() {
            @Override
            public void onChange(RealmResults<Story> stories, OrderedCollectionChangeSet changeSet) {
                if(stories.isEmpty()){
                    FetchStoryTask.start(MainActivity.this);
                }
                storyRv.getAdapter().notifyDataSetChanged();
            }
        });
        adapter = new StoryAdapter(stories);
        adapter.setOnStoryCilck(this);
        storyRv.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStoryClicked(final Story story, int action) {
        if (action == 0) {
            Intent intent = new Intent(this, StoryPlayActivity.class);
            intent.putExtra("storyId", story.getId());
            startActivity(intent);
        }else{
            new AlertDialog.Builder(this)
                    .setMessage("确定要删除这个故事视频吗？")
                    .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.where(Story.class)
                                            .equalTo("id",story.getId())
                                            .findAll().deleteAllFromRealm();
                                }
                            });
                        }
                    })
                    .setNegativeButton("取消",null)
                    .show();
        }
    }
}
