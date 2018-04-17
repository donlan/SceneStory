package gui.dong.scenestory.adapter;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import gui.dong.scenestory.R;
import gui.dong.scenestory.bean.IStoryElement;

/**
 * @author 梁桂栋
 * @version 1.0
 * @date 2018/4/8  13:53.
 * e-mail 760625325@qq.com
 * GitHub: https://github.com/donlan
 * description: gui.dong.scenestory.adapter
 */
public class SceneAdapter extends BaseQuickAdapter<IStoryElement,BaseViewHolder> {
    public SceneAdapter(OnItemClickListener listener, List<IStoryElement> elements) {
        super(R.layout.item_scene,elements);
        setOnItemClickListener(listener);
    }

    @Override
    protected void convert(BaseViewHolder helper, IStoryElement item) {
        ImageView sceneImg = helper.getView(R.id.sceneIv);
        Glide.with(sceneImg)
                .load(item.resId())
                .into(sceneImg);
    }
}
