package software.enginer.litterallyless.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import software.enginer.litterallyless.ui.state.DrawableDetection;

public class DetectionView extends View {

    private List<DrawableDetection> drawableDetectionList = new ArrayList<>();

    public DetectionView(Context context) {
        super(context);
    }

    public DetectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawableDetectionList.forEach(detection -> {
            canvas.drawRect(detection.getBoundingBox(), detection.getBoundBoxOutline());
            canvas.drawRect(
                    detection.getTextBBoxLeft(),
                    detection.getTextBBoxTop(),
                    detection.getTextBBoxRight(),
                    detection.getTextBBoxBottom(),
                    detection.getTextBackgroundPaint()
            );
            canvas.drawText(
                    detection.getDetectionInfo(),
                    detection.getTextX(),
                    detection.getTextY(),
                    detection.getTextPaint()
            );
        });
    }

    public void setContent(List<DrawableDetection> drawableDetectionList) {
        this.drawableDetectionList = drawableDetectionList;
        invalidate();
    }
}
