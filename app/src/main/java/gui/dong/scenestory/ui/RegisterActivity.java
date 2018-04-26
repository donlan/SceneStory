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
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;

import dong.lan.labelTextview.LabelTextView;
import gui.dong.scenestory.R;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout accountTill;
    private TextInputLayout passTil;
    private LabelTextView registerTv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regster);
        accountTill = findViewById(R.id.account_til);
        passTil = findViewById(R.id.password_til);
        registerTv = findViewById(R.id.register_ltv);
        accountTill.setHint("用户名");
        passTil.setHint("密码");
        //点击注册按钮，进行注册逻辑
        registerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(accountTill.getEditText().getText().toString(),passTil.getEditText().getText().toString());
            }
        });
    }

    /**
     * 根据用户输入的用户名，密码进行注册
     * @param account 用户名
     * @param password 密码
     */
    private void register(final String account, final String password) {
        //对用户名 密码进行判空操作，防止非法调用
        if(TextUtils.isEmpty(account) || TextUtils.isEmpty(password)){
            Toast.makeText(this,"账号密码不能为空",Toast.LENGTH_SHORT).show();
        }else{
            registerTv.startLoading();
            //创建一份用户信息
            AVUser avUser = new AVUser();
            avUser.setPassword(password);
            avUser.setUsername(account);
            //进行注册
            avUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    registerTv.finishLoading("");
                    if(e == null){
                        //注册成功，返回用户密码到登录页面，登陆页面就可以进行自动登录逻辑
                        Intent intent = new Intent();
                        intent.putExtra("account",account);
                        intent.putExtra("password",password);
                        setResult(1,intent);
                        finish();
                    }else{
                        //注册失败：可能原因很多，网络问题，用户名重复等
                        Toast.makeText(RegisterActivity.this,"注册失败",Toast.LENGTH_SHORT).show();
                    }
                }

            });
        }
    }
}
