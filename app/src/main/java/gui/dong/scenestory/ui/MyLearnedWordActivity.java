package gui.dong.scenestory.ui;

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
import gui.dong.scenestory.adapter.WordItemAdapter;
import gui.dong.scenestory.bean.Word;
import io.realm.Realm;
import io.realm.RealmResults;


public class MyLearnedWordActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Realm realm;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.normal_toolbar_container);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("我学习的词汇");
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
        RealmResults<Word> words = realm.where(Word.class)
                .equalTo("isLearned",true)
                .findAll();
        WordItemAdapter wordItemAdapter = new WordItemAdapter(words);
        recyclerView.setAdapter(wordItemAdapter);

        if(words.isEmpty()){
            Toast.makeText(this,"你还没有已学会的词汇",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
