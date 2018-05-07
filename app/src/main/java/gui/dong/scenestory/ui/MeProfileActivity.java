package gui.dong.scenestory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import gui.dong.scenestory.R;
import gui.dong.scenestory.utils.FileUtils;

/**
 * 账号信息页面
 */
public class MeProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText nicknameEt;
    private RadioGroup sexGroup;
    private ImageView avatar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_profile);
        avatar = findViewById(R.id.avatar_img);
        avatar.setOnClickListener(this);
        findViewById(R.id.reset_password).setOnClickListener(this);
        findViewById(R.id.logout).setOnClickListener(this);
        nicknameEt = findViewById(R.id.nickname_et);
        sexGroup = findViewById(R.id.sex_rg);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        AVUser avUser = AVUser.getCurrentUser();
        nicknameEt.setText(avUser.getString("nickname"));
        int gender = avUser.getInt("gender");
        if (gender == 1) {
            sexGroup.check(R.id.sex_rb_boy);
        } else if (gender == 0) {
            sexGroup.check(R.id.sex_rb_girl);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        /**
         * 退出登录
         */
        if (id == R.id.logout) {
            AVUser.logOut();
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("logout", true);
            startActivity(intent);
        } else if (id == R.id.reset_password) {
            //重置密码
        } else if (id == R.id.avatar_img) {
            //点击头像
            PictureSelector.create(this)
                    .openGallery(PictureMimeType.ofImage())
                    .maxSelectNum(1)
                    .enableCrop(true)
                    .showCropGrid(true)
                    .showCropFrame(true)
                    .cropWH(400, 400)
                    .forResult(1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            // 图片、视频、音频选择结果回调
            List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
            // 例如 LocalMedia 里面返回三种path
            // 1.media.getPath(); 为原图path
            // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true  注意：音视频除外
            // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true  注意：音视频除外
            // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
            if (selectList != null && !selectList.isEmpty()) {
                String path = selectList.get(0).getCutPath();
                Glide.with(this)
                        .load(path)
                        .apply(new RequestOptions().error(R.drawable.logo).circleCrop())
                        .into(avatar);
                try {
                    final AVFile avFile = AVFile.withFile(FileUtils.PathToFileName(path),new File(path));
                    avFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if(e==null){
                                AVUser avUser = AVUser.getCurrentUser();
                                avUser.put("avatar",avFile);
                                avUser.saveInBackground();
                            }else{
                                Toast.makeText(MeProfileActivity.this,"上传头像失败",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(MeProfileActivity.this,"上传头像失败",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateProfile();
    }

    private void updateProfile() {
        AVUser avUser = AVUser.getCurrentUser();
        boolean hasModify = false;
        if (avUser.getString("nickname")==null || !avUser.getString("nickname").equals(nicknameEt.getText().toString())) {
            hasModify = true;
            avUser.put("nickname", nicknameEt.getText().toString());
        }
        int sex = sexGroup.getCheckedRadioButtonId() == R.id.sex_rb_boy ? 1 : 0;
        if (sex != avUser.getInt("gender")) {
            avUser.put("gender", sex);
            hasModify = true;
        }
        if (hasModify) {
            avUser.saveInBackground();
        }
    }
}
