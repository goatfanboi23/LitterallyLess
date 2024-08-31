package software.enginer.litterallyless.data;

import com.google.ar.core.Pose;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import software.enginer.litterallyless.util.Degradable;
import software.enginer.litterallyless.util.TimestampedCircularBuffer;
import software.enginer.litterallyless.util.utilities.MovingAverageFilter;
import software.enginer.litterallyless.util.utilities.PoseFilter;

@Data
public class Trackable {
    private final Degradable<Pose> currentPose;
    private final TimestampedCircularBuffer<Pose> poseBuffer;
    private final PoseFilter filter;
    private final AtomicInteger collectionsThresholdCounter = new AtomicInteger();
    private final AtomicBoolean collected = new AtomicBoolean(false);
}
