package software.enginer.litterallyless.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.RenderNode;
import android.hardware.HardwareBuffer;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.exceptions.NotYetAvailableException;
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
import java.util.OptionalDouble;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import software.enginer.litterallyless.util.FallbackYuvToRgbConverter;
import software.enginer.litterallyless.util.PoseUtils;

public class ARDetectorRepository {
    private static final String assetPath = "model.tflite";

    private final TrashModel trashModel;
    private ImageProcessingOptions imageProcessingOptions;
    private final DetectionListener resultListener;
    private final AtomicInteger detectionViewWidth = new AtomicInteger(0);
    private final AtomicInteger detectionViewHeight = new AtomicInteger(0);
    private final ArrayBlockingQueue<Double> fpsQueue = new ArrayBlockingQueue<>(30);
    private final ReentrantReadWriteLock imageProcessingLock = new ReentrantReadWriteLock();
    private final List<Pose> anchors = new ArrayList<>();
    private final ReentrantLock anchorLock = new ReentrantLock();


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


    public void detectLivestreamFrame(Image image, int rotation) {
        long frameTime = SystemClock.uptimeMillis();
        Bitmap bitmap = FallbackYuvToRgbConverter.yuv420ToBitmap(image);
        if (rotation != getImageRotation()) {
            updateRotation(rotation);
            bitmap.recycle();
            return;
        }
        image.close();
        bitmapDetection(bitmap, frameTime);
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
