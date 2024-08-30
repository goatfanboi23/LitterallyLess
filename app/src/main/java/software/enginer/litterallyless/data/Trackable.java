package software.enginer.litterallyless.data;

import com.google.ar.core.Pose;

import java.util.List;
import java.util.Queue;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import software.enginer.litterallyless.util.Degradable;
import software.enginer.litterallyless.util.TimeStamped;
import software.enginer.litterallyless.util.TimestampedCircularBuffer;
import software.enginer.litterallyless.util.utilities.DetectionFilter;

@Data
public class Trackable {
    private final Degradable<Pose> currentPose;
    private final TimestampedCircularBuffer<Pose> poseBuffer;
    private final DetectionFilter filter;
}
