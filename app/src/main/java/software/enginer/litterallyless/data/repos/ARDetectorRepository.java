package software.enginer.litterallyless.data.repos;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.SystemClock;
import android.util.Log;

import com.google.ar.core.Pose;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;

import software.enginer.litterallyless.data.AnchorProximityResult;
import software.enginer.litterallyless.data.DetectionListener;
import software.enginer.litterallyless.data.DetectionResult;
import software.enginer.litterallyless.data.TrashModel;
import software.enginer.litterallyless.util.CircularBuffer;
import software.enginer.litterallyless.util.convertors.FallbackYuvToRgbConverter;
import software.enginer.litterallyless.util.utilities.PoseUtils;
import software.enginer.litterallyless.util.convertors.YuvConverter;

public class ARDetectorRepository {
    private static final String assetPath = "model.tflite";

    private final TrashModel trashModel;
    private ImageProcessingOptions imageProcessingOptions;
    private final DetectionListener resultListener;
    private final AtomicInteger detectionViewWidth = new AtomicInteger(0);
    private final AtomicInteger detectionViewHeight = new AtomicInteger(0);
    private final CircularBuffer<Double> detectionFpsMonitor = new CircularBuffer<>();
    private final CircularBuffer<Double> conversionFpsMonitor = new CircularBuffer<>();
    private final ReentrantReadWriteLock imageProcessingLock = new ReentrantReadWriteLock();
    private final List<Pose> anchors = new ArrayList<>();
    private final ReentrantLock anchorLock = new ReentrantLock();
    private static final FallbackYuvToRgbConverter fallbackConverter = new FallbackYuvToRgbConverter();


    public ARDetectorRepository(Context context, DetectionListener listener){
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


    public boolean detectLivestreamFrame(Image image, int rotation, @Nullable YuvConverter converter) {
        long frameTime = SystemClock.uptimeMillis();
        Bitmap bitmap;
        if (converter == null){
            bitmap = fallbackConverter.yuv2rgb(image);
        }else{
            bitmap = converter.yuv2rgb(image);
        }
        long deltaDetectionTime = (SystemClock.uptimeMillis() - frameTime);
        getDetectionFpsMonitor().addQueue((double) deltaDetectionTime);
        double avgFPS = getDetectionFpsMonitor().averageDouble(Double::doubleValue);
        Log.i(ARDetectorRepository.class.getSimpleName(), "CONVERSION TIME: " + avgFPS);
        if (rotation != getImageRotation()) {
            updateRotation(rotation);
            bitmap.recycle();
            image.close();
            return false;
        }
        image.close();
        bitmapDetection(bitmap, frameTime);
        return true;
    }

    private void bitmapDetection(Bitmap bitmapBuffer, long frameTime){
        MPImage mpImage = new BitmapImageBuilder(bitmapBuffer).build();
        imageProcessingLock.readLock().lock();
        ImageProcessingOptions build = ImageProcessingOptions.builder().setRotationDegrees(imageProcessingOptions.rotationDegrees()).build();
        imageProcessingLock.readLock().unlock();
        trashModel.getDetector().detectAsync(mpImage, build, frameTime);
    }

    public void returnResult(ObjectDetectorResult result, MPImage input) {
        long finishTimeMs = SystemClock.uptimeMillis();
        long inferenceTime = finishTimeMs - result.timestampMs();
        DetectionResult dr = new DetectionResult(List.of(result), inferenceTime, input.getWidth(), input.getHeight(), getImageRotation());
        resultListener.onResult(dr);
    }

    public void updateRotation(int rotation){
        try {
            boolean success = imageProcessingLock.writeLock().tryLock(1, TimeUnit.SECONDS);
            if (!success){
                throw new IllegalArgumentException("COULD NOT SECURE LOCK");
            }else{
                this.imageProcessingOptions = ImageProcessingOptions.builder().setRotationDegrees(rotation).build();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            imageProcessingLock.writeLock().unlock();
        }
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

    public CircularBuffer<Double> getDetectionFpsMonitor(){
        return detectionFpsMonitor;
    }

    public CircularBuffer<Double> getConversionFpsMonitor() {
        return conversionFpsMonitor;
    }

    public void addAnchor(Pose anchorPose){
        anchorLock.lock();
        anchors.add(anchorPose);
        anchorLock.unlock();
    }

    public AnchorProximityResult getClosestAnchor(Pose pose){
        anchorLock.lock();
        if (anchors.isEmpty()){
            anchorLock.unlock();
            return new AnchorProximityResult(-1,null);
        }
        Pose closestAnchor = anchors.get(0);
        float closestProx = (float) PoseUtils.distance(closestAnchor, pose);
        for (int i = 1; i < anchors.size(); i++) {
            Pose anchor = anchors.get(0);
            float distance = (float) PoseUtils.distance(anchor, pose);
            if (distance < closestProx){
                closestProx = distance;
                closestAnchor = anchor;
            }
        }
        anchorLock.unlock();
        return new AnchorProximityResult(closestProx, closestAnchor);
    }
}