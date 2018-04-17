package gui.dong.scenestory;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;



public class CircleImageButton extends android.support.v7.widget.AppCompatImageButton {

    private int bg_color = Color.BLUE;
    private Paint mPaint;

    public CircleImageButton(Context context) {
        this(context, null);
    }

    public CircleImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleImageButton);
            bg_color = ta.getColor(R.styleable.CircleImageButton_cib_bg_color, bg_color);
            ta.recycle();
        }
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(bg_color);
    }


    public void setBgColor(int bgColor) {
        this.bg_color = bgColor;
        mPaint.setColor(bgColor);
        invalidate();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        int d = Math.min(getWidth(), getHeight());
        canvas.drawCircle(d / 2, d / 2, d / 2, mPaint);
        super.onDraw(canvas);
    }
}
