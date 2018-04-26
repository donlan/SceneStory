package gui.dong.scenestory.utils;

import android.content.Context;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

import gui.dong.scenestory.bean.Character;
import gui.dong.scenestory.bean.IStoryElement;
import gui.dong.scenestory.bean.Scene;

public class ImageResource {



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
}
