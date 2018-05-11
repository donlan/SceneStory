package gui.dong.scenestory.adapter;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import gui.dong.scenestory.R;
import gui.dong.scenestory.bean.Word;

/**
 * 单词列表适配器
 */
public class WordItemAdapter extends BaseQuickAdapter<Word,BaseViewHolder> {
    public WordItemAdapter(@Nullable List<Word> data) {
        super(R.layout.item_word,data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Word item) {
        ImageView icon = helper.getView(R.id.item_word_icon);
        //单词图片
        Glide.with(icon)
                .load(item.getIconUrl())
                .apply(new RequestOptions().error(R.drawable.u48))
                .into(icon);
        //单词英文
        helper.setText(R.id.item_word_en_name,item.getEnName());
        //单词中文
        helper.setText(R.id.item_word_name,item.getName());
        helper.setText(R.id.item_word_learn_ltv,item.isLearned()?"已学会":"去学习");
    }
}
