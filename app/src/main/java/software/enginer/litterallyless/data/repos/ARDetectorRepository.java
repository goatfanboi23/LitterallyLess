package software.enginer.litterallyless.data.repos;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.SystemClock;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

import com.google.ar.core.Pose;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;

import software.enginer.litterallyless.data.AnchorProximityResult;
import software.enginer.litterallyless.data.DetectionListener;
import software.enginer.litterallyless.data.DetectionResult;
import software.enginer.litterallyless.data.Trackable;
import software.enginer.litterallyless.data.TrackableAnchorManager;
import software.enginer.litterallyless.data.TrashModel;
import software.enginer.litterallyless.util.CircularArrayBuffer;
import software.enginer.litterallyless.util.Degradable;
import software.enginer.litterallyless.util.convertors.FallbackYuvToRgbConverter;
import software.enginer.litterallyless.util.convertors.YuvConverter;

public class ARDetectorRepository {
    private static final String assetPath = "model.tflite";

    private final TrashModel trashModel;
    private ImageProcessingOptions imageProcessingOptions;
    private final DetectionListener resultListener;
    private final CircularArrayBuffer<Double> detectionFpsMonitor = new CircularArrayBuffer<>();
    private final CircularArrayBuffer<Double> conversionFpsMonitor = new CircularArrayBuffer<>();
    private final ReentrantReadWriteLock imageProcessingLock = new ReentrantReadWriteLock();
    private static final FallbackYuvToRgbConverter fallbackConverter = new FallbackYuvToRgbConverter();
    private final TrackableAnchorManager anchorManager = new TrackableAnchorManager();


    public ARDetectorRepository(Context context, DetectionListener listener) {
        this.resultListener = listener;
        // TODO: decouple preferences with repo (this is just for testing)
        String delete = PreferenceManager.getDefaultSharedPreferences(context).getString("delegate","cpu");
        Delegate delegate;
        try{
            delegate = Delegate.valueOf(delete.toUpperCase());
        }catch (IllegalArgumentException e){
            delegate = Delegate.CPU;
        }
        BaseOptions.Builder baseBuilder = BaseOptions.builder();
        baseBuilder.setModelAssetPath(assetPath).setDelegate(delegate);
        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseBuilder.build())
                .setScoreThreshold(0.5f)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener(this::returnResult)
                .setMaxResults(4).build();
        this.imageProcessingOptions = ImageProcessingOptions.builder()
                .setRotationDegrees(0)
                .build();
        trashModel = new TrashModel(ObjectDetector.createFromOptions(context, options));

    }


    public boolean detectLivestreamFrame(Image image, int rotation, @Nullable YuvConverter converter) {
        long frameTime = SystemClock.uptimeMillis();
        Bitmap bitmap;
        if (converter == null) {
            bitmap = fallbackConverter.yuv2rgb(image);
        } else {
            bitmap = converter.yuv2rgb(image);
        }
        long deltaDetectionTime = (SystemClock.uptimeMillis() - frameTime);
        getDetectionFpsMonitor().add((double) deltaDetectionTime);
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

    private void bitmapDetection(Bitmap bitmapBuffer, long frameTime) {
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

    public void updateRotation(int rotation) {
        imageProcessingLock.writeLock().lock();
        this.imageProcessingOptions = ImageProcessingOptions.builder().setRotationDegrees(rotation).build();
        imageProcessingLock.writeLock().unlock();
    }

    public int getImageRotation() {
        imageProcessingLock.readLock().lock();
        int deg = imageProcessingOptions.rotationDegrees();
        imageProcessingLock.readLock().unlock();
        return deg;
    }

    public CircularArrayBuffer<Double> getDetectionFpsMonitor() {
        return detectionFpsMonitor;
    }

    public CircularArrayBuffer<Double> getConversionFpsMonitor() {
        return conversionFpsMonitor;
    }

    public TrackableAnchorManager getAnchorManager() {
        return anchorManager;
    }
}
