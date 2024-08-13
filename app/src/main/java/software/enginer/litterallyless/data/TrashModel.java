package software.enginer.litterallyless.data;

import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector;

import lombok.Data;

@Data
public class TrashModel {
    private final ObjectDetector detector;
}
