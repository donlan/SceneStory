package gui.dong.scenestory.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import gui.dong.scenestory.R;
import gui.dong.scenestory.bean.Sound;

/**
 * @author 梁桂栋
 * @version 1.0
 * @date 2018/4/27  21:57.
 * e-mail 760625325@qq.com
 * GitHub: https://github.com/donlan
 * description: gui.dong.scenestory.adapter
 */
public class SoundAdapter extends BaseQuickAdapter<Sound,BaseViewHolder> {
    private int lastClick = -1;

    public int getLastClick() {
        return lastClick;
    }

    public void setLastClick(int lastClick) {
        this.lastClick = lastClick;
    }

    public SoundAdapter(@Nullable List<Sound> data) {
        super(R.layout.item_sound,data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Sound item) {
        if(item.isPlaying()){
            helper.setImageResource(R.id.item_sound_img, R.drawable.ic_pause);
        }else {
            helper.setImageResource(R.id.item_sound_img, R.drawable.ic_play_arrow);
        }
        helper.setText(R.id.item_sound_text,item.getName());
    }
}
