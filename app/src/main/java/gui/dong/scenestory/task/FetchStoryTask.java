package gui.dong.scenestory.task;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetDataCallback;

import java.io.IOException;
import java.util.List;

import gui.dong.scenestory.bean.Story;
import gui.dong.scenestory.utils.FileUtil;
import io.realm.Realm;

public class FetchStoryTask extends IntentService {

    public static void start(Context context){
        context.startService(new Intent(context,FetchStoryTask.class));
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchStoryTask(String name) {
        super(name);
    }

    public FetchStoryTask() {
        super("UploadStoryTask");
    }

    public static void start(Context context,String storyId){
        Intent intent = new Intent(context,FetchStoryTask.class);
        intent.putExtra("id",storyId);
        context.startService(intent);
    }
    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        if(intent!=null){
            AVQuery<AVObject> query = new AVQuery<>("Story");
            query.whereEqualTo("creator", AVUser.getCurrentUser());
            query.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> list, AVException e) {
                    if(e == null || !list.isEmpty()){
                        for(AVObject avObject:list){
                            Story story = new Story();
                            story.setObjId(avObject.getObjectId());
                            story.setName(avObject.getString("name"));
                            story.setCreatedAt(avObject.getCreatedAt().getTime());
                            story.setCreatorId(AVUser.getCurrentUser().getUsername());
                            story.setId(avObject.getString("id"));
                            downloadStory(story,avObject.getAVFile("video"));
                        }
                    }
                }
            });

        }
    }

    private void downloadStory(final Story story, AVFile avFile){
        avFile.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, AVException e) {
                if(e == null){
                    String path =System.currentTimeMillis()+".MP4";
                    try {
                        story.setLocalPath(path);
                        FileUtil.createFile(bytes,path);
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        realm.copyToRealm(story);
                        realm.commitTransaction();
                        realm.close();
                    }catch (IOException e2){
                        e2.printStackTrace();
                    }
                }
            }
        });
    }
}
