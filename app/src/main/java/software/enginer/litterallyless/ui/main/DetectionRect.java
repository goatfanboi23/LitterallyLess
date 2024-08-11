package software.enginer.litterallyless.ui.main;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

public class DetectionRect extends RenderableRect {

    public DetectionRect(RectF bounds, int displayWidth, int displayHeight, int displayRotation, int scaleFactor) {
        super(bounds, displayWidth, displayHeight, displayRotation, scaleFactor);
        initPaints();
    }

    private void initPaints() {
        getTextBackgroundPaint().setColor(Color.BLACK);
        getTextBackgroundPaint().setStyle(Paint.Style.FILL);
        getTextBackgroundPaint().setTextSize(50f);

        getTextPaint().setColor(Color.WHITE);
        getTextPaint().setStyle(Paint.Style.FILL);
        getTextPaint().setTextSize(50f);

        getOutlinePaint().setColor(Color.rgb(0,127,139));
        getOutlinePaint().setStrokeWidth(8F);
        getOutlinePaint().setStyle(Paint.Style.STROKE);
    }

}
