package com.xiaopo.flying.sticker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sticker View
 */
public class StickerView extends FrameLayout {

    private boolean showIcons;
    private boolean showBorder;
    private boolean enableBorder;
    private final boolean bringToFrontCurrentSticker;

    @IntDef({
            ActionMode.NONE, ActionMode.DRAG, ActionMode.ZOOM_WITH_TWO_FINGER, ActionMode.ICON,
            ActionMode.CLICK
    })
    @Retention(RetentionPolicy.SOURCE)
    protected @interface ActionMode {
        int NONE = 0;
        int DRAG = 1;
        int ZOOM_WITH_TWO_FINGER = 2;
        int ICON = 3;
        int CLICK = 4;
    }

    @IntDef(flag = true, value = {FLIP_HORIZONTALLY, FLIP_VERTICALLY})
    @Retention(RetentionPolicy.SOURCE)
    protected @interface Flip {
    }

    private static final String TAG = "StickerView";

    private static final int DEFAULT_MIN_CLICK_DELAY_TIME = 200;

    public static final int FLIP_HORIZONTALLY = 1;
    public static final int FLIP_VERTICALLY = 1 << 1;

    private final List<Sticker> stickers = new ArrayList<>();
    private final List<BitmapStickerIcon> icons = new ArrayList<>(4);

    private final Paint borderPaint = new Paint();
    private final RectF stickerRect = new RectF();

    private final Matrix sizeMatrix = new Matrix();
    private final Matrix downMatrix = new Matrix();
    private final Matrix moveMatrix = new Matrix();

    // region storing variables
    private final float[] bitmapPoints = new float[8];
    private final float[] bounds = new float[8];
    private final float[] point = new float[2];
    private final PointF currentCenterPoint = new PointF();
    private final float[] tmp = new float[2];
    private PointF midPoint = new PointF();
    // endregion
    private final int touchSlop;

    private BitmapStickerIcon currentIcon;
    //the first point down position
    private float downX;
    private float downY;

    private float oldDistance = 0f;
    private float oldRotation = 0f;

    @ActionMode
    private int currentMode = ActionMode.NONE;

    private Sticker handlingSticker;

    private boolean locked;
    private boolean constrained;

    private OnStickerOperationListener onStickerOperationListener;

    private long lastClickTime = 0;
    private int minClickDelayTime = DEFAULT_MIN_CLICK_DELAY_TIME;

    public StickerView(Context context) {
        this(context, null);
    }

    public StickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        TypedArray a = null;
        try {
            a = context.obtainStyledAttributes(attrs, R.styleable.StickerView);
            showIcons = a.getBoolean(R.styleable.StickerView_showIcons, false);
            showBorder = a.getBoolean(R.styleable.StickerView_showBorder, false);
            bringToFrontCurrentSticker =
                    a.getBoolean(R.styleable.StickerView_bringToFrontCurrentSticker, false);

            borderPaint.setAntiAlias(true);
            borderPaint.setColor(a.getColor(R.styleable.StickerView_borderColor, Color.BLACK));
            borderPaint.setAlpha(a.getInteger(R.styleable.StickerView_borderAlpha, 128));

            configDefaultIcons();
        } finally {
            if (a != null) {
                a.recycle();
            }
        }
    }

    public boolean isEnableBorder() {
        return enableBorder;
    }

    public void setEnableBorder(boolean enableBorder) {
        this.enableBorder = enableBorder;
        invalidate();
    }

    public void configDefaultIcons() {
        BitmapStickerIcon deleteIcon = new BitmapStickerIcon(
                ContextCompat.getDrawable(getContext(), R.drawable.sticker_ic_close_white_18dp),
                BitmapStickerIcon.LEFT_TOP);
        deleteIcon.setIconEvent(new DeleteIconEvent());
        BitmapStickerIcon zoomIcon = new BitmapStickerIcon(
                ContextCompat.getDrawable(getContext(), R.drawable.sticker_ic_scale_white_18dp),
                BitmapStickerIcon.RIGHT_BOTOM);
        zoomIcon.setIconEvent(new ZoomIconEvent());
        BitmapStickerIcon flipIcon = new BitmapStickerIcon(
                ContextCompat.getDrawable(getContext(), R.drawable.sticker_ic_flip_white_18dp),
                BitmapStickerIcon.RIGHT_TOP);
        flipIcon.setIconEvent(new FlipHorizontallyEvent());

        icons.clear();
        icons.add(deleteIcon);
        icons.add(zoomIcon);
        icons.add(flipIcon);
    }





    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            stickerRect.left = left;
            stickerRect.top = top;
            stickerRect.right = right;
            stickerRect.bottom = bottom;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawStickers(canvas);
    }

    //已经添加的的素材列表
    public List<Sticker> getStickers() {
        return stickers;
    }

    /**
     * 将素材列表逐一绘制到页面上
     * @param canvas
     */
    protected void drawStickers(Canvas canvas) {
        for (int i = 0; i < stickers.size(); i++) {
            Sticker sticker = stickers.get(i);
            if (sticker != null) {
                sticker.draw(canvas);
                //绘制素材边框
                if (enableBorder && showBorder) {
                    getStickerPoints(sticker, bitmapPoints);

                    float x1 = bitmapPoints[0];
                    float y1 = bitmapPoints[1];
                    float x2 = bitmapPoints[2];
                    float y2 = bitmapPoints[3];
                    float x3 = bitmapPoints[4];
                    float y3 = bitmapPoints[5];
                    float x4 = bitmapPoints[6];
                    float y4 = bitmapPoints[7];

                    if (showBorder) {
                        canvas.drawLine(x1, y1, x2, y2, borderPaint);
                        canvas.drawLine(x1, y1, x3, y3, borderPaint);
                        canvas.drawLine(x2, y2, x4, y4, borderPaint);
                        canvas.drawLine(x4, y4, x3, y3, borderPaint);
                    }
                }
            }
        }

        if (handlingSticker != null && !locked && (showBorder || showIcons)) {

            getStickerPoints(handlingSticker, bitmapPoints);

            float x1 = bitmapPoints[0];
            float y1 = bitmapPoints[1];
            float x2 = bitmapPoints[2];
            float y2 = bitmapPoints[3];
            float x3 = bitmapPoints[4];
            float y3 = bitmapPoints[5];
            float x4 = bitmapPoints[6];
            float y4 = bitmapPoints[7];

            //draw icons
            if (showIcons) {
                float rotation = calculateRotation(x4, y4, x3, y3);
                for (int i = 0; i < icons.size(); i++) {
                    BitmapStickerIcon icon = icons.get(i);
                    switch (icon.getPosition()) {
                        case BitmapStickerIcon.LEFT_TOP:

                            configIconMatrix(icon, x1, y1, rotation);
                            break;

                        case BitmapStickerIcon.RIGHT_TOP:
                            configIconMatrix(icon, x2, y2, rotation);
                            break;

                        case BitmapStickerIcon.LEFT_BOTTOM:
                            configIconMatrix(icon, x3, y3, rotation);
                            break;

                        case BitmapStickerIcon.RIGHT_BOTOM:
                            configIconMatrix(icon, x4, y4, rotation);
                            break;
                    }
                    icon.draw(canvas, borderPaint);
                }
            }
        }
    }

    protected void configIconMatrix(@NonNull BitmapStickerIcon icon, float x, float y,
                                    float rotation) {
        icon.setX(x);
        icon.setY(y);
        icon.getMatrix().reset();

        icon.getMatrix().postRotate(rotation, icon.getWidth() / 2, icon.getHeight() / 2);
        icon.getMatrix().postTranslate(x - icon.getWidth() / 2, y - icon.getHeight() / 2);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (locked) {
            return super.onInterceptTouchEvent(ev);
        }

        int i = ev.getAction();
        if (i == MotionEvent.ACTION_DOWN) {
            downX = ev.getX();
            downY = ev.getY();

            return findCurrentIconTouched() != null || findHandlingSticker() != null;
        }

        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 处理触摸事件，点击 拖动动等
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (locked) {
            return super.onTouchEvent(event);
        }

        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            //手指点击屏幕
            case MotionEvent.ACTION_DOWN:
                if (!onTouchDown(event)) {
                    return false;
                }
                break;
                //多个手指按下屏幕
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDistance = calculateDistance(event);
                oldRotation = calculateRotation(event);

                midPoint = calculateMidPoint(event);

                if (handlingSticker != null && isInStickerArea(handlingSticker, event.getX(1),
                        event.getY(1)) && findCurrentIconTouched() == null) {
                    currentMode = ActionMode.ZOOM_WITH_TWO_FINGER;
                }
                break;

                //手指在屏幕移动
            case MotionEvent.ACTION_MOVE:
                handleCurrentMode(event);
                invalidate();
                break;

                //手指离开屏幕
            case MotionEvent.ACTION_UP:
                showBorder =false;
                onTouchUp(event);
                break;

                //多个手机点击屏幕时，手指离开屏幕
            case MotionEvent.ACTION_POINTER_UP:
                showBorder = false;
                if (currentMode == ActionMode.ZOOM_WITH_TWO_FINGER && handlingSticker != null) {
                    if (onStickerOperationListener != null) {
                        onStickerOperationListener.onStickerZoomFinished(handlingSticker);
                    }
                }
                currentMode = ActionMode.NONE;
                break;
        }

        return true;
    }

    /**
     * @param event MotionEvent received from {@link #onTouchEvent)
     * @return true if has touch something
     */
    protected boolean onTouchDown(@NonNull MotionEvent event) {
        currentMode = ActionMode.DRAG;

        downX = event.getX();
        downY = event.getY();

        midPoint = calculateMidPoint();
        oldDistance = calculateDistance(midPoint.x, midPoint.y, downX, downY);
        oldRotation = calculateRotation(midPoint.x, midPoint.y, downX, downY);

        currentIcon = findCurrentIconTouched();
        if (currentIcon != null) {
            currentMode = ActionMode.ICON;
            currentIcon.onActionDown(this, event);
        } else {
            handlingSticker = findHandlingSticker();
        }

        if (handlingSticker != null) {
            onStickerOperationListener.onStickerTouchedDown(handlingSticker);
            downMatrix.set(handlingSticker.getMatrix());
            if (bringToFrontCurrentSticker) {
                stickers.remove(handlingSticker);
                stickers.add(handlingSticker);
            }
        }

        if (currentIcon == null && handlingSticker == null) {
            return false;
        }
        showBorder = true;
        invalidate();
        return true;
    }

    protected void onTouchUp(@NonNull MotionEvent event) {
        long currentTime = SystemClock.uptimeMillis();

        if (currentMode == ActionMode.ICON && currentIcon != null && handlingSticker != null) {
            currentIcon.onActionUp(this, event);
        }

        if (currentMode == ActionMode.DRAG
                && Math.abs(event.getX() - downX) < touchSlop
                && Math.abs(event.getY() - downY) < touchSlop
                && handlingSticker != null) {
            currentMode = ActionMode.CLICK;
            if (onStickerOperationListener != null) {
                onStickerOperationListener.onStickerClicked(handlingSticker);
            }
            if (currentTime - lastClickTime < minClickDelayTime) {
                if (onStickerOperationListener != null) {
                    onStickerOperationListener.onStickerDoubleTapped(handlingSticker);
                }
            }
        }

        if (currentMode == ActionMode.DRAG && handlingSticker != null) {
            if (onStickerOperationListener != null) {
                onStickerOperationListener.onStickerDragFinished(handlingSticker);
            }
        }

        currentMode = ActionMode.NONE;
        lastClickTime = currentTime;
    }

    protected void handleCurrentMode(@NonNull MotionEvent event) {
        switch (currentMode) {
            case ActionMode.NONE:
            case ActionMode.CLICK:
                break;
            case ActionMode.DRAG:
                if (handlingSticker != null) {
                    moveMatrix.set(downMatrix);
                    moveMatrix.postTranslate(event.getX() - downX, event.getY() - downY);
                    handlingSticker.setMatrix(moveMatrix);
                    if (constrained) {
                        constrainSticker(handlingSticker);
                    }
                }
                break;
            case ActionMode.ZOOM_WITH_TWO_FINGER:
                if (handlingSticker != null) {
                    float newDistance = calculateDistance(event);
                    float newRotation = calculateRotation(event);

                    moveMatrix.set(downMatrix);
                    moveMatrix.postScale(newDistance / oldDistance, newDistance / oldDistance, midPoint.x,
                            midPoint.y);
                    moveMatrix.postRotate(newRotation - oldRotation, midPoint.x, midPoint.y);
                    handlingSticker.setMatrix(moveMatrix);
                }

                break;

            case ActionMode.ICON:
                if (handlingSticker != null && currentIcon != null) {
                    currentIcon.onActionMove(this, event);
                }
                break;
        }
    }

    /**
     * 对当前操控的贴图进行缩放旋转
     * @param event
     */
    public void zoomAndRotateCurrentSticker(@NonNull MotionEvent event) {
        zoomAndRotateSticker(handlingSticker, event);
    }

    /**
     * 对贴图进行缩放旋转
     * @param sticker 指定需要缩放旋转的贴图
     * @param event
     */
    public void zoomAndRotateSticker(@Nullable Sticker sticker, @NonNull MotionEvent event) {
        if (sticker != null) {
            float newDistance = calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
            float newRotation = calculateRotation(midPoint.x, midPoint.y, event.getX(), event.getY());

            moveMatrix.set(downMatrix);
            moveMatrix.postScale(newDistance / oldDistance, newDistance / oldDistance, midPoint.x,
                    midPoint.y);
            moveMatrix.postRotate(newRotation - oldRotation, midPoint.x, midPoint.y);
            handlingSticker.setMatrix(moveMatrix);
        }
    }

    /**
     * 控制贴图的平移
     * @param sticker
     */
    protected void constrainSticker(@NonNull Sticker sticker) {
        float moveX = 0;
        float moveY = 0;
        int width = getWidth();
        int height = getHeight();
        sticker.getMappedCenterPoint(currentCenterPoint, point, tmp);
        if (currentCenterPoint.x < 0) {
            moveX = -currentCenterPoint.x;
        }

        if (currentCenterPoint.x > width) {
            moveX = width - currentCenterPoint.x;
        }

        if (currentCenterPoint.y < 0) {
            moveY = -currentCenterPoint.y;
        }

        if (currentCenterPoint.y > height) {
            moveY = height - currentCenterPoint.y;
        }

        sticker.getMatrix().postTranslate(moveX, moveY);
    }

    @Nullable
    protected BitmapStickerIcon findCurrentIconTouched() {
        for (BitmapStickerIcon icon : icons) {
            float x = icon.getX() - downX;
            float y = icon.getY() - downY;
            float distance_pow_2 = x * x + y * y;
            if (distance_pow_2 <= Math.pow(icon.getIconRadius() + icon.getIconRadius(), 2)) {
                return icon;
            }
        }

        return null;
    }

    /**
     * 根据手指按下的位置找到点击的素材
     **/
    @Nullable
    protected Sticker findHandlingSticker() {
        for (int i = stickers.size() - 1; i >= 0; i--) {
            if (isInStickerArea(stickers.get(i), downX, downY)) {
                return stickers.get(i);
            }
        }
        return null;
    }

    //手指点击的位置是否在素材的显示区域内
    protected boolean isInStickerArea(@NonNull Sticker sticker, float downX, float downY) {
        tmp[0] = downX;
        tmp[1] = downY;
        return sticker.contains(tmp);
    }

    /**
     * 计算两个手指按下屏幕的时候，按下两点的中点
     * @param event
     * @return
     */
    @NonNull
    protected PointF calculateMidPoint(@Nullable MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) {
            midPoint.set(0, 0);
            return midPoint;
        }
        float x = (event.getX(0) + event.getX(1)) / 2;
        float y = (event.getY(0) + event.getY(1)) / 2;
        midPoint.set(x, y);
        return midPoint;
    }

    @NonNull
    protected PointF calculateMidPoint() {
        if (handlingSticker == null) {
            midPoint.set(0, 0);
            return midPoint;
        }
        handlingSticker.getMappedCenterPoint(midPoint, point, tmp);
        return midPoint;
    }

    /**
     * 根据两个手指按下的坐标点计算旋转角度
     **/
    protected float calculateRotation(@Nullable MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) {
            return 0f;
        }
        return calculateRotation(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
    }

    protected float calculateRotation(float x1, float y1, float x2, float y2) {
        double x = x1 - x2;
        double y = y1 - y2;
        double radians = Math.atan2(y, x);
        return (float) Math.toDegrees(radians);
    }

    /**
     * 计算两个手指按下屏幕的坐标点间的距离
     **/
    protected float calculateDistance(@Nullable MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) {
            return 0f;
        }
        return calculateDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
    }

    protected float calculateDistance(float x1, float y1, float x2, float y2) {
        double x = x1 - x2;
        double y = y1 - y2;

        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        for (int i = 0; i < stickers.size(); i++) {
            Sticker sticker = stickers.get(i);
            if (sticker != null) {
                transformSticker(sticker);
            }
        }
    }

    /**
     *
     * 某些素材图片可能过大或者过小，通过缩放使每个素材显示保持一个统一的大小
     * step 1：let the center of the sticker image is coincident with the center of the View.
     * step 2：Calculate the zoom and zoom
     **/
    protected void transformSticker(@Nullable Sticker sticker) {
        if (sticker == null) {
            Log.e(TAG, "transformSticker: the bitmapSticker is null or the bitmapSticker bitmap is null");
            return;
        }

        sizeMatrix.reset();

        float width = getWidth();
        float height = getHeight();
        float stickerWidth = sticker.getWidth();
        float stickerHeight = sticker.getHeight();
        //step 1
        float offsetX = (width - stickerWidth) / 2;
        float offsetY = (height - stickerHeight) / 2;

        sizeMatrix.postTranslate(offsetX, offsetY);

        //step 2
        float scaleFactor;
        if (width < height) {
            scaleFactor = width / stickerWidth;
        } else {
            scaleFactor = height / stickerHeight;
        }

        sizeMatrix.postScale(scaleFactor / 1f, scaleFactor / 1f, width / 2f, height / 2f);

        sticker.getMatrix().reset();
        sticker.setMatrix(sizeMatrix);

        invalidate();
    }

    public void flipCurrentSticker(int direction) {
        flip(handlingSticker, direction);
    }

    //对素材图片进行镜像翻转
    public void flip(@Nullable Sticker sticker, @Flip int direction) {
        if (sticker != null) {
            sticker.getCenterPoint(midPoint);
            if ((direction & FLIP_HORIZONTALLY) > 0) {
                sticker.getMatrix().preScale(-1, 1, midPoint.x, midPoint.y);
                sticker.setFlippedHorizontally(!sticker.isFlippedHorizontally());
            }
            if ((direction & FLIP_VERTICALLY) > 0) {
                sticker.getMatrix().preScale(1, -1, midPoint.x, midPoint.y);
                sticker.setFlippedVertically(!sticker.isFlippedVertically());
            }

            if (onStickerOperationListener != null) {
                onStickerOperationListener.onStickerFlipped(sticker);
            }

            invalidate();
        }
    }

    public boolean replace(@Nullable Sticker sticker) {
        return replace(sticker, true);
    }

    /**
     * 指定某个素材进行替换
     * @param sticker 用来替换的熟菜
     * @param needStayState 保留被替换素材的状态：缩放，旋转，大小
     * @return
     */
    public boolean replace(@Nullable Sticker sticker, boolean needStayState) {
        if (handlingSticker != null && sticker != null) {
            float width = getWidth();
            float height = getHeight();
            if (needStayState) {
                sticker.setMatrix(handlingSticker.getMatrix());
                sticker.setFlippedVertically(handlingSticker.isFlippedVertically());
                sticker.setFlippedHorizontally(handlingSticker.isFlippedHorizontally());
            } else {
                handlingSticker.getMatrix().reset();
                // reset scale, angle, and put it in center
                float offsetX = (width - handlingSticker.getWidth()) / 2f;
                float offsetY = (height - handlingSticker.getHeight()) / 2f;
                sticker.getMatrix().postTranslate(offsetX, offsetY);

                float scaleFactor;
                if (width < height) {
                    scaleFactor = width / handlingSticker.getDrawable().getIntrinsicWidth();
                } else {
                    scaleFactor = height / handlingSticker.getDrawable().getIntrinsicHeight();
                }
                sticker.getMatrix().postScale(scaleFactor / 2f, scaleFactor / 2f, width / 2f, height / 2f);
            }
            int index = stickers.indexOf(handlingSticker);
            stickers.set(index, sticker);
            handlingSticker = sticker;

            invalidate();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 移除指定素材
     * @param sticker
     * @return
     */
    public boolean remove(@Nullable Sticker sticker) {
        if (stickers.contains(sticker)) {
            stickers.remove(sticker);
            if (onStickerOperationListener != null) {
                onStickerOperationListener.onStickerDeleted(sticker);
            }
            if (handlingSticker == sticker) {
                handlingSticker = null;
            }
            invalidate();

            return true;
        } else {
            Log.d(TAG, "remove: the sticker is not in this StickerView");

            return false;
        }
    }

    /**
     * 移除当前素材（手指点击的，或刚添加的）
     * @return
     */
    public boolean removeCurrentSticker() {
        return remove(handlingSticker);
    }

    public void removeAllStickers() {
        stickers.clear();
        if (handlingSticker != null) {
            handlingSticker.release();
            handlingSticker = null;
        }
        invalidate();
    }

    /**
     * 添加一个素材
     * @param sticker
     * @return
     */
    @NonNull
    public StickerView addSticker(@NonNull Sticker sticker) {
        return addSticker(sticker, Sticker.Position.CENTER);
    }

    @NonNull
    public StickerView addSticker(@NonNull final Sticker sticker, final float scale) {
        if (ViewCompat.isLaidOut(this)) {
            addStickerImmediately(sticker, Sticker.Position.CENTER, scale);
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    addStickerImmediately(sticker, Sticker.Position.CENTER, scale);
                }
            });
        }
        return this;
    }

    /**
     * 添加一个素材
     * @param sticker 添加的素材
     * @param position 相对屏幕的位置
     * @return
     */
    public StickerView addSticker(@NonNull final Sticker sticker,
                                  final @Sticker.Position int position) {
        if (ViewCompat.isLaidOut(this)) {
            addStickerImmediately(sticker, position);
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    addStickerImmediately(sticker, position);
                }
            });
        }
        return this;
    }

    protected void addStickerImmediately(@NonNull Sticker sticker, @Sticker.Position int position) {
        setStickerPosition(sticker, position);


        float scaleFactor, widthScaleFactor, heightScaleFactor;

        widthScaleFactor = (float) getWidth() / sticker.getDrawable().getIntrinsicWidth();
        heightScaleFactor = (float) getHeight() / sticker.getDrawable().getIntrinsicHeight();
        scaleFactor = widthScaleFactor > heightScaleFactor ? heightScaleFactor : widthScaleFactor;

        addStickerImmediately(sticker, position, scaleFactor);

    }

    protected void addStickerImmediately(@NonNull Sticker sticker, @Sticker.Position int position, float scale) {

        sticker.getMatrix()
                .postScale(scale / 2, scale / 2, getWidth() / 2, getHeight() / 2);

        handlingSticker = sticker;
        stickers.add(sticker);
        if (onStickerOperationListener != null) {
            onStickerOperationListener.onStickerAdded(sticker);
        }
        invalidate();
    }

    protected void setStickerPosition(@NonNull Sticker sticker, @Sticker.Position int position) {
        float width = getWidth();
        float height = getHeight();
        float offsetX = width - sticker.getWidth();
        float offsetY = height - sticker.getHeight();
        if ((position & Sticker.Position.TOP) > 0) {
            offsetY /= 4f;
        } else if ((position & Sticker.Position.BOTTOM) > 0) {
            offsetY *= 3f / 4f;
        } else {
            offsetY /= 2f;
        }
        if ((position & Sticker.Position.LEFT) > 0) {
            offsetX /= 4f;
        } else if ((position & Sticker.Position.RIGHT) > 0) {
            offsetX *= 3f / 4f;
        } else {
            offsetX /= 2f;
        }
        sticker.getMatrix().postTranslate(offsetX, offsetY);
    }

    @NonNull
    public float[] getStickerPoints(@Nullable Sticker sticker) {
        float[] points = new float[8];
        getStickerPoints(sticker, points);
        return points;
    }

    /**
     * 获取指定素材的四个矩形角的坐标值
     *
     * @param sticker 素材
     * @param dst     坐标值通过给定的数组返回
     */
    public void getStickerPoints(@Nullable Sticker sticker, @NonNull float[] dst) {
        if (sticker == null) {
            Arrays.fill(dst, 0);
            return;
        }
        sticker.getBoundPoints(bounds);
        sticker.getMappedPoints(dst, bounds);
    }


    /**
     * 将两个素材合并位一个
     *
     * @param backSticker 位于下面的素材
     * @param upSticker   当前移动操作的位于上方的素材
     */
    public void concat(Sticker backSticker, Sticker upSticker) {
        //计算两个素材的四个矩形角坐标
        float[] backBound = new float[8];
        getStickerPoints(backSticker, backBound);
        float[] upBound = new float[8];
        getStickerPoints(upSticker, upBound);
        //计算两个矩形重合后形成的最大矩形边界
        float top = Math.min(Math.min(backBound[1], upBound[1]), Math.min(backBound[3], upBound[3]));
        float left = Math.min(Math.min(backBound[0], upBound[0]), Math.min(backBound[2], upBound[2]));
        float right = Math.max(Math.max(backBound[2], upBound[2]), Math.max(backBound[6], upBound[6]));
        float bottom = Math.max(Math.max(backBound[5], upBound[5]), Math.max(backBound[7], upBound[7]));
        Log.e("test",left+","+top+"  "+right+","+bottom);
        //根据合成的矩形边界生成对应大小的画布
        Bitmap bitmap = Bitmap.createBitmap((int) (right - left), (int) (bottom - top), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        //计算两个素材的缩放，选中，平移值
        Matrix backMatrix = new Matrix();
        Matrix upMatrix = new Matrix();

        backMatrix.postScale(backSticker.getMatrixValue(backSticker.getMatrix(),Matrix.MSCALE_X), backSticker.getMatrixValue(backSticker.getMatrix(),Matrix.MSCALE_Y));
        backMatrix.postRotate(backSticker.getCurrentAngle());
        backMatrix.postTranslate(backBound[0] - left, backBound[1] - top);
        upMatrix.postScale(upSticker.getMatrixValue(upSticker.getMatrix(),Matrix.MSCALE_X), upSticker.getMatrixValue(upSticker.getMatrix(),Matrix.MSCALE_Y));
        upMatrix.postRotate(upSticker.getCurrentAngle());
        upMatrix.postTranslate(upBound[0] - left, upBound[1] - top);
        //将经过缩放，旋转，平移的素材重新会知道画布上
        canvas.drawBitmap(((BitmapDrawable) backSticker.getDrawable()).getBitmap(),
                backMatrix,
                paint
        );
        canvas.drawBitmap(((BitmapDrawable) upSticker.getDrawable()).getBitmap(),
                upMatrix,
                paint
        );
        //从画布取出绘制结果，并添加到当前的场景中
        BitmapStickerIcon bitmapStickerIcon = new BitmapStickerIcon(new BitmapDrawable(getResources(),bitmap), BitmapStickerIcon.LEFT_TOP);
        if (stickers.remove(upSticker)) {
            stickers.remove(backSticker);
            addSticker(bitmapStickerIcon);
        }
    }

    @NonNull
    public Bitmap createBitmap() throws OutOfMemoryError {
        handlingSticker = null;
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);
        return bitmap;
    }

    public int getStickerCount() {
        return stickers.size();
    }

    public boolean isNoneSticker() {
        return getStickerCount() == 0;
    }

    public boolean isLocked() {
        return locked;
    }

    @NonNull
    public StickerView setLocked(boolean locked) {
        this.locked = locked;
        invalidate();
        return this;
    }

    @NonNull
    public StickerView setMinClickDelayTime(int minClickDelayTime) {
        this.minClickDelayTime = minClickDelayTime;
        return this;
    }

    public int getMinClickDelayTime() {
        return minClickDelayTime;
    }

    public boolean isConstrained() {
        return constrained;
    }

    @NonNull
    public StickerView setConstrained(boolean constrained) {
        this.constrained = constrained;
        postInvalidate();
        return this;
    }

    @NonNull
    public StickerView setOnStickerOperationListener(
            @Nullable OnStickerOperationListener onStickerOperationListener) {
        this.onStickerOperationListener = onStickerOperationListener;
        return this;
    }

    @Nullable
    public OnStickerOperationListener getOnStickerOperationListener() {
        return onStickerOperationListener;
    }

    @Nullable
    public Sticker getCurrentSticker() {
        return handlingSticker;
    }

    @NonNull
    public List<BitmapStickerIcon> getIcons() {
        return icons;
    }

    public void setIcons(@NonNull List<BitmapStickerIcon> icons) {
        this.icons.clear();
        this.icons.addAll(icons);
        invalidate();
    }

    /**
     * 判断两个素材在页面显示中是否重合
     * @param backSticker
     * @param upSticker
     * @return
     */
    public boolean isContains(Sticker backSticker, Sticker upSticker) {
        float[] backBound = new float[8];
        float[] upBound = new float[8];
        getStickerPoints(backSticker, backBound);
        getStickerPoints(upSticker, upBound);
        /**
         * 上面的素材的四个角的坐标是否在下面的素材的矩形区域内
         */
        Region region = new Region();
        region.set((int) backBound[0], (int) backBound[1], (int) backBound[6], (int) backBound[7]);
        if (region.contains((int) upBound[0], (int) upBound[1])) {
            return true;
        }
        if (region.contains((int) upBound[2], (int) upBound[3])) {
            return true;
        }
        if (region.contains((int) upBound[4], (int) upBound[5])) {
            return true;
        }
        if (region.contains((int) upBound[6], (int) upBound[7])) {
            return true;
        }
        /**
         * 下面的素材的四个角的坐标是否在上面的素材的矩形区域内
         */
        region.set((int) upBound[0], (int) upBound[1], (int) upBound[6], (int) upBound[7]);
        if (region.contains((int) backBound[0], (int) backBound[1])) {
            return true;
        }
        if (region.contains((int) backBound[2], (int) backBound[3])) {
            return true;
        }
        if (region.contains((int) backBound[4], (int) backBound[5])) {
            return true;
        }
        if (region.contains((int) backBound[6], (int) backBound[7])) {
            return true;
        }
        return false;
    }

    /**
     * 贴图操作回调接口
     */
    public interface OnStickerOperationListener {
        /**
         * 新添加一个贴图时回调
         * @param sticker
         */
        void onStickerAdded(@NonNull Sticker sticker);

        /**
         * 贴图被点击时回调
         * @param sticker
         */
        void onStickerClicked(@NonNull Sticker sticker);

        /**
         * 贴图被删除的时候回调
         * @param sticker
         */
        void onStickerDeleted(@NonNull Sticker sticker);

        /**
         * 贴图拖动结束（手指离开屏幕）时回调
         * @param sticker
         */
        void onStickerDragFinished(@NonNull Sticker sticker);

        /**
         * 手指刚按下贴图的时候回调
         * @param sticker
         */
        void onStickerTouchedDown(@NonNull Sticker sticker);

        /**
         * 双指对贴图缩放结束时回调
         * @param sticker
         */
        void onStickerZoomFinished(@NonNull Sticker sticker);

        /**
         * 对贴图镜像翻转后回调
         * @param sticker
         */
        void onStickerFlipped(@NonNull Sticker sticker);

        /**
         * 双击贴图时回调
         * @param sticker
         */
        void onStickerDoubleTapped(@NonNull Sticker sticker);
    }
}
