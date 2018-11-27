package gui.dong.scenestory.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import gui.dong.scenestory.R;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * 主页面，展示已经创建的场景故事
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {



    private int curTab = 1;
    //底部三个导航图片按钮
    private ImageView[] tab = new ImageView[3];
    /**
     * 一个fragment就是一个页面
     */
    private Fragment[] fragments = new Fragment[3];
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(getIntent().getBooleanExtra("logout",false)){
            finish();
            startActivity(new Intent(this,LoginActivity.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果是点击退出登录跳转到这个页面，则直接关闭页面退出
        if(getIntent().getBooleanExtra("logout",false)){
            finish();
            startActivity(new Intent(this,LoginActivity.class));
            return;
        }
        setContentView(R.layout.activity_main);

        /**
         * 6.0以上的Android手机对某些危险权限需要在运行app的时候进行申请，用户同意后才能使用
         */
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, 2);
        }
        //初始化本地数据库
        Realm.init(getApplicationContext());
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(1)
                .name("story.realm")
                .build();
        Realm.setDefaultConfiguration(configuration);

        tab[0] = findViewById(R.id.study_tab_img);
        tab[0].setOnClickListener(this);
        tab[1] = findViewById(R.id.story_tab_img);
        tab[1].setOnClickListener(this);
        tab[2] = findViewById(R.id.user_tab_img);
        tab[2].setOnClickListener(this);
        /**
         * 初始化三个页面：学习，故事，个人中心
         */
        fragments[0]= new StudyFragment();
        fragments[1] =new StoryFragment();
        fragments[2] = new UserFragment();
        //通过FragmentManager管理三个页面的显示或隐藏
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container,fragments[0])
                .add(R.id.container,fragments[1])
                .add(R.id.container,fragments[2])
                .hide(fragments[0])
                .hide(fragments[2])
                .show(fragments[1])
                .commit();
        tab[1].setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        int thisTab = -1;
        if(id == R.id.story_tab_img){
            thisTab = 1;
        }else if(id == R.id.study_tab_img){
            thisTab = 0;
        }else if(id == R.id.user_tab_img){
            thisTab = 2;
        }
        if(thisTab ==-1 && thisTab==curTab){
            return;
        }
        //点击底部的切换图标的时候，判断需要显示哪一个页面
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(fragments[curTab])
                .show(fragments[thisTab]);
        transaction.commit();
        //去除上一次点击的图标的颜色（透明色）
        tab[curTab].setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        //当前选中的图标，设置灰色背景
        tab[thisTab].setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
        curTab = thisTab;
    }
}
