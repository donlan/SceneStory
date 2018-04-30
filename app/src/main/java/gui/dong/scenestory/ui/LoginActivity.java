package gui.dong.scenestory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;

import dong.lan.labelTextview.LabelTextView;
import gui.dong.scenestory.R;

/**
 * 登录界面
 */
public class LoginActivity extends AppCompatActivity {


    private TextInputLayout accountTill;
    private TextInputLayout passTil;
    private LabelTextView loginTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //初始化后台数据库服务
        AVOSCloud.initialize(getApplicationContext(),"Jifv9w6CrxO81MTWtmULGcio-gzGzoHsz","kF9jN8faS2tHcoa3CHuual6x");
        //当前用户不为空说明已经登录了，直接进入主页面
        if (AVUser.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            accountTill = findViewById(R.id.account_til);
            passTil = findViewById(R.id.password_til);
            loginTv = findViewById(R.id.login_ltv);
            accountTill.setHint("用户名");
            passTil.setHint("密码");
            findViewById(R.id.tv_2).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(LoginActivity.this, RegisterActivity.class), 1);
                }
            });
            loginTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login(accountTill.getEditText().getText().toString(), passTil.getEditText().getText().toString());
                }
            });
        }
    }

    /**
     * 根据输入的用户名，密码进行登录
     * @param account 用户名
     * @param password 密码
     */
    private void login(String account, String password) {
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "账号密码不能为空", Toast.LENGTH_SHORT).show();
        } else {
            loginTv.startLoading();
            AVUser.logInInBackground(account, password, new LogInCallback<AVUser>() {
                @Override
                public void done(AVUser avUser, AVException e) {
                    loginTv.finishLoading("");
                    //登录成功直接进入主页面
                    if (e == null) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //如果是从注册页面放回的用户名密码，说明注册成功，直接已返回的用户名密码直接登录
        if (requestCode == 1 && data != null) {
            login(data.getStringExtra("account"), data.getStringExtra("password"));
        }
    }
}
