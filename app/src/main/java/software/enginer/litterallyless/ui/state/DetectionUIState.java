package software.enginer.litterallyless.ui.state;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DetectionUIState {
    private final List<DrawableDetection> drawableDetectionList;

    public DetectionUIState(List<DrawableDetection> drawableDetectionList) {
        this.drawableDetectionList = drawableDetectionList;
    }

    public DetectionUIState() {
        drawableDetectionList = new ArrayList<>();
    }
}
