package gui.dong.scenestory.adapter;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import gui.dong.scenestory.R;
import gui.dong.scenestory.bean.Course;

/**
 * @author 梁桂栋
 * @version 1.0
 * @date 2018/5/6  0:20.
 * e-mail 760625325@qq.com
 * GitHub: https://github.com/donlan
 * description: gui.dong.scenestory.adapter
 */
public class StudyCourseAdapter extends BaseQuickAdapter<Course, BaseViewHolder> {
    public StudyCourseAdapter(@Nullable List<Course> data) {
        super(R.layout.item_course, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Course item) {
        ImageView icon = helper.getView(R.id.item_course_icon);
        Glide.with(icon)
                .load(item.getIconUrl())
                .into(icon);
        helper.setText(R.id.item_course_name,item.getName());
        helper.setText(R.id.item_course_intro,"词汇数："+item.getWordCount());
    }
}
