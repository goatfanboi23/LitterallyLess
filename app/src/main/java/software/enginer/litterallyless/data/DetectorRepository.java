package software.enginer.litterallyless.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;

import androidx.camera.core.ImageProxy;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.util.List;

public class DetectorRepository{
    private static final String assetPath = "model.tflite";

    private final TrashModel trashModel;

    private ImageProcessingOptions imageProcessingOptions;
    private final DetectionListener resultListener;
    private int detectionViewWidth = 0;
    private int detectionViewHeight = 0;

    public DetectorRepository(Context context, DetectionListener listener){
        this.resultListener = listener;
        BaseOptions.Builder baseBuilder = BaseOptions.builder();
        baseBuilder.setModelAssetPath(assetPath).setDelegate(Delegate.CPU);
        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseBuilder.build())
                .setScoreThreshold(0.2f)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener(this::returnResult)
                .setMaxResults(3).build();
        this.imageProcessingOptions = ImageProcessingOptions.builder()
                .setRotationDegrees(0)
                .build();
        trashModel = new TrashModel(ObjectDetector.createFromOptions(context, options));

    }

    public void detectLivestreamFrame(ImageProxy imageProxy) {
        long frameTime = SystemClock.uptimeMillis();

        // Copy out RGB bits from the frame to a bitmap buffer
        Bitmap bitmapBuffer = Bitmap.createBitmap(
                imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888
        );
        bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());
        imageProxy.close();

        // If the input image rotation is change, stop all detector
        if (imageProxy.getImageInfo().getRotationDegrees() != imageProcessingOptions.rotationDegrees()) {
            updateRotation(imageProxy.getImageInfo().getRotationDegrees());
            return;
        }

        // Convert the input Bitmap object to an MPImage object to run inference
        MPImage mpImage = new BitmapImageBuilder(bitmapBuffer).build();
        trashModel.getDetector().detectAsync(mpImage, imageProcessingOptions, frameTime);
    }

    public void returnResult(ObjectDetectorResult result, MPImage input) {
        long finishTimeMs = SystemClock.uptimeMillis();
        long inferenceTime = finishTimeMs - result.timestampMs();
        DetectionResult dr = new DetectionResult(List.of(result), inferenceTime, input.getWidth(), input.getHeight(), imageProcessingOptions.rotationDegrees());
        resultListener.onResult(dr);
    }

    public void updateRotation(int rotation){
        this.imageProcessingOptions = ImageProcessingOptions.builder().setRotationDegrees(rotation).build();
    }

    public void updateDetectionDim(int width, int height) {
        this.detectionViewWidth = width;
        this.detectionViewHeight = height;
    }

    public int getDetectionViewWidth() {
        return detectionViewWidth;
    }

    public int getDetectionViewHeight() {
        return detectionViewHeight;
    }

    public int getImageRotation() {
        return imageProcessingOptions.rotationDegrees();
    }
}
