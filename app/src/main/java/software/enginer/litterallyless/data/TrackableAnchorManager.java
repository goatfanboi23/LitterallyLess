package software.enginer.litterallyless.data;

import android.util.Log;

import com.google.ar.core.Pose;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import software.enginer.litterallyless.util.filters.CostProximityResult;
import software.enginer.litterallyless.util.Degradable;
import software.enginer.litterallyless.util.TimestampedCircularBuffer;
import software.enginer.litterallyless.util.filters.PoseFilter;
import software.enginer.litterallyless.util.utilities.PoseUtils;

public class TrackableAnchorManager {

    private final List<Trackable> tracks = Collections.synchronizedList(new ArrayList<>());

    public void degradeAnchors() {
        synchronized (tracks){
            Iterator<Trackable> iterator = tracks.iterator();
            while (iterator.hasNext()) {
                Trackable entry = iterator.next();
                int ticks = entry.getCurrentPose().incrementAndGetTicks();
                if (ticks >= 10) {
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

    public boolean moveToPoses(@NotNull Trackable trackable, @NotNull Pose newPose){
        trackable.getCurrentPose().setValue(newPose);
        trackable.getCurrentPose().refresh();
        //update filter
        trackable.getFilter().update(newPose);
        trackable.getPoseBuffer().addStamped(trackable.getFilter().getPose());
        Double velocity = getVelocity(trackable);
        Log.i(TrackableAnchorManager.class.getSimpleName(), "VELOCITY: " + velocity);

        if (!velocity.isNaN() && !velocity.isInfinite() && Math.abs(velocity) > 6){
            int increaseAmount = Math.min(3, ((int) Math.abs(velocity)) / 6);
            int count = trackable.getCollectionsThresholdCounter().addAndGet(increaseAmount);
            Log.i(TrackableAnchorManager.class.getSimpleName(), "COUNT: " + count);
            boolean result = count >= 30;
            if (result){
                trackable.getCollected().set(true);
            }
            return result;
        }else{
            trackable.getCollectionsThresholdCounter().set(0);
            return false;
        }
    }

    public void addTrackable(@NotNull Degradable<Pose> poseDegradable) {
        PoseFilter filter = new PoseFilter(0.9f);
        filter.update(poseDegradable.getValue());
        tracks.add(new Trackable(poseDegradable, new TimestampedCircularBuffer<>(10), filter));
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

}
