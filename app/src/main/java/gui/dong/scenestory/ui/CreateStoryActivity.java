package gui.dong.scenestory.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
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
import java.util.concurrent.TimeUnit;

import gui.dong.scenestory.CircleImageButton;
import gui.dong.scenestory.R;
import gui.dong.scenestory.RecorderService;
import gui.dong.scenestory.adapter.CharacterAdapter;
import gui.dong.scenestory.adapter.MyPagerAdapter;
import gui.dong.scenestory.adapter.SceneAdapter;
import gui.dong.scenestory.adapter.SoundAdapter;
import gui.dong.scenestory.bean.IStoryElement;
import gui.dong.scenestory.bean.Sound;
import gui.dong.scenestory.bean.Story;
import gui.dong.scenestory.task.UploadStoryTask;
import gui.dong.scenestory.utils.CommonInputListener;
import gui.dong.scenestory.utils.CommonOneInputFragment;
import gui.dong.scenestory.utils.DisplayUtils;
import gui.dong.scenestory.utils.SceneResource;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.realm.Realm;

/**
 * 创建故事页面
 */
public class CreateStoryActivity extends AppCompatActivity implements View.OnClickListener, BaseQuickAdapter.OnItemClickListener {

    private StickerView stickerView;
    private ImageView sceneImg;
    private ImageView deleteBin;
    private CircleImageButton recordIb;
    private ImageView toggleIb;


    private MediaProjectionManager projectionManager;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Story story;
    private MediaPlayer mediaPlayer;
    private int lastSoundIndex = -1;

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
            public void onStickerDragFinished(@NonNull final Sticker sticker) {
                int r = DisplayUtils.dip2px(CreateStoryActivity.this, 25);
                //移动到右上角垃圾箱位置，则删除
                if (sticker.contains(deleteBin.getX() + r, deleteBin.getY() + r)) {
                    stickerView.remove(sticker);
                } else {
                    //拖动结束如果重叠则进行合并
                    Observable.just(sticker)
                            .delay(200, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .map(new Function<Sticker, Sticker>() {
                                @Override
                                public Sticker apply(Sticker sticker) throws Exception {
                                    int i = stickerView.getStickers().indexOf(sticker);
                                    Sticker backSticker = null;
                                    //从当前推动的素材往下层寻找。找到第一个重叠的
                                    for (int n = i - 1; n >= 0; n--) {
                                        float[] bound = new float[8];
                                        stickerView.getStickerPoints(stickerView.getStickers().get(n), bound);
                                        if (stickerView.isContains(stickerView.getStickers().get(n), sticker)) {
                                            backSticker = stickerView.getStickers().get(n);
                                            break;
                                        }
                                    }
                                    return backSticker;
                                }
                            }).subscribe(new Consumer<Sticker>() {
                        @Override
                        public void accept(Sticker backSticker) throws Exception {
                            stickerView.concat(backSticker, sticker);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                        }
                    });

                }
            }

            @Override
            public void onStickerTouchedDown(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerZoomFinished(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerFlipped(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerDoubleTapped(@NonNull Sticker sticker) {
                //双击素材进行水平镜像替换
                stickerView.flip(sticker, StickerView.FLIP_HORIZONTALLY);
            }
        });

        Intent intent = new Intent(this, RecorderService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(true);
        initPager();
    }

    /**
     * 初始化素材资源
     */
    private void initPager() {
        List<View> pagerViews = new ArrayList<>();
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView one = new RecyclerView(this);
        one.setBackgroundColor(Color.WHITE);
        one.setLayoutManager(llm);
        one.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        one.setAdapter(new CharacterAdapter(this, SceneResource.getLocalCharacter(this, "scene_", 20, true)));
        one.setTag("场景");
        pagerViews.add(one);

        RecyclerView two = new RecyclerView(this);
        two.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        two.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        two.setAdapter(new SceneAdapter(this, SceneResource.getLocalCharacter(this, "tool_", 32, false)));
        two.setTag("道具");
        pagerViews.add(two);

        RecyclerView three = new RecyclerView(this);
        three.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        three.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        three.setAdapter(new SceneAdapter(this, SceneResource.getLocalCharacter(this, "animal_", 26, false)));
        three.setTag("动物");
        pagerViews.add(three);

        RecyclerView four = new RecyclerView(this);
        four.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        four.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        four.setAdapter(new SceneAdapter(this, SceneResource.getLocalCharacter(this, "boy_", 17, false)));
        four.setTag("男孩");
        pagerViews.add(four);

        RecyclerView five = new RecyclerView(this);
        five.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        five.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        five.setAdapter(new SceneAdapter(this, SceneResource.getLocalCharacter(this, "girl_", 15, false)));
        five.setTag("女孩");
        pagerViews.add(five);

        RecyclerView six = new RecyclerView(this);
        six.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        six.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        six.setAdapter(new SceneAdapter(this, SceneResource.getLocalCharacter(this, "man_", 17, false)));
        six.setTag("男性人物");
        pagerViews.add(six);

        RecyclerView seven = new RecyclerView(this);
        seven.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        seven.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        seven.setAdapter(new SceneAdapter(this, SceneResource.getLocalCharacter(this, "woman_", 14, false)));
        seven.setTag("女性人物");
        pagerViews.add(seven);


        RecyclerView soundRv = new RecyclerView(this);
        soundRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        soundRv.setLayoutParams(new RecyclerView.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        SoundAdapter soundAdapter = new SoundAdapter(SceneResource.getBackgroudSound());
        soundAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Sound sound = (Sound) adapter.getData().get(position);
                if (position != lastSoundIndex && lastSoundIndex != -1) {
                    ((Sound) adapter.getData().get(lastSoundIndex)).togglePlaying();
                    adapter.notifyItemChanged(lastSoundIndex);
                }
                sound.togglePlaying();
                adapter.notifyItemChanged(position);

                //重复点击同一个，则只做暂停、播放切换
                if (position == lastSoundIndex) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    } else {
                        mediaPlayer.start();
                    }
                    return;
                }
                lastSoundIndex = position;

                //加载音频资源到播放器
                AssetFileDescriptor afd = getResources().openRawResourceFd(sound.getRawId());
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    afd.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        soundRv.setAdapter(soundAdapter);
        soundRv.setTag("背景音乐");
        pagerViews.add(soundRv);
        viewPager.setAdapter(new MyPagerAdapter(pagerViews));
        tabLayout.setupWithViewPager(viewPager);

        stickerView.setEnableBorder(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_ib:
                //如果正在录制，点击后停止录制，并保存录制的MP4文件到本地，并保存录制记录到数据库
                if (recordService.isRunning()) {
                    stickerView.setEnableBorder(true);
                    toggleIb.setVisibility(View.VISIBLE);
                    deleteBin.setVisibility(View.VISIBLE);
                    recordIb.setImageResource(R.drawable.ic_play_arrow);
                    if (recordService.stopRecord()) {
                        CommonOneInputFragment.newInstance("故事名称", "给录制的故事起个名字", new CommonInputListener() {
                            @Override
                            public void onInputCommit(final String inputText) {
                                Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        story = new Story();
                                        story.setName(!TextUtils.isEmpty(inputText) ? inputText : "我的故事" + realm.where(Story.class).count());
                                        story.setCreatorId(AVUser.getCurrentUser().getUsername());
                                        story.setId(UUID.randomUUID().toString());
                                        story.setCreatedAt(System.currentTimeMillis());
                                        story.setLocalPath(recordService.getSavePath());
                                        realm.copyToRealm(story);
                                    }
                                });
                                UploadStoryTask.start(CreateStoryActivity.this.getApplicationContext(), story.getId());
                                finish();
                            }
                        }).show(getSupportFragmentManager(), "");

                    }
                } else {
                    //未在录制则开始进行屏幕录制
                    tabLayout.setVisibility(View.GONE);
                    viewPager.setVisibility(View.GONE);
                    toggleIb.setVisibility(View.GONE);
                    deleteBin.setVisibility(View.GONE);
                    stickerView.setEnableBorder(false);
                    Intent captureIntent = projectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, 3);
                }
                break;

            case R.id.panel_toggle_ib:
                //素材选择栏可见或者不可见
                if (tabLayout.getVisibility() == View.VISIBLE) {
                    tabLayout.setVisibility(View.GONE);
                    viewPager.setVisibility(View.GONE);
                } else {
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
            MediaProjection mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            recordService.setMediaProject(mediaProjection);
            if (recordService.isRunning()) {
                recordService.stopRecord();
            }
            recordService.startRecord();
            recordIb.setImageResource(R.drawable.ic_pause);
        }
    }

    /**
     * 这是屏幕录制的后台服务，与当前页面进行绑定
     */
    private RecorderService recordService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            //录制服务启动并绑定成功，进行屏幕录制相关资源的初始化
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecorderService.RecordBinder binder = (RecorderService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
            recordIb.setEnabled(true);
            recordIb.setImageResource(R.drawable.ic_play_arrow);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recordService.isRunning()) {
            recordService.stopRecord();
        }
        if(mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        unbindService(connection);
    }

    /**
     * 素材点击的回调方法
     *
     * @param adapter
     * @param view
     * @param position
     */
    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        IStoryElement element = (IStoryElement) adapter.getItem(position);
        //如果点击的是背景素材，则设置背景图片
        if (element.isScene()) {
            Glide.with(sceneImg)
                    .load(element.resId())
                    .into(sceneImg);
        } else {
            //点击的是普通的素材，则生成并添加一个素材
            BitmapStickerIcon stickerIcon = new BitmapStickerIcon(((ImageView) view).getDrawable(), BitmapStickerIcon.LEFT_BOTTOM);
            stickerView.addSticker(stickerIcon);
        }
    }
}
