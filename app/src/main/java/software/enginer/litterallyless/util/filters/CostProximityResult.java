package software.enginer.litterallyless.util.filters;

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import software.enginer.litterallyless.data.AnchorProximityResult;

@Getter
@EqualsAndHashCode
@ToString
public class CostProximityResult {
    private final List<AnchorProximityResult> proximityResult;
    private final int[] costs;
    private int index = 0;
    private int minCostIndex = -1;

    public CostProximityResult(int numberOfTracks) {
        this.costs = new int[numberOfTracks];
        this.proximityResult = new ArrayList<>();
    }

    public void add(AnchorProximityResult anchorProximityResult) {
        proximityResult.add(anchorProximityResult);
        costs[index] = anchorProximityResult.getProximity().intValue();
        if (minCostIndex == -1 || costs[index] <= costs[minCostIndex]){
            minCostIndex = index;
        }
        index++;
    }
}
