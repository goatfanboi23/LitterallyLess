package software.enginer.litterallyless.logic;

import android.graphics.Bitmap;
import android.os.SystemClock;

import androidx.camera.core.ImageProxy;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Detector {
    private final ModelBuilder builder;
    private final AtomicReference<Consumer<DetectionResult>> subscriber = new AtomicReference<>((v)->{});
    private ObjectDetector detector;
    private int imageRotation = 0;
    ImageProcessingOptions imageProcessingOptions;

    public Detector(ModelBuilder builder) {
        this.builder = builder;
    }
    public void setupObjectDetector(){
        detector = ObjectDetector.createFromOptions(builder.getContext(), builder.build());
        imageProcessingOptions = ImageProcessingOptions.builder()
                .setRotationDegrees(imageRotation)
                .build();
    }
    public void resetObjectDetector(){
        builder.resetOptions();
    }

    public void detectLivestreamFrame(ImageProxy imageProxy) {
        if (builder.getRunningMode() != RunningMode.LIVE_STREAM) {
            throw new IllegalArgumentException(
                    "Attempting to call detectLivestreamFrame" + " while not using RunningMode.LIVE_STREAM"
            );
        }

        long frameTime = SystemClock.uptimeMillis();

        // Copy out RGB bits from the frame to a bitmap buffer
        Bitmap bitmapBuffer = Bitmap.createBitmap(
                imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888
        );
        bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());
        imageProxy.close();

        // If the input image rotation is change, stop all detector
        if (imageProxy.getImageInfo().getRotationDegrees() != imageRotation) {
            imageRotation = imageProxy.getImageInfo().getRotationDegrees();
            resetObjectDetector();
            setupObjectDetector();
        }

        // Convert the input Bitmap object to an MPImage object to run inference
        MPImage mpImage = new BitmapImageBuilder(bitmapBuffer).build();

        detectAsync(mpImage, frameTime);
    }

    // Run object detection using MediaPipe Object Detector API
    void detectAsync(MPImage mpImage, long frameTime) {
        // As we're using running mode LIVE_STREAM, the detection result will be returned in
        // returnLivestreamResult function
        detector.detectAsync(mpImage, imageProcessingOptions, frameTime);
    }

    // Return the detection result to this ObjectDetectorHelper's caller
    private void returnLivestreamResult(ObjectDetectorResult result, MPImage inputImage) {
        long finishTimeMs = SystemClock.uptimeMillis();
        long inferenceTime = finishTimeMs - result.timestampMs();
        DetectionResult dr = new DetectionResult(List.of(result), inferenceTime, inputImage.getWidth(), inputImage.getHeight(), imageRotation);
        subscriber.get().accept(dr);
    }
    public void setSubscriber(Consumer<DetectionResult> subscriber){
        this.subscriber.set(subscriber);
    }
}
