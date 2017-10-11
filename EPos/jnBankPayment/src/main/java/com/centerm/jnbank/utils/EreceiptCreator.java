package com.centerm.jnbank.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextPaint;
import android.text.TextUtils;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 电子签购单构建器。宽度固定
 * author:wanliang527</br>
 * date:2016/12/8</br>
 */

public class EreceiptCreator {

    private Logger logger = Logger.getLogger(getClass());

    private final static int FONT_SIZE_SMALL = 13;
    private final static int FONT_SIZE_MEDIUM = 20;
    private final static int FONT_SIZE_LARGE = 27;

    private final static int VERTICAL_OFFSET = 10;//上下边距
    private final static int HORIZONTAL_OFFSET = 10;//左右边距
    private final static int MIN_WIDTH = 17 * FONT_SIZE_MEDIUM + 2 * HORIZONTAL_OFFSET;//中号字体大概可以容纳17个字，这个是固定的宽度，高度自适应


    private List<ReceiptElement> elementList;
    private String directory;
    private String batchNo;
    private String serialNo;

    private int width = MIN_WIDTH;
    private int picMaxHeght = width / 3 * 2;//最大高度不超过宽的2/3
    private int height;

    private Paint paint;


    private EreceiptCreator() {
        elementList = new ArrayList<>();
        logger.debug("电子签购单生成器==>创建实例");
    }

    public static EreceiptCreator newInstance(String directory, String batchNo, String serialNo) {
        EreceiptCreator instance = new EreceiptCreator();
        instance.directory = directory;
        instance.batchNo = batchNo;
        instance.serialNo = serialNo;
        instance.paint = new Paint();
        instance.paint.setColor(Color.BLACK);
        instance.paint.setTypeface(Typeface.DEFAULT);
        return instance;
    }

    public EreceiptCreator addElement(ReceiptElement ele) {
        if (ele == null) {
            return this;
        }
        elementList.add(ele);
        if (ele instanceof TextItem) {
            TextItem item = (TextItem) ele;
            height += item.getSize().getValue();
            float itemWidth = initPaint(item).measureText(item.getContent());
            if (itemWidth > width - 2 * HORIZONTAL_OFFSET) {
                width = (int) itemWidth + 2 * HORIZONTAL_OFFSET;
            }
        } else if (ele instanceof PictureItem) {
            PictureItem item = (PictureItem) ele;
            Bitmap b = decodeFromFile(item.getFilePath());
            if (b != null) {
                height += adjustBitmapSize(null, item)[1];
                b.recycle();
            }
        }
        return this;
    }

    private Paint initPaint(TextItem item) {
        paint.setTextAlign(item.getAlign());
        paint.setTextSize(item.getSize().getValue());
        return paint;
    }

    public EreceiptCreator addElement(Context context, ReceiptElement ele) {
        if (ele == null) {
            return this;
        }
        elementList.add(ele);
        if (ele instanceof TextItem) {
            TextItem item = (TextItem) ele;
            height += item.getSize().getValue();
            float itemWidth = initPaint(item).measureText(item.getContent());
            if (itemWidth > width) {
                width = (int) itemWidth;
            }
        } else if (ele instanceof PictureItem) {
            PictureItem item = (PictureItem) ele;
            Bitmap b = decodeFromDrawable(context, item.getResId());
            if (b != null) {
                height += adjustBitmapSize(context, item)[1];
                b.recycle();
            }
        }
        return this;
    }

    private Bitmap decodeFromFile(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }

    private Bitmap decodeFromDrawable(Context context, int resId) {
        try {
            BitmapDrawable code = (BitmapDrawable) context.getResources().getDrawable(resId);
            Bitmap codeBitmap = code.getBitmap();
            return codeBitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public EreceiptCreator addAll(List<ReceiptElement> list) {
        if (list == null) {
            return this;
        }
        for (int i = 0; i < list.size(); i++) {
            addElement(list.get(i));
        }
        return this;
    }

    public String create(Context context) {
        logger.debug("签购单生成器==>开始生成图片");
        height += VERTICAL_OFFSET * 2;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        TextPaint paint = new TextPaint();
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.DEFAULT);
//        paint.setAntiAlias(true);
        int drawedHeight = VERTICAL_OFFSET;
        for (int i = 0; i < elementList.size(); i++) {
            ReceiptElement ele = elementList.get(i);
            if (ele instanceof TextItem) {
                TextItem item = (TextItem) ele;
                drawedHeight += item.getSize().getValue();
                paint.setTextSize(item.getSize().getValue());
                Paint.Align align = item.getAlign();
                paint.setTextAlign(align);
                switch (align) {
                    case LEFT:
                        canvas.drawText(item.getContent(), HORIZONTAL_OFFSET, drawedHeight, paint);
                        break;
                    case CENTER:
                        canvas.drawText(item.getContent(), width / 2, drawedHeight, paint);
                        break;
                    case RIGHT:
                        canvas.drawText(item.getContent(), width - HORIZONTAL_OFFSET, drawedHeight, paint);
                        break;
                }
            } else if (ele instanceof PictureItem) {
                PictureItem item = (PictureItem) ele;
                Bitmap b = decodeFromFile(item.getFilePath());
                logger.warn(item.toString());
                if (b == null) {
                    b = decodeFromDrawable(context, item.getResId());
                }
                if (b != null) {
                    Rect src = new Rect(0, 0, b.getWidth(), b.getHeight());
                    int[] size = adjustBitmapSize(context, item);
                    Rect dst = new Rect((width - size[0]) / 2, drawedHeight, (width - size[0]) / 2 + size[0], drawedHeight + size[1]);
                    if (!b.isRecycled()) {
                        canvas.drawBitmap(b, src, dst, null);
                        b.recycle();
                        drawedHeight += size[1];
                    }
                }
            }
        }
        if (saveToFile(bitmap)) {
            return getFileName();
        }
        return null;
    }

    private void drawTextAutoWrap(Canvas canvas, Paint paint, float y, String content) {
        Paint.Align align = paint.getTextAlign();
        if (align == null) {
            align = Paint.Align.LEFT;
        }
        float measureWidth = paint.measureText(content);
        if (measureWidth < width - 2 * HORIZONTAL_OFFSET) {
            switch (align) {
                case LEFT:
                    canvas.drawText(content, HORIZONTAL_OFFSET, y, paint);
                    break;
                case CENTER:
                    canvas.drawText(content, width / 2, y, paint);
                    break;
                case RIGHT:
                    canvas.drawText(content, width - HORIZONTAL_OFFSET, y, paint);
                    break;
            }
        } else {
            int index = findWrapIndex(paint, content);
            if (index != content.length() - 1) {
                drawTextAutoWrap(canvas, paint, y, content.substring(0, index));
                drawTextAutoWrap(canvas, paint, y, content.substring(index, content.length()));
            } else {
                switch (align) {
                    case LEFT:
                        canvas.drawText(content, HORIZONTAL_OFFSET, y, paint);
                        break;
                    case CENTER:
                        canvas.drawText(content, width / 2, y, paint);
                        break;
                    case RIGHT:
                        canvas.drawText(content, width - HORIZONTAL_OFFSET, y, paint);
                        break;
                }
            }

        }
    }

    private int findWrapIndex(Paint paint, String content) {
        int len = content.length();
        int maxWidth = width - 2 * HORIZONTAL_OFFSET;
        for (int i = 0; i < len; i++) {
            if (i != len - 1) {
                if (paint.measureText(content.substring(0, i)) <= maxWidth
                        && paint.measureText(content.substring(0, i + 1)) > maxWidth) {
                    return i;
                }
            } else {
                return len - 1;
            }
        }
        return 0;
    }


    private boolean saveToFile(Bitmap bitmap) {
        File file = new File(getFileName());
        if (file.exists()) {
            logger.warn(file.getAbsoluteFile() + "==>文件已存在==>删除");
            file.delete();
        }
        try {
            FileOutputStream fps = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fps);
            fps.flush();
            fps.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String getFileName() {
        File file = new File(directory);
        if (!file.exists()) {
            FileUtils.createDirectory(directory);
        }
        return directory + File.separator + batchNo + "_" + serialNo + ".png";
    }

    private int[] adjustBitmapSize(Context context, PictureItem item) {
        Bitmap bitmap = decodeFromFile(item.getFilePath());
        boolean isFromDrawable = false;
        if (bitmap == null && item.getResId() > 0 && context != null) {
            bitmap = decodeFromDrawable(context, item.getResId());
            isFromDrawable = true;
        }
        int[] size = new int[2];
        if (bitmap != null) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            if (item.getWidth() > 0) {
                //如果外部有指定宽度的话
                size[0] = item.getWidth();
            } else {
                size[0] = w;
            }
            if (size[0] > 300) {
                //最大宽度不超过凭条宽度
                size[0] = 300;
            }
            //计算高度，最大不超过凭条宽度的三分之二
            if (size[0] / w > 1) {
                //被放大了，高度也对应放大
//                logger.warn("放大比例："+(size[0] / (double) w));
                size[1] = (int) (h * (size[0] / (double) w));
            } else {
                //被缩小了，高度也对应缩小
//                logger.warn("缩小比例："+ (w / (double) size[0]));
                size[1] = (int) (h / (w / (double) size[0]));
            }
            if (size[1] > picMaxHeght) {
                size[1] = picMaxHeght;
            }
            if (!isFromDrawable) {
                bitmap.recycle();
            }
//            logger.warn("最大高度："+picMaxHeght);
//            logger.warn("图片原尺寸：" + w + " " + h);
//            logger.warn("调整后尺寸：" + size[0] + "  " + size[1]);
        }
        return size;
    }

    public static class PictureItem implements ReceiptElement {
        private String filePath;
        private int width;
        private int height;
        private int resId;

        public PictureItem(int resId) {
            this.resId = resId;
        }

        public PictureItem(String filePath) {
            this.filePath = filePath;
        }

        public PictureItem(String filePath, int width) {
            this.filePath = filePath;
            this.width = width;
        }

        public PictureItem(String filePath, int width, int height) {
            this.filePath = filePath;
            this.width = width;
            this.height = height;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getResId() {
            return resId;
        }

        public void setResId(int resId) {
            this.resId = resId;
        }

        @Override
        public String toString() {
            return "PictureItem{" +
                    "filePath='" + filePath + '\'' +
                    ", width=" + width +
                    ", height=" + height +
                    ", resId=" + resId +
                    '}';
        }
    }


    public static class TextItem implements ReceiptElement {
        private String content = "";
        private FontSize size = FontSize.MEDIUM;
        private Paint.Align align = Paint.Align.LEFT;

        public TextItem(String content, FontSize size, Paint.Align align) {
            this.content = content;
            this.size = size;
            this.align = align;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            if (TextUtils.isEmpty(content)) {
                this.content = "";
            } else {
                this.content = content;
            }
        }

        public FontSize getSize() {
            return size;
        }

        public void setSize(FontSize size) {
            if (size == null) {
                this.size = FontSize.MEDIUM;
            } else {
                this.size = size;
            }
        }

        public Paint.Align getAlign() {
            return align;
        }

        public void setAlign(Paint.Align align) {
            if (align == null) {
                this.align = Paint.Align.LEFT;
            } else {
                this.align = align;
            }
        }

        @Override
        public String toString() {
            return "TextItem{" +
                    "content='" + content + '\'' +
                    ", size=" + size +
                    ", align=" + align +
                    '}';
        }
    }

    public interface ReceiptElement {
    }


    /**
     * 字号大小
     */
    public enum FontSize {
        SMALL(FONT_SIZE_SMALL),//小
        MEDIUM(FONT_SIZE_MEDIUM),//中
        LARGE(FONT_SIZE_LARGE);//大

        private int value;

        FontSize(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
