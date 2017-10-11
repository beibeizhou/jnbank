package com.centerm.jnbank.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * 手写板
 *
 * @ClassName HandwritePad
 */
public class HandwrittenPad extends View {
    private String tag = HandwrittenPad.class.getSimpleName();
    private Paint paint;
    private Canvas cacheCanvas;
    private Bitmap cachebBitmap;
    private Path path;
    private int width;
    private int height;
    private float cur_x, cur_y;
    private int strokeNumber = 0;
    private boolean isSign = false;    // 是否有签字

	// 以下几个变量保存签名的有效区域
	private float validLeft = 0; // 签名区域的最左端
	private float validTop = 0; // 签名区域的最顶端
	private float validRight = 0; // 签名区域的最右端
	private float validBottom = 0;// 签名区域的最底端
	
	private String signature;		// 水印
	private float signSize = 50f;	// 水印字体大小
	private int watermarkColor = Color.LTGRAY;	// 水印
	private static final float WRITE_STROLE_WIDTH = 7f;	// 手写笔画宽度
	private static final float WATERMARK_STROKE_WIDTH = 0.5f;	// 水印笔画宽度

	public HandwrittenPad(Context context) {
		super(context);
	}

	public HandwrittenPad(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		width = measureWidth(widthMeasureSpec);
		height = measureHeight(heightMeasureSpec);
		setMeasuredDimension(width, height);
		try {
			init(width, height);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int measureHeight(int measureSpec) {
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		
		int result = 500;
		if (specMode == MeasureSpec.AT_MOST) {
			result = specSize;
		} else if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		}

		return result;
	}

	private int measureWidth(int measureSpec) {
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		int result = 500;
		if (specMode == MeasureSpec.AT_MOST) {
			result = specSize;
		} else if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		}
		return result;
	}

    private void init(int width, int height) {
        requestFocus();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(WRITE_STROLE_WIDTH);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        path = new Path();
        if (cachebBitmap == null) {
            isSign = false;
            cachebBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        }
        validLeft = cachebBitmap.getWidth();
        validTop = cachebBitmap.getHeight();
        if (cacheCanvas == null) {
            cacheCanvas = new Canvas(cachebBitmap);
            cacheCanvas.drawColor(Color.WHITE);
            try {
                if (!TextUtils.isEmpty(signature)) {
                    watermarkBitmap(cachebBitmap, signature, signSize);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            cacheCanvas.save();
            cacheCanvas.restore();
        }
    }

	public void clear() {
		if (cacheCanvas != null && cachebBitmap != null) {
			strokeNumber = 0;
			cacheCanvas.drawPaint(paint);
			paint.setColor(Color.BLACK);
			cacheCanvas.drawColor(Color.WHITE);
			validLeft = cachebBitmap.getWidth();
			validTop = cachebBitmap.getHeight();
			validRight = 0;
			validBottom = 0;
			if(!TextUtils.isEmpty(signature)) {
				watermarkBitmap(cachebBitmap, signature, signSize);
			}
			invalidate();
		}
		
		isSign = false;
	}

	public synchronized int getStrokeNumber() {
		return strokeNumber;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(cachebBitmap, 0, 0, null);
		canvas.drawPath(path, paint);
	}

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.i(tag, "onSizeChanged");
        int curW = cachebBitmap != null ? cachebBitmap.getWidth() : 0;
        int curH = cachebBitmap != null ? cachebBitmap.getHeight() : 0;

        curW = Math.max(curW, w);
        curH = Math.max(curH, h);
        Bitmap newBitmap = Bitmap.createBitmap(curW, curH,
                Config.ARGB_8888);
        Canvas newCanvas = new Canvas();
        newCanvas.setBitmap(newBitmap);
        if (cachebBitmap != null) {
            newCanvas.drawBitmap(cachebBitmap, 0, 0, null);
        }
        cachebBitmap = newBitmap;
        cacheCanvas = newCanvas;
    }

    public Bitmap getCachebBitmap() {
        return cachebBitmap;
    }


    @Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		if (x <= validLeft) {
			validLeft = x;
		}
		if (x >= validRight) {
			validRight = x;
		}

		if (y <= validTop) {
			validTop = y;
		}
		if (y >= validBottom) {
			validBottom = y;
		}

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cur_x = x;
                cur_y = y;
                path.moveTo(cur_x, cur_y);
                break;
            case MotionEvent.ACTION_MOVE:
                path.quadTo(cur_x, cur_y, x, y);
                cur_x = x;
                cur_y = y;
                isSign = true;
                break;
            case MotionEvent.ACTION_UP:
                cacheCanvas.drawPath(path, paint);
                cacheCanvas.save();
                path.reset();
                strokeNumber++;
                break;
        }
        invalidate();
        return true;
    }

    /**
     * 设置文字水印
     *
     * @param src
     * @param mark
     * @return
     * @date 2015年2月1日 下午8:41:41
     * @since 1.0
     */
    public Bitmap watermarkBitmap(Bitmap src, String mark, float size) {
        Canvas canvas = new Canvas(src);// 初始化画布 绘制的图像到icon上
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);// 设置画笔
        textPaint.setTextSize(size);// 字体大小
        textPaint.setTypeface(Typeface.DEFAULT);// 采用默认的宽度
        textPaint.setFakeBoldText(true);
        textPaint.setColor(watermarkColor);// 采用的颜色
        float tX = (src.getWidth() - getFontlength(textPaint, mark)) / 2;
        float tY = (src.getHeight() - getFontHeight(textPaint)) / 2
                + getFontLeading(textPaint);
        textPaint.setStrokeWidth(WATERMARK_STROKE_WIDTH);
        canvas.drawText(mark, tX, tY, textPaint);// 绘制上去 字，开始未知x,y采用那只笔绘制
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return src;
    }

    /**
     * @return 返回指定笔和指定字符串的长度
     */
    public static float getFontlength(Paint paint, String str) {
        if (null == str)
            str = "";

        return paint.measureText(str);
    }

    /**
     * @return 返回指定笔的文字高度
     */
    public static float getFontHeight(Paint paint) {
        FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }

	/**
	 * @return 返回指定笔离文字顶部的基准距离
	 */
	public static float getFontLeading(Paint paint) {
		FontMetrics fm = paint.getFontMetrics();
		return fm.leading - fm.ascent;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public float getSignSize() {
		return signSize;
	}

	public void setSignSize(float signSize) {
		this.signSize = signSize;
	}
	
	public int getWatermarkColor() {
		return watermarkColor;
	}

	public void setWatermarkColor(int watermarkColor) {
		this.watermarkColor = watermarkColor;
		
		watermarkBitmap(cachebBitmap, signature, signSize);
		invalidate();
	}

	public boolean isSign() {
		return isSign;
	}

	public void setSign(boolean isSign) {
		this.isSign = isSign;
	}
	
}
