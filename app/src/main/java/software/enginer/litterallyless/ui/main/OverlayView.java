package software.enginer.litterallyless.ui.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.mediapipe.tasks.components.containers.Category;
import com.google.mediapipe.tasks.components.containers.Detection;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.text.DecimalFormat;

import software.enginer.litterallyless.logic.DetectionResult;

public class OverlayView extends View {

    private DetectionResult detectionResult;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        if (detectionResult == null || detectionResult.getResults().isEmpty()){
            return;
        }
        ObjectDetectorResult objectDetectorResult = detectionResult.getResults().get(0);
        objectDetectorResult.detections().forEach(result -> {
            DetectionRect rect = rectFromDetection(result,
                    detectionResult.getInputImageWidth(),
                    detectionResult.getInputImageHeight(),
                    detectionResult.getInputImageRotation());
            Matrix matrix = new Matrix();
            matrix.postTranslate(-rect.getDisplayWidth()/2f, -rect.getDisplayHeight()/2f);
            matrix.postRotate(rect.getDisplayRotation());
            if (rect.getDisplayRotation() == 90 || rect.getDisplayRotation() == 270) {
                matrix.postTranslate(rect.getDisplayHeight() / 2f, rect.getDisplayWidth() / 2f);
            } else {
                matrix.postTranslate(rect.getDisplayWidth() / 2f, rect.getDisplayHeight() / 2f);
            }
            RectF bounds = rect.getBounds();
            matrix.mapRect(bounds);
            float top = bounds.top * rect.getScaleFactor();
            float bottom = bounds.bottom * rect.getScaleFactor();
            float left = bounds.left * rect.getScaleFactor();
            float right = bounds.right * rect.getScaleFactor();
            RectF drawableRect = new RectF(left, top, right, bottom);

            canvas.drawRect(drawableRect, rect.getOutlinePaint());

            Category category = result.categories().get(0);
            String drawableText = category.categoryName() + " " + df.format(category.score());
            rect.getTextBackgroundPaint().getTextBounds(
                    drawableText,
                    0,
                    drawableText.length(),
                    new Rect((int) (bounds.left),(int) (bounds.top),(int) (bounds.right),(int) (bounds.bottom))
            );

            float textWidth = bounds.width();
            float textHeight = bounds.height();
            canvas.drawRect(
                    left,
                    top,
                    left + textWidth + 8,
                    top + textHeight + 8,
                    rect.getTextBackgroundPaint()
            );

            canvas.drawText(
                    drawableText,
                    left,
                    top + bounds.height(),
                    rect.getTextPaint()
            );

        });
    }
    public void setDetectionResult(DetectionResult detectionResult){
        this.detectionResult = detectionResult;
        invalidate();


    }
    private DetectionRect rectFromDetection(Detection detection, int imageWidth, int imageHeight, int rotation){
        int outputWidth = imageWidth;
        int outputHeight = imageHeight;
        if (rotation == 90 || rotation == 270){
            outputWidth = imageHeight;
            outputHeight = imageWidth;
        }
        int scaleFactor = Math.max(getWidth() / outputWidth, getHeight() / outputHeight);
        return new DetectionRect(
                detection.boundingBox(),
                outputWidth,
                outputHeight,
                rotation,
                scaleFactor
        );
    }



}
