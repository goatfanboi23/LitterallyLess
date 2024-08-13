package software.enginer.litterallyless.data;

import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.util.List;

import lombok.Data;

@Data
public class DetectionResult {
    private final List<ObjectDetectorResult> results;
    private final long inferenceTime;
    private final int inputImageWidth;
    private final int inputImageHeight;
    private final int inputImageRotation;
}
