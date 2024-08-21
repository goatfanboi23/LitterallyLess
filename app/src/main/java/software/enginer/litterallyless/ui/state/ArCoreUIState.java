package software.enginer.litterallyless.ui.state;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import software.enginer.litterallyless.ui.LabeledAnchor;

@Data
@AllArgsConstructor
public class ArCoreUIState {
    private final List<LabeledAnchor> labeledAnchorList;
    private final String inferenceLabel;

    public ArCoreUIState() {
        labeledAnchorList = new ArrayList<>();
        inferenceLabel = "";
    }
}
