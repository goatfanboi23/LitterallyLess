package software.enginer.litterallyless.data;

import com.google.ar.core.Pose;

import org.apache.commons.math3.linear.RealVector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import software.enginer.litterallyless.CostProximityResult;
import software.enginer.litterallyless.util.Degradable;
import software.enginer.litterallyless.util.TimestampedCircularBuffer;
import software.enginer.litterallyless.util.utilities.DetectionFilter;
import software.enginer.litterallyless.util.utilities.PoseUtils;

public class TrackableAnchorManager {

    private final List<Trackable> tracks = Collections.synchronizedList(new ArrayList<>());

    public void degradeAnchors() {
        synchronized (tracks){
            Iterator<Trackable> iterator = tracks.iterator();
            while (iterator.hasNext()) {
                Trackable entry = iterator.next();
                int ticks = entry.getCurrentPose().incrementAndGetTicks();
                if (ticks >= 20) {
                    iterator.remove();
                }
            }
        }
    }

    public List<Double> getVelocities(){
        synchronized (tracks){
            return tracks.stream()
                    .map(this::getVelocity)
                    .filter(d -> !d.isNaN())
                    .collect(Collectors.toList());
        }
    }

    public void moveToPoses(@NotNull Trackable trackable, @NotNull Pose newPose){
        Pose oldPose = trackable.getCurrentPose().getValue();
        trackable.getCurrentPose().setValue(newPose);
        trackable.getCurrentPose().refresh();
        trackable.getPoseBuffer().addStamped(oldPose);
        //update filter
        trackable.getFilter().predict(System.nanoTime());
        trackable.getFilter().update(newPose);
    }

    public void addTrackable(@NotNull Degradable<Pose> poseDegradable) {
        tracks.add(new Trackable(poseDegradable, new TimestampedCircularBuffer<>(10), new DetectionFilter(poseDegradable.getValue())));
    }

    public double[] getMetersToAnchors(@NotNull Pose pose){
        synchronized (tracks){
            return tracks.stream().mapToDouble(t -> {
                Pose trackablePose = t.getCurrentPose().getValue();
                return PoseUtils.distance(trackablePose, pose);
            }).toArray();
        }
    }
    public CostProximityResult getMicroMetersToAnchors(@NotNull Pose pose){
        synchronized (tracks){
            CostProximityResult result = new CostProximityResult(tracks.size());
            tracks.forEach(track -> {
                Pose trackablePose = track.getCurrentPose().getValue();
                int distance = (int) (PoseUtils.distance(trackablePose, pose) * 1e+6);
                AnchorProximityResult anchorProximityResult = new AnchorProximityResult(distance, track);
                result.add(anchorProximityResult);
            });
            return result;
        }
    }

    public int getTrackerCount() {
        return tracks.size();
    }

    public Double getVelocity(@NotNull Trackable pose) {
        TimestampedCircularBuffer<Pose> poseBuffer = pose.getPoseBuffer();
        if (poseBuffer == null){ //allow if one short
            return Double.NaN;
        }
        double meterPerNano = poseBuffer.queryChangeOverTime(PoseUtils::distance);
        return meterPerNano * 1e+11; // centimeters per second
    }
    public void updateFilter(){

    }

    public Double estimatedVelocity(@NotNull Trackable pose){
        RealVector state = pose.getFilter().getState();
        double vx = state.getEntry(3);
        double vy = state.getEntry(4);
        double vz = state.getEntry(5);
        return Math.sqrt(vx*vx + vy*vy + vz*vz);
    }

}
