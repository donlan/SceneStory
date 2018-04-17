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

/**
 * @author 梁桂栋
 * @version 1.0
 * @date 2018/4/9  16:42.
 * e-mail 760625325@qq.com
 * GitHub: https://github.com/donlan
 * description: gui.dong.scenestory.ui
 */
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
        registerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(accountTill.getEditText().getText().toString(),passTil.getEditText().getText().toString());
            }
        });
    }

    private void register(final String account, final String password) {
        if(TextUtils.isEmpty(account) || TextUtils.isEmpty(password)){
            Toast.makeText(this,"账号密码不能为空",Toast.LENGTH_SHORT).show();
        }else{
            registerTv.startLoading();
            AVUser avUser = new AVUser();
            avUser.setPassword(password);
            avUser.setUsername(account);
            avUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    registerTv.finishLoading("");
                    if(e == null){
                        Intent intent = new Intent();
                        intent.putExtra("account",account);
                        intent.putExtra("password",password);
                        setResult(1,intent);
                        finish();
                    }else{
                        Toast.makeText(RegisterActivity.this,"注册失败",Toast.LENGTH_SHORT).show();
                    }
                }

            });
        }
    }
}
