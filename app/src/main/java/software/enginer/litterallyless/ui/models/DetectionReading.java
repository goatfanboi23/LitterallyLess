package software.enginer.litterallyless.ui.models;

import com.google.ar.core.Pose;
import com.google.mediapipe.tasks.components.containers.Category;

import lombok.Data;

@Data
public class DetectionReading {
    private final Pose pose;
    private final Category category;
}
