package software.enginer.litterallyless.logic;

import android.content.Context;

import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.core.OutputHandler;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.util.function.Function;

public class ModelBuilder {
    private static final int defMaxResults = 1;
    private static final float defDetectionThreshold = 0.50f;
    private static final Delegate defRenderMode = Delegate.CPU;
    private static final RunningMode defRunningMode = RunningMode.LIVE_STREAM;


    private final Context context;
    private BaseOptions.Builder baseBuilder = BaseOptions.builder();
    private int maxResults = defMaxResults;
    private float detectionThreshold = defDetectionThreshold;
    private Delegate renderMode = defRenderMode;
    private RunningMode runningMode = defRunningMode;
    private OutputHandler.ResultListener<ObjectDetectorResult, MPImage> resultListener = (r,v)->{};

    public ModelBuilder(Context context, String assetPath) {
        this.context = context;
    }

    public ObjectDetector.ObjectDetectorOptions build(){
        return ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseBuilder.build())
                .setScoreThreshold(detectionThreshold)
                .setRunningMode(runningMode)
                .setResultListener(resultListener)
                .setMaxResults(maxResults).build();
    }

    public void resetOptions(){
        setMaxResults(defMaxResults);
        setDetectionThreshold(defDetectionThreshold);
        setRenderMode(defRenderMode);
        setRunningMode(defRunningMode);
    }

    public Delegate getRenderMode() {
        return renderMode;
    }

    public void setRenderMode(Delegate renderMode) {
        this.renderMode = renderMode;
    }

    public float getDetectionThreshold() {
        return detectionThreshold;
    }

    public void setDetectionThreshold(float detectionThreshold) {
        this.detectionThreshold = detectionThreshold;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public RunningMode getRunningMode() {
        return runningMode;
    }

    public void setRunningMode(RunningMode runningMode) {
        this.runningMode = runningMode;
    }

    public Context getContext() {
        return context;
    }

    public void setResultCallback(OutputHandler.ResultListener<ObjectDetectorResult, MPImage> resultListener) {
        this.resultListener = resultListener;
    }

    public OutputHandler.ResultListener<ObjectDetectorResult, MPImage> getResultListener() {
        return resultListener;
    }
}
