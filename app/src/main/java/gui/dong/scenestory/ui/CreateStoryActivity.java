package gui.dong.scenestory.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.avos.avoscloud.AVUser;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xiaopo.flying.sticker.BitmapStickerIcon;
import com.xiaopo.flying.sticker.Sticker;
import com.xiaopo.flying.sticker.StickerView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import gui.dong.scenestory.CircleImageButton;
import gui.dong.scenestory.R;
import gui.dong.scenestory.RecorderService;
import gui.dong.scenestory.adapter.CharacterAdapter;
import gui.dong.scenestory.adapter.MyPagerAdapter;
import gui.dong.scenestory.adapter.SceneAdapter;
import gui.dong.scenestory.bean.IStoryElement;
import gui.dong.scenestory.bean.Story;
import gui.dong.scenestory.task.UploadStoryTask;
import gui.dong.scenestory.utils.CommonInputListener;
import gui.dong.scenestory.utils.CommonOneInputFragment;
import gui.dong.scenestory.utils.DisplayUtils;
import gui.dong.scenestory.utils.ImageResource;
import io.realm.Realm;

public class CreateStoryActivity extends AppCompatActivity implements View.OnClickListener, BaseQuickAdapter.OnItemClickListener {

    private StickerView stickerView;
    private ImageView sceneImg;
    private ImageView deleteBin;
    private CircleImageButton recordIb;
    private ImageView toggleIb;


    private List<Sticker> stickers = new ArrayList<>();
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Story story;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_create_story);
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tab);
        stickerView = findViewById(R.id.stickerView);
        sceneImg = findViewById(R.id.scene_img);
        deleteBin = findViewById(R.id.delete_bin_iv);
        recordIb = findViewById(R.id.record_ib);
        toggleIb = findViewById(R.id.panel_toggle_ib);
        recordIb.setOnClickListener(this);
        toggleIb.setOnClickListener(this);



        stickerView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {
            @Override
            public void onStickerAdded(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerClicked(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerDeleted(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerDragFinished(@NonNull Sticker sticker) {
                int r = DisplayUtils.dip2px(CreateStoryActivity.this,25);
                if(sticker.contains(deleteBin.getX()+r ,deleteBin.getY()+r)){
                    stickerView.remove(sticker);
                    stickers.remove(sticker);
                }
            }

            @Override
            public void onStickerZoomFinished(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerFlipped(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerDoubleTapped(@NonNull Sticker sticker) {
                stickerView.remove(sticker);
                stickers.remove(sticker);
            }
        });

        Intent intent = new Intent(this, RecorderService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        initPager();
    }

    private void initPager() {
        List<View> pagerViews  = new ArrayList<>();
        LinearLayoutManager llm = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        RecyclerView one = new RecyclerView(this);
        one.setLayoutManager(llm);
        one.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        one.setAdapter(new CharacterAdapter(this, ImageResource.getLocalCharacter(this,"scene_",20,true)));
        one.setTag("场景");
        pagerViews.add(one);

        RecyclerView two = new RecyclerView(this);
        two.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        two.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        two.setAdapter(new SceneAdapter(this, ImageResource.getLocalCharacter(this,"tool_",32,false)));
        two.setTag("道具");
        pagerViews.add(two);

        RecyclerView three = new RecyclerView(this);
        three.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        three.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        three.setAdapter(new SceneAdapter(this, ImageResource.getLocalCharacter(this,"animal_",26,false)));
        three.setTag("动物");
        pagerViews.add(three);

        RecyclerView four = new RecyclerView(this);
        four.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        four.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        four.setAdapter(new SceneAdapter(this, ImageResource.getLocalCharacter(this,"boy_",17,false)));
        four.setTag("男孩");
        pagerViews.add(four);

        RecyclerView five = new RecyclerView(this);
        five.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        five.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        five.setAdapter(new SceneAdapter(this, ImageResource.getLocalCharacter(this,"girl_",15,false)));
        five.setTag("女孩");
        pagerViews.add(five);

        RecyclerView six = new RecyclerView(this);
        six.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        six.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        six.setAdapter(new SceneAdapter(this, ImageResource.getLocalCharacter(this,"man_",17,false)));
        six.setTag("男性人物");
        pagerViews.add(six);

        RecyclerView seven = new RecyclerView(this);
        seven.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        seven.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        seven.setAdapter(new SceneAdapter(this, ImageResource.getLocalCharacter(this,"woman_",14,false)));
        seven.setTag("女性人物");
        pagerViews.add(seven);
        viewPager.setAdapter(new MyPagerAdapter(pagerViews));
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_ib:
                if(recordService.isRunning()){
                    toggleIb.setVisibility(View.VISIBLE);
                    deleteBin.setVisibility(View.VISIBLE);
                    recordIb.setImageResource(R.drawable.ic_play_arrow);
                    if(recordService.stopRecord()){
                        CommonOneInputFragment.newInstance("故事名称", "给录制的故事起个名字", new CommonInputListener() {
                            @Override
                            public void onInputCommit(final String inputText) {
                                Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        story = new Story();
                                        story.setName(!TextUtils.isEmpty(inputText)?inputText:"我的故事"+realm.where(Story.class).count());
                                        story.setCreatorId(AVUser.getCurrentUser().getUsername());
                                        story.setId(UUID.randomUUID().toString());
                                        story.setCreatedAt(System.currentTimeMillis());
                                        story.setLocalPath(recordService.getSavePath());
                                        realm.copyToRealm(story);
                                    }
                                });
                                UploadStoryTask.start(CreateStoryActivity.this.getApplicationContext(),story.getId());
                                finish();
                            }
                        }).show(getSupportFragmentManager(),"");

                    }
                }else{
                    tabLayout.setVisibility(View.GONE);
                    viewPager.setVisibility(View.GONE);
                    toggleIb.setVisibility(View.GONE);
                    deleteBin.setVisibility(View.GONE);
                    Intent captureIntent= projectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, 3);
                }
                break;
            case R.id.panel_toggle_ib:
                if(tabLayout.getVisibility() ==View.VISIBLE){
                    tabLayout.setVisibility(View.GONE);
                    viewPager.setVisibility(View.GONE);
                }else{
                    tabLayout.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.VISIBLE);
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3 && resultCode == RESULT_OK) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            recordService.setMediaProject(mediaProjection);
            if(recordService.isRunning()){
                recordService.stopRecord();
            }
            recordService.startRecord();
            recordIb.setImageResource(R.drawable.ic_pause);
        }
    }

    private RecorderService recordService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecorderService.RecordBinder binder = (RecorderService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
            recordIb.setEnabled(true);
            recordIb.setImageResource(R.drawable.ic_play_arrow);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {}
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(recordService.isRunning()){
            recordService.stopRecord();
        }
        unbindService(connection);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        IStoryElement element = (IStoryElement) adapter.getItem(position);
        if(element.isScene()){
            Glide.with(sceneImg)
                    .load(element.resId())
                    .into(sceneImg);
        }else{
            BitmapStickerIcon stickerIcon = new BitmapStickerIcon(((ImageView) view).getDrawable(), BitmapStickerIcon.LEFT_BOTTOM);
            stickerView.addSticker(stickerIcon);
            stickers.add(stickerIcon);
        }
    }
}
