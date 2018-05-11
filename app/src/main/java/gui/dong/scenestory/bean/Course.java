package gui.dong.scenestory.bean;

import com.avos.avoscloud.AVObject;

import java.util.UUID;

import io.realm.RealmObject;

/**
 * 课程实体类
 */
public class Course extends RealmObject {

    private String objId;
    private String id = UUID.randomUUID().toString();
    private String name;
    private int wordCount;
    private int iconId;
    private String iconUrl;


    public Course(AVObject object) {
        objId = object.getObjectId();
        name = object.getString("name");
        wordCount=object.getInt("wordCount");
        iconUrl = object.getAVFile("icon").getUrl();
    }
    public Course() {

    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getObjId() {
        return objId;
    }

    public void setObjId(String objId) {
        this.objId = objId;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }



}
