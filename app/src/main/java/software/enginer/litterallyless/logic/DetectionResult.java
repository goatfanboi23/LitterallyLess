package software.enginer.litterallyless.logic;

import androidx.annotation.NonNull;

import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectionResult;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DetectionResult {
    private final List<ObjectDetectorResult> results;
    private final long inferenceTime;
    private final int inputImageWidth;
    private final int inputImageHeight;
    private final int inputImageRotation;

    public DetectionResult(List<ObjectDetectorResult> results, long inferenceTime, int inputImageWidth, int inputImageHeight, int inputImageRotation) {
        this.results = results;
        this.inferenceTime = inferenceTime;
        this.inputImageWidth = inputImageWidth;
        this.inputImageHeight = inputImageHeight;
        this.inputImageRotation = inputImageRotation;
    }

    public List<ObjectDetectorResult> getResults() {
        return results;
    }

    public long getInferenceTime() {
        return inferenceTime;
    }

    public int getInputImageWidth() {
        return inputImageWidth;
    }

    public int getInputImageHeight() {
        return inputImageHeight;
    }

    public int getInputImageRotation() {
        return inputImageRotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetectionResult that = (DetectionResult) o;
        return inferenceTime == that.inferenceTime && inputImageWidth == that.inputImageWidth && inputImageHeight == that.inputImageHeight && inputImageRotation == that.inputImageRotation && Objects.equals(results, that.results);
    }

    @Override
    public int hashCode() {
        return Objects.hash(results, inferenceTime, inputImageWidth, inputImageHeight, inputImageRotation);
    }

    @NonNull
    @Override
    public String toString() {
        return "DetectionResult{" +
                "results=" + results +
                ", inferenceTime=" + inferenceTime +
                ", inputImageWidth=" + inputImageWidth +
                ", inputImageHeight=" + inputImageHeight +
                ", inputImageRotation=" + inputImageRotation +
                '}';
    }
}
