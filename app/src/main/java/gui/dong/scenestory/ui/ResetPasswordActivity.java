package gui.dong.scenestory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.RequestPasswordResetCallback;
import com.avos.avoscloud.SaveCallback;

import dong.lan.labelTextview.LabelTextView;
import gui.dong.scenestory.R;

/**
 * 重置密码页面
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailEt;
    private LabelTextView commitLtv;
    private LabelTextView reLoginLtv;
    private AVUser avUser;
    private int resetFlag = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        avUser = AVUser.getCurrentUser();
        emailEt = findViewById(R.id.email_et);
        reLoginLtv = findViewById(R.id.relogin_ltv);
        commitLtv = findViewById(R.id.reset_ltv);
        commitLtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //还没有绑定邮箱
                if(resetFlag == 0){
                    avUser.setEmail(emailEt.getText().toString());
                    avUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e == null) {
                                resetFlag = 1;
                                commitLtv.setText("已确认绑定链接，点我发送重置密码请求");
                                Toast.makeText(ResetPasswordActivity.this, "发送绑定邮箱链接成功，请登录该邮箱确认绑定链接", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ResetPasswordActivity.this, "绑定邮箱失败", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }else if(resetFlag ==1 || resetFlag==2){
                    //已经绑定邮箱，直接向邮箱发送重置密码的链接
                    AVUser.requestPasswordResetInBackground(emailEt.getText().toString(), new RequestPasswordResetCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e == null) {
                                resetFlag=2;
                                commitLtv.setText("未收到重置密码链接？重新发送到该邮箱");
                                reLoginLtv.setVisibility(View.VISIBLE);
                                Toast.makeText(ResetPasswordActivity.this, "发送重置密码链接成功，请登录该邮箱确认修改密码链接", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ResetPasswordActivity.this, "发送重置密码链接失败", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
        //如果重置了密码，点击去登录
        reLoginLtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AVUser.logOut();
                Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
                intent.putExtra("logout",true);
                startActivity(intent);
                finish();
            }
        });


        if (TextUtils.isEmpty(avUser.getEmail())) {
            commitLtv.setText("验证邮箱");
            reLoginLtv.setVisibility(View.GONE);
            resetFlag = 0;
        } else {
            emailEt.setText(avUser.getEmail());
            commitLtv.setText("发送重置密码链接");
            resetFlag = 1;
        }
    }
}
