package software.enginer.litterallyless.data.repos;

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
import java.util.OptionalDouble;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import software.enginer.litterallyless.data.DetectionListener;
import software.enginer.litterallyless.data.DetectionResult;
import software.enginer.litterallyless.data.TrashModel;


public class DetectorRepository{
    private static final String assetPath = "model.tflite";

    private final TrashModel trashModel;
    private ImageProcessingOptions imageProcessingOptions;
    private final DetectionListener resultListener;
    private final AtomicInteger detectionViewWidth = new AtomicInteger(0);
    private final AtomicInteger detectionViewHeight = new AtomicInteger(0);
    private final ArrayBlockingQueue<Double> fpsQueue = new ArrayBlockingQueue<>(30);
    private final ReentrantReadWriteLock imageProcessingLock = new ReentrantReadWriteLock();


    public DetectorRepository(Context context, DetectionListener listener){
        this.resultListener = listener;
        BaseOptions.Builder baseBuilder = BaseOptions.builder();
        baseBuilder.setModelAssetPath(assetPath).setDelegate(Delegate.GPU);
        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseBuilder.build())
                .setScoreThreshold(0.5f)
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
        Bitmap bitmapBuffer = Bitmap.createBitmap(
                imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888
        );
        bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());
        imageProxy.close();
        if (imageProxy.getImageInfo().getRotationDegrees() != getImageRotation()) {
            updateRotation(imageProxy.getImageInfo().getRotationDegrees());
            return;
        }
        bitmapDetection(bitmapBuffer, frameTime);
    }

    private void bitmapDetection(Bitmap bitmapBuffer, long frameTime){
        MPImage mpImage = new BitmapImageBuilder(bitmapBuffer).build();
        imageProcessingLock.readLock().lock();
        trashModel.getDetector().detectAsync(mpImage, imageProcessingOptions, frameTime);
        imageProcessingLock.readLock().unlock();
    }

    public void returnResult(ObjectDetectorResult result, MPImage input) {
        long finishTimeMs = SystemClock.uptimeMillis();
        long inferenceTime = finishTimeMs - result.timestampMs();
        DetectionResult dr = new DetectionResult(List.of(result), inferenceTime, input.getWidth(), input.getHeight(), getImageRotation());
        resultListener.onResult(dr);
    }

    public void updateRotation(int rotation){
        imageProcessingLock.writeLock().lock();
        this.imageProcessingOptions = ImageProcessingOptions.builder().setRotationDegrees(rotation).build();
        imageProcessingLock.writeLock().unlock();
    }

    public void updateDetectionDim(int width, int height) {
        this.detectionViewWidth.set(width);
        this.detectionViewHeight.set(height);
    }

    public int getDetectionViewWidth() {
        return detectionViewWidth.get();
    }

    public int getDetectionViewHeight() {
        return detectionViewHeight.get();
    }

    public int getImageRotation() {
        imageProcessingLock.readLock().lock();
        int deg = imageProcessingOptions.rotationDegrees();
        imageProcessingLock.readLock().unlock();
        return deg;
    }

    public void addFpsQueue(Double d) {
       while(!fpsQueue.offer(d)){
           fpsQueue.poll();
       }
    }

    public double calcAverageFPS(){
        OptionalDouble average = fpsQueue.stream().mapToDouble(Double::doubleValue).average();
        return average.orElse(0);
    }
}
