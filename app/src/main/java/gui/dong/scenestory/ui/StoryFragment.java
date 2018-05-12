package gui.dong.scenestory.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;

import gui.dong.scenestory.R;
import gui.dong.scenestory.adapter.StoryAdapter;
import gui.dong.scenestory.bean.Story;
import gui.dong.scenestory.task.FetchStoryTask;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;


public class StoryFragment extends Fragment implements StoryAdapter.OnStoryClick {

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
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story,container,false);
        storyRv = view.findViewById(R.id.story_rv);
        searchEt = view.findViewById(R.id.story_search_et);
        searchEt.addTextChangedListener(textWatcher);
        storyRv.setLayoutManager(new GridLayoutManager(getContext(),1));
        //点击添加按钮，跳转到添加场景故事的界面
        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(),CreateStoryActivity.class));
            }
        });
        initData();
        return view;
    }

    private void initData() {

        //查找创建了的所有场景故事
        stories = Realm.getDefaultInstance().where(Story.class)
                .equalTo("creatorId",AVUser.getCurrentUser().getUsername())
                .findAllAsync();
        //数据库中的场景故事发生变更后，会进行回调，此时刷新列表就可显示最新的场景故事了
        stories.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Story>>() {
            @Override
            public void onChange(RealmResults<Story> stories, OrderedCollectionChangeSet changeSet) {
                if(stories.isEmpty()){
                    FetchStoryTask.start(getContext());
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
    public void onStoryClicked(final Story story, int action) {
        if (action == 0) {
            //点击了列表中的场景故事，跳转到播放页面
            Intent intent = new Intent(getContext(), StoryPlayActivity.class);
            intent.putExtra("storyId", story.getId());
            startActivity(intent);
        }else if(action == 1){
            //点击了场景故事的右上角删除按钮，提示是否删除该场景故事
            new AlertDialog.Builder(getContext())
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
        }else if(action ==2){
            AVObject avObject = AVObject.createWithoutData("Story",story.getObjId());
            avObject.getRelation("favorite").add(AVUser.getCurrentUser());
            avObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if(e == null){
                        Toast.makeText(getContext(), "收藏成功", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getContext(), "收藏失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.setOnStoryCilck(null);
        stories.removeAllChangeListeners();
        adapter =null;
        searchEt=null;
        storyRv=null;
        stories=null;
    }
}
