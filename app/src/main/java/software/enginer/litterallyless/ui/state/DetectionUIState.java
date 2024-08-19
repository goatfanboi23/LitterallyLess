package software.enginer.litterallyless.ui.state;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class DetectionUIState {
    private final List<DrawableDetection> drawableDetectionList;
    private final String inferenceLabel;

    public DetectionUIState() {
        drawableDetectionList = new ArrayList<>();
        inferenceLabel = "";
    }

}
