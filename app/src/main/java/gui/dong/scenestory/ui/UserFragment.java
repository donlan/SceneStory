package gui.dong.scenestory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import gui.dong.scenestory.R;
import gui.dong.scenestory.bean.Story;
import gui.dong.scenestory.bean.Word;
import io.realm.Realm;

/**
 * 个人中心页面
 */
public class UserFragment extends Fragment implements View.OnClickListener {

    private ImageView avatar;
    private TextView nicknameTv;
    private TextView storyCountTv;
    private TextView wordCountTv;
    private TextView rangTv;
    private TextView creativeTv;
    private Realm realm;
    private AVUser avUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        view.findViewById(R.id.avatar).setOnClickListener(this);
        view.findViewById(R.id.user_nickname).setOnClickListener(this);
        view.findViewById(R.id.study_word_img).setOnClickListener(this);
        view.findViewById(R.id.study_word_tv).setOnClickListener(this);
        view.findViewById(R.id.my_story_img).setOnClickListener(this);
        view.findViewById(R.id.my_story_tv).setOnClickListener(this);
        view.findViewById(R.id.logout).setOnClickListener(this);
        view.findViewById(R.id.reset_password).setOnClickListener(this);
        avatar = view.findViewById(R.id.avatar);
        nicknameTv = view.findViewById(R.id.user_nickname);
        storyCountTv = view.findViewById(R.id.user_story_tv);
        wordCountTv = view.findViewById(R.id.user_word_tv);
        rangTv = view.findViewById(R.id.user_rang_tv);
        creativeTv = view.findViewById(R.id.user_creative_tv);
        initData();
        return view;
    }

    private void initData() {
        avUser = AVUser.getCurrentUser();
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            /**
             * 退出登录
             */
            case R.id.logout:
                AVUser.logOut();
                getActivity().finish();
                break;
            case R.id.reset_password:
                //重置密码
                startActivity(new Intent(getContext(),ResetPasswordActivity.class));
                break;
            case R.id.avatar:
            case R.id.user_nickname:
                startActivity(new Intent(getContext(), MeProfileActivity.class));
                break;
            case R.id.study_word_img:
            case R.id.study_word_tv:
                startActivity(new Intent(getContext(), MyLearnedWordActivity.class));
                break;
            case R.id.my_story_img:
            case R.id.my_story_tv:
                startActivity(new Intent(getContext(), MyStoryActivity.class));
                break;
            default:
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        nicknameTv.setText(TextUtils.isEmpty(avUser.getString("nickname")) ? avUser.getUsername() : avUser.getString("nickname"));
        long storyCount = realm.where(Story.class).count();
        long wordCount = realm.where(Word.class).equalTo("isLearned", true).count();
        storyCountTv.setText(storyCount + "个视频");
        wordCountTv.setText(wordCount + "个词汇");
        creativeTv.setText("创造力 " + (storyCount + wordCount));
        rangTv.setText("我的排名 1");
        if (avUser.getAVFile("avatar") != null) {
            Glide.with(this)
                    .load(avUser.getAVFile("avatar").getUrl())
                    .apply(new RequestOptions().error(R.drawable.logo)
                            .circleCrop())
                    .into(avatar);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }
}
