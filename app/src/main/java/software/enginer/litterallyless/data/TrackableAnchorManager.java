package software.enginer.litterallyless.data;

import android.util.Log;

import com.google.ar.core.Pose;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import software.enginer.litterallyless.util.CircularArrayBuffer;
import software.enginer.litterallyless.util.Degradable;
import software.enginer.litterallyless.util.TimestampedCircularBuffer;
import software.enginer.litterallyless.util.utilities.PoseUtils;

public class TrackableAnchorManager {

    private final ConcurrentHashMap<Degradable<Pose>, TimestampedCircularBuffer<Pose>> anchorStateMap = new ConcurrentHashMap<>();

    public void degradeAnchors() {
        Iterator<Map.Entry<Degradable<Pose>, TimestampedCircularBuffer<Pose>>> iterator = anchorStateMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Degradable<Pose>, TimestampedCircularBuffer<Pose>> entry = iterator.next();
            int ticks = entry.getKey().incrementAndGetTicks();
            if (ticks >= 20) {
                iterator.remove();
            }
        }
    }
    public List<Double> getVelocities(){
        return anchorStateMap.keySet()
                .stream()
                .map(this::getVelocity)
                .filter(d -> !d.isNaN())
                .collect(Collectors.toList());
    }

    @NotNull
    public Degradable<Pose> moveToPoses(@NotNull Degradable<Pose> oldPose, @NotNull Pose newPose){
        Degradable<Pose> degradable = new Degradable<>(newPose);
        TimestampedCircularBuffer<Pose> timeBuffer = anchorStateMap.remove(oldPose);
        assert timeBuffer != null;
        timeBuffer.addStamped(oldPose.getValue());
        anchorStateMap.put(degradable, timeBuffer);
        return degradable;
    }

    public void addAnchor(@NotNull Degradable<Pose> poseDegradable) {
        anchorStateMap.put(poseDegradable, new TimestampedCircularBuffer<>(10));
    }

    @Nullable
    public AnchorProximityResult getClosestAnchor(@NotNull Pose pose) {
        Degradable<Pose> closestAnchor = null;
        float closestProx = Float.MAX_VALUE;
        for (Degradable<Pose> anchor : anchorStateMap.keySet()) {
            float distance = (float) PoseUtils.distance(anchor.getValue(), pose);
            if (distance < closestProx) {
                closestProx = distance;
                closestAnchor = anchor;
            }
        }
        if (closestProx < 0.075) {
            Log.i(TrackableAnchorManager.class.getSimpleName(), "UPDATING BUFFER!");
            closestAnchor = moveToPoses(closestAnchor, pose);
        }
        return new AnchorProximityResult(closestProx, closestAnchor);
    }

    public int getAnchorCount() {
        return anchorStateMap.size();
    }

    public Double getVelocity(@NotNull Degradable<Pose> pose) {
        TimestampedCircularBuffer<Pose> poseTimestampedCircularBuffer = anchorStateMap.get(pose);
        if (poseTimestampedCircularBuffer == null || poseTimestampedCircularBuffer.getBufferSize() < poseTimestampedCircularBuffer.getMaxBufferSize() - 1){ //allow if one short
            return Double.NaN;
        }
        double meterPerNano = poseTimestampedCircularBuffer.queryChangeOverTime(PoseUtils::distance);
        return meterPerNano * 1e+11; // centimeters per second
    }
}
