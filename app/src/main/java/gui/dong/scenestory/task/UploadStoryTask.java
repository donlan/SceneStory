package gui.dong.scenestory.task;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;

import java.io.FileNotFoundException;

import gui.dong.scenestory.bean.Story;
import gui.dong.scenestory.utils.FileUtils;
import io.realm.Realm;

/**
 * @author 梁桂栋
 * @version 1.0
 * @date 2018/4/14  19:39.
 * e-mail 760625325@qq.com
 * GitHub: https://github.com/donlan
 * description: gui.dong.scenestory.task
 */
public class UploadStoryTask extends IntentService {
    private AVObject avObject;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UploadStoryTask(String name) {
        super(name);
    }

    public UploadStoryTask() {
        super("UploadStoryTask");
    }

    public static void start(Context context,String storyId){
        Intent intent = new Intent(context,UploadStoryTask.class);
        intent.putExtra("id",storyId);
        context.startService(intent);
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent!=null){
            String stroryId= intent.getStringExtra("id");
            Realm realm =Realm.getDefaultInstance();
            Story story = realm.where(Story.class)
                    .equalTo("id",stroryId)
                    .findFirst();
            if(story!=null){
                try {
                    AVFile avFile = AVFile.withAbsoluteLocalPath(FileUtils.PathToFileName(story.getLocalPath()),story.getLocalPath());
                    avFile.save();
                    avObject = AVObject.create("Story");
                    avObject.put("name",story.getName());
                    avObject.put("id",story.getId());
                    avObject.put("video",avFile);
                    avObject.put("creator", AVUser.getCurrentUser());
                    avObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if(e == null){
                                Realm realm =Realm.getDefaultInstance();
                                Story story = realm.where(Story.class)
                                        .equalTo("id",avObject.getString("id"))
                                        .findFirst();
                                if(story!=null){
                                    realm.beginTransaction();
                                    story.setObjId(avObject.getObjectId());
                                    realm.commitTransaction();
                                }
                                realm.close();
                            }
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (AVException e) {
                    e.printStackTrace();
                }finally {
                    realm.close();
                }
            }
        }
    }
}
