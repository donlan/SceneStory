package gui.dong.scenestory.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 作者：梁桂栋
 * 日期： 2015/12/7  05:37.
 * <p>
 * <p>
 * 引导页的滑动 PagerAdapter
 */
public class MyPagerAdapter extends PagerAdapter {


    private List<? extends View> views;

    public MyPagerAdapter(List<? extends View> v) {
        views = v;
    }


    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position), 0);
        return views.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return views.get(position).getTag().toString();
    }
}
