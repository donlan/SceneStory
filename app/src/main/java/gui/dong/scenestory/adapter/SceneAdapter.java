package gui.dong.scenestory.adapter;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import gui.dong.scenestory.R;
import gui.dong.scenestory.bean.IStoryElement;

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
