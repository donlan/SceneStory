package gui.dong.scenestory.bean;

public class Sound {
    private boolean isPlaying;
    private String name;
    private int rawId;
    private String path;
    private boolean isBackground;

    public Sound(String name, int rawId, String path, boolean isBackground) {
        this.name = name;
        this.rawId = rawId;
        this.path = path;
        this.isBackground = isBackground;
    }

    public void togglePlaying(){
        isPlaying =!isPlaying;
    }
    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRawId() {
        return rawId;
    }

    public void setRawId(int rawId) {
        this.rawId = rawId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isBackground() {
        return isBackground;
    }

    public void setBackground(boolean background) {
        isBackground = background;
    }
}
