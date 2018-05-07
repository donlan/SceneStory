package gui.dong.scenestory.bean;

import com.avos.avoscloud.AVObject;

import io.realm.RealmObject;

public class Word extends RealmObject {
    private String id;
    private String courseId;
    private String name;
    private String enName;
    private int iconId;
    private String iconUrl;
    private String pinyin;
    private String enSoundUrl;
    private boolean isLearned;

    public Word() {
    }

    public Word(AVObject object) {
        id = object.getObjectId();
        courseId= object.getAVObject("course").getObjectId();
        name = object.getString("cnName");
        enName = object.getString("enName");
        iconUrl = object.getAVFile("icon").getUrl();
        pinyin = object.getString("cnPinyin");
        enSoundUrl = object.getString("enSoundUrl");
        isLearned = false;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getEnSoundUrl() {
        return enSoundUrl;
    }

    public void setEnSoundUrl(String enSoundUrl) {
        this.enSoundUrl = enSoundUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public boolean isLearned() {
        return isLearned;
    }

    public void setLearned(boolean learned) {
        isLearned = learned;
    }
}
