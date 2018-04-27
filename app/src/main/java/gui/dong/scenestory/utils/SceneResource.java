package gui.dong.scenestory.utils;

import android.content.Context;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

import gui.dong.scenestory.R;
import gui.dong.scenestory.bean.Character;
import gui.dong.scenestory.bean.IStoryElement;
import gui.dong.scenestory.bean.Scene;
import gui.dong.scenestory.bean.Sound;

public class SceneResource {



    public static List<IStoryElement> getLocalCharacter(Context context,String preFix,int count,boolean isScene) {
        List<IStoryElement> characters = new ArrayList<>();
        Resources resources = context.getResources();
        for (int i = 1; i <= count; i++) {
            if(isScene){
                Scene scene = new Scene();
                scene.setResId(resources.getIdentifier(preFix + i, "drawable", context.getPackageName()));
                characters.add(scene);
            }else {
                Character scene = new Character();
                scene.setResId(resources.getIdentifier(preFix + i, "drawable", context.getPackageName()));
                characters.add(scene);
            }
        }
        return characters;
    }

    public static List<Sound> getBackgroudSound(){
        List<Sound> sounds = new ArrayList<>();
        sounds.add(new Sound("百变小樱1", R.raw.bbxy_2,"",true));
        sounds.add(new Sound("百变小樱2", R.raw.bbxy_3,"",true));
        sounds.add(new Sound("百变小樱3", R.raw.bbxy_4,"",true));
        sounds.add(new Sound("百变小樱4", R.raw.bbxy_5,"",true));
        sounds.add(new Sound("百变小樱5", R.raw.bbxy_6,"",true));
        sounds.add(new Sound("百变小樱6", R.raw.bbxy_7,"",true));
        sounds.add(new Sound("百变小樱7", R.raw.bbxy_8,"",true));
        sounds.add(new Sound("百变小樱8", R.raw.bbxy_9,"",true));
        sounds.add(new Sound("百变小樱9", R.raw.bbxy_10,"",true));
        sounds.add(new Sound("哆啦A梦1", R.raw.dlam_1,"",true));
        sounds.add(new Sound("名侦探柯南1", R.raw.mztkn_1,"",true));
        return sounds;
    }
}
