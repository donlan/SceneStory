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
 * 课程适配器
 */
public class StudyCourseAdapter extends BaseQuickAdapter<Course, BaseViewHolder> {
    public StudyCourseAdapter(@Nullable List<Course> data) {
        super(R.layout.item_course, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Course item) {
        ImageView icon = helper.getView(R.id.item_course_icon);
        //课程图标
        Glide.with(icon)
                .load(item.getIconUrl())
                .into(icon);
        //课程名
        helper.setText(R.id.item_course_name,item.getName());
        helper.setText(R.id.item_course_intro,"词汇数："+item.getWordCount());
    }
}
