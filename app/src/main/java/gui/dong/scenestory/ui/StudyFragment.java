package gui.dong.scenestory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import gui.dong.scenestory.R;
import gui.dong.scenestory.adapter.StudyCourseAdapter;
import gui.dong.scenestory.adapter.WordItemAdapter;
import gui.dong.scenestory.bean.Course;
import gui.dong.scenestory.bean.Word;
import io.realm.Realm;
import io.realm.RealmResults;


public class StudyFragment extends Fragment {

    private RecyclerView studyCourseRv;
    private WordItemAdapter wordItemAdapter;
    private ImageView backIv;
    private boolean isShowCourse = true;
    Realm realm;
    private StudyCourseAdapter courseAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study,container,false);
        studyCourseRv =view.findViewById(R.id.study_course_rv);
        backIv = view.findViewById(R.id.back);
        studyCourseRv.setLayoutManager(new GridLayoutManager(getContext(),1));
        studyCourseRv.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        realm = Realm.getDefaultInstance();
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isShowCourse){
                    isShowCourse = true;
                    studyCourseRv.setAdapter(courseAdapter);
                    backIv.setVisibility(View.GONE);
                }
            }
        });
        return view;
    }

    private boolean isInit = false;
    @Override
    public void onStart() {
        super.onStart();
        if(isInit){
            return;
        }
        isInit = true;
        initData();
    }

    private void initData(){

        RealmResults<Course> courses = realm.where(Course.class)
                .findAll();
        if(courses.isEmpty()){
            AVQuery<AVObject> query = new AVQuery<>("Course");
            query.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> list, AVException e) {
                    if(e == null) {
                        List<Course> courseList = new ArrayList<>();
                        for (AVObject object : list) {
                            courseList.add(new Course(object));
                        }
                        realm.beginTransaction();
                        realm.copyToRealm(courseList);
                        realm.commitTransaction();
                        courseAdapter = new StudyCourseAdapter(courseList);
                        studyCourseRv.setAdapter(courseAdapter);
                        setupAdapter();
                    }
                }
            });
        }else{
            courseAdapter = new StudyCourseAdapter(courses);
            studyCourseRv.setAdapter(courseAdapter);
            setupAdapter();
        }
    }

    private void setupAdapter(){
        courseAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if(wordItemAdapter == null){
                    wordItemAdapter = new WordItemAdapter(new ArrayList<Word>());
                    wordItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                            Word word = wordItemAdapter.getItem(position);
                            Intent intent = new Intent(getContext(),WordStudyActivity.class);
                            intent.putExtra("id",word.getId());
                            startActivity(intent);
                        }
                    });
                }
                Course course = (Course) adapter.getItem(position);
                RealmResults<Word> words = realm.where(Word.class)
                        .equalTo("courseId",course.getId())
                        .findAll();
                if(words.isEmpty()){
                    AVQuery<AVObject> query = new AVQuery<>("Word");
                    query.whereEqualTo("course",AVObject.createWithoutData("Course",course.getObjId()));
                    query.findInBackground(new FindCallback<AVObject>() {
                        @Override
                        public void done(List<AVObject> list, AVException e) {
                            List<Word> wordList = new ArrayList<>();
                            if (e == null) {
                                for (AVObject object : list) {
                                    wordList.add(new Word(object));
                                }
                            }
                            wordItemAdapter.setNewData(wordList);
                            if (!realm.isClosed()) {
                                realm.beginTransaction();
                                realm.copyToRealm(wordList);
                                realm.commitTransaction();
                            }
                        }
                    });
                }else{
                    wordItemAdapter.setNewData(words);
                }
                studyCourseRv.setAdapter(wordItemAdapter);
                backIv.setVisibility(View.VISIBLE);
                isShowCourse = false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }
}
