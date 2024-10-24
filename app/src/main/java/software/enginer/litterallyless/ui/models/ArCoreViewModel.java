package software.enginer.litterallyless.ui.models;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Paint;
import android.location.Location;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.core.Anchor;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Pose;
import com.google.ar.core.exceptions.FatalException;
import com.google.mediapipe.tasks.components.containers.Category;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;
import com.mapbox.geojson.Point;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import software.enginer.litterallyless.data.DetectionReading;
import software.enginer.litterallyless.data.repos.FirebaseUserRepository;
import software.enginer.litterallyless.data.repos.MapRepository;
import software.enginer.litterallyless.util.filters.CostProximityResult;
import software.enginer.litterallyless.data.Trackable;
import software.enginer.litterallyless.opengl.TextTextureCache;
import software.enginer.litterallyless.data.repos.ARDetectorRepository;
import software.enginer.litterallyless.data.AnchorProximityResult;
import software.enginer.litterallyless.data.DetectionResult;
import software.enginer.litterallyless.ui.state.LabeledAnchor;
import software.enginer.litterallyless.ui.state.ArCoreUIState;
import software.enginer.litterallyless.util.Degradable;
import software.enginer.litterallyless.util.filters.HungarianAlgorithm;
import software.enginer.litterallyless.util.convertors.YuvConverter;
import software.enginer.litterallyless.util.utilities.GeometryUtils;

public class ArCoreViewModel extends AndroidViewModel {
    private static final String TAG = ArCoreViewModel.class.getSimpleName();
    private static final DecimalFormat df = new DecimalFormat("0.00");

    private final MutableLiveData<ArCoreUIState> uiState = new MutableLiveData<>(new ArCoreUIState());
    private final ARDetectorRepository detectorRepository;
    private final FirebaseUserRepository userRepository;
    private Frame frame;
    private boolean usingFrame = false;
    private final ReentrantLock frameLock = new ReentrantLock();
    private final Condition condition = frameLock.newCondition();
    private final MapRepository mapRepository;

    public ArCoreViewModel(@NonNull Application application, FirebaseUserRepository userRepository, MapRepository mapRepository) {
        super(application);
        this.detectorRepository = new ARDetectorRepository(application.getApplicationContext(), this::onResult);
        this.userRepository = userRepository;
        this.mapRepository = mapRepository;
    }

    private Anchor createAnchor(float imageX, float imageY, Frame frame) {
        float[] conv = new float[4];
        float[] convOut = new float[4];

        conv[0] = imageX;
        conv[1] = imageY;
        frame.transformCoordinates2d(
                Coordinates2d.IMAGE_PIXELS,
                conv,
                Coordinates2d.VIEW,
                convOut
        );
        List<HitResult> hits = frame.hitTest(convOut[0], convOut[1]);
        if (!hits.isEmpty()) {
            HitResult result = hits.get(0);
            try {
                return result.getTrackable().createAnchor(result.getHitPose());
            } catch (FatalException e) {
                Log.w(TAG, "Failed to create Anchor");
            }
        }
        return null;
    }

    private List<DetectionReading> parseReadings(DetectionResult result) {
        List<DetectionReading> readings = new ArrayList<>();
        long start = System.nanoTime();
        try {
            ObjectDetectorResult objectDetectorResult = result.getResults().get(0);
            readings = objectDetectorResult.detections().stream().map(rect -> {
                Anchor anchor = createAnchor(rect.boundingBox().centerX(), rect.boundingBox().centerY(), frame);
                if (anchor == null) {
                    return null;
                }
                Pose anchorPose = anchor.getPose();
                anchor.detach();
                return new DetectionReading(anchorPose, rect.categories().get(0));
            }).filter(Objects::nonNull).collect(Collectors.toList());
            long end = System.nanoTime();
            Log.d(TAG, "ANCHOR CALC TIME:" + ((end - start) / 1e+6));
        } finally {
            setFrameFree();
        }
        return readings;
    }

    private void onResult(DetectionResult result) {
        List<DetectionReading> readings = parseReadings(result);

        /*
        // process results (non blocking with main thread)


        /--------------------\
        | CREATE COST MATRIX |
        \--------------------/

        //for each reading calculate distance to each track

        Cost Matrix Format
        ____________________
        |   |  t1   |    t2|
        |---|-------|------|
        |a1 | c1-1  |  c1-2|
        |---|-------|------|
        |a2 | c2-1  |  c2-2|
        |___|_______|______|
        */
        long start = System.nanoTime();
        List<LabeledAnchor> labeledAnchorList = new ArrayList<>();
        ;
        int trackerCount = detectorRepository.getAnchorManager().getTrackerCount();
        int detectionCount = readings.size();
        int dim = Math.max(trackerCount, detectionCount);
        int[][] costMatrix = new int[dim][dim];
        List<Integer> dummyRows = new ArrayList<>();
        List<Integer> dummyCols = new ArrayList<>();
        if (dim == 0) {
            return;
        }
        //mark rows that are just there to make the matrix square
        for (int i = detectionCount; i < dim; i++) {
            dummyRows.add(i);
        }
        //mark cols that are just there to make the matrix square
        for (int i = trackerCount; i < dim; i++) {
            dummyCols.add(i);
        }
        int collected = 0;
        List<DetectionReading> newPendingDetections = new ArrayList<>();
        //map of costMatrix row index to list of proximities to trackables.
        HashMap<Integer, List<AnchorProximityResult>> rowProxMap = new HashMap<>();
        for (int row = 0; row < detectionCount; row++) {
            DetectionReading detectionReading = readings.get(row);
            CostProximityResult proxResults = detectorRepository.getAnchorManager().getMicroMetersToAnchors(detectionReading.getPose());
            int[] rowCosts = proxResults.getCosts();
            int minCost;
            if (proxResults.getMinCostIndex() != -1) {
                minCost = rowCosts[proxResults.getMinCostIndex()];
                final double threshold = 0.3;
                if (minCost > (int) (threshold * 1e+6)) {
                    Log.d(TAG, "ROW : " + row + ", MIN COST:" + minCost);
                    dummyRows.add(row);
                    //Add detection to list that will be assigned a new track after the track assignment algorithm finishes assigning other tracks
                    newPendingDetections.add(detectionReading);
                    continue;
                }
            }
            System.arraycopy(rowCosts, 0, costMatrix[row], 0, rowCosts.length);
            rowProxMap.put(row, proxResults.getProximityResult());
        }

        /*

        /----------------------------\
        | COST MATRIX MUST BE SQUARE |
        \----------------------------/


         This section is here for theory (it is not needed because values are initialized to zero when array is created
        -------------------------------------------------------------
        if (detectionCount > trackerCount){
            int dummyCols = detectionCount - trackerCount;
            //for every detection that does not have a tracker we have to add a "dummy" tracker with a cost of zero
            for (int col = dim - 1; col > dummyCols - 1; col--) {
                //insert zero at every entry in row
                for (int row = 0; row < dim; row++) {
                    costMatrix[row][col] = 0;
                }
            }
        }
        ---------------------------------------------------------------


       This section is here for theory (it is not needed because values are initialized to zero when array is created
       -------------------------------------------------------------
        if (trackerCount > detectionCount){
            int dummyRows = trackerCount - detectionCount;
            // for every tracker that does not have a detection we have to add a "dummy" tracker with an unreasonably high cost to avoid paring
            // A track will be removed if not pared up for some amount of frames.
            for (int row = dim - dummyRows; row < dim; row++) {
                for (int col = 0; col < dim; col++) {
                    costMatrix[row][col] = Integer.MAX_VALUE;
                }
            }
        }
        ---------------------------------------------------------------

        */

        //Apply a filter to create tracks for detections whose cost is too high to be assigned to any track
        HungarianAlgorithm associationAlgo = null;
        try {
            Log.d(TAG, "COST MATRIX SIZE: " + costMatrix.length + " : " + costMatrix[0].length);
            associationAlgo = new HungarianAlgorithm(costMatrix);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Matrix is not square", e);
            throw new RuntimeException(e);
        }
        int[][] optimalAssignment = associationAlgo.findOptimalAssignment();

        // The results are returned as a two-dimensional array where each sub array represents an assignment.
        // The first element of each assignment represents the column number.
        // The second element represents the row number of the costMatrix.
        for (int row = 0; row < dim; row++) {
            assert optimalAssignment[row].length == 2;
            int trackColId = optimalAssignment[row][0];
            int detectionRowId = optimalAssignment[row][1];
            Log.d(TAG, "Detection Row ID: " + detectionRowId + ", Tracking Col ID: " + trackColId);
            //check if detection or track are "dummy" track/detections
            boolean isDummyCol = dummyCols.contains(trackColId);
            boolean isDummyRow = dummyRows.contains(detectionRowId);
            Log.d(TAG, "STATES {\n\t" +
                    "COL DUMMY: " + isDummyCol + "@ " + trackColId + "," +
                    "\n\tROW DUMMY: " + isDummyRow + "@ " + detectionRowId +
                    "\n}");
            //detection was parred with a dummy track
            if (isDummyCol && !isDummyRow) {
                // create track for detection (dummy was created because the number of detections was greater than the number of tracks)
                Log.d(TAG, "CREATING TRACK FOR DETECTION");
                Degradable<Pose> degradable = new Degradable<>(readings.get(detectionRowId).getPose());
                detectorRepository.getAnchorManager().addTrackable(degradable);
            } else if (!isDummyCol && !isDummyRow) {
                List<AnchorProximityResult> rowResults = rowProxMap.get(detectionRowId);
                if (rowResults == null) {
                    Log.e(TAG, "ROW NOT FOUND IN MAP (INTERNAL ERROR)" +
                            "Detection row id = " + detectionRowId + "," +
                            "Possible Detections Row IDS = " + rowProxMap.keySet());
                    throw new RuntimeException();
                }
                AnchorProximityResult trackable = rowResults.get(trackColId);
                DetectionReading detectionReading = readings.get(detectionRowId);
                Trackable track = trackable.getTrack();
                Pose detectionPose = detectionReading.getPose();
                if (track != null) {
                    boolean wasMoving = track.getCollected().get();
                    boolean moving = detectorRepository.getAnchorManager().moveToPoses(track, detectionPose);
                    if (!wasMoving && moving) {
                        collected++;
                    }
                    Paint paint = trackable.getTrack().getCollected().get() ? TextTextureCache.greenTextPaint : TextTextureCache.redTextPaint;
                    //for now we will render here for debugging
                    Category category = detectionReading.getCategory();
                    String drawableText = category.categoryName();
                    LabeledAnchor la = new LabeledAnchor(detectionPose, drawableText, paint);
                    labeledAnchorList.add(la);
                } else {
                    Log.w(TAG, "Trackable Not Found in Proximity Result");
                }
            }
        }

        newPendingDetections.forEach(reading -> {
            Degradable<Pose> degradable = new Degradable<>(reading.getPose());
            detectorRepository.getAnchorManager().addTrackable(degradable);
            Category category = reading.getCategory();
            String drawableText = category.categoryName();
            LabeledAnchor la = new LabeledAnchor(reading.getPose(), drawableText, TextTextureCache.purpleTextPaint);
            labeledAnchorList.add(la);
        });
        detectorRepository.getAnchorManager().degradeAnchors();
        double fps = 1000.0 / result.getInferenceTime();
        detectorRepository.getDetectionFpsMonitor().add(fps);
        int detections = userRepository.incrementDetections(collected);
        if (collected > 0) {
            userRepository.saveUserDetections();
        }
        String inferenceLabel = "Trash Collected: " + detections;
        ArCoreUIState arCoreUIState = new ArCoreUIState(labeledAnchorList, inferenceLabel);
        uiState.postValue(arCoreUIState);
        long end = System.nanoTime();
        Log.d(TAG, "TRACKABLE CALC TIME:" + ((end - start) / 1e+6));
    }

    public LiveData<ArCoreUIState> getUiState() {
        return uiState;
    }

    public void detectLivestreamFrame(Image image, int rotation, @Nullable YuvConverter converter) {
        boolean success = detectorRepository.detectLivestreamFrame(image, rotation, converter);
        if (!success) {
            setFrameFree();
        }
    }

    public void setFrame(Frame frame) {
        waitUntilFrameFree();
        this.frame = frame;
    }

    public void waitUntilFrameFree() {
        frameLock.lock();
        try {
            while (usingFrame) {
                condition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            frameLock.unlock();
        }

    }

    public void setFrameInUse() {
        frameLock.lock();
        try {
            usingFrame = true;
        } finally {
            frameLock.unlock();
        }
    }

    private void setFrameFree() {
        frameLock.lock();
        try {
            usingFrame = false;
            condition.signalAll();
        } finally {
            frameLock.unlock();
        }
    }

    @SuppressLint("MissingPermission")
    public void stillInFeature(Consumer<Boolean> callback) {
        mapRepository.getFusedLocationClient().getLastLocation().addOnCompleteListener(task -> {
            Location result = task.getResult();
            Log.d(TAG, "Current Location (long, lat):" + result.getLongitude() + ", " + result.getLatitude());
            boolean r = GeometryUtils.pointInPolygon(mapRepository.getCoordinates(), Point.fromLngLat(result.getLongitude(), result.getLatitude()));
            callback.accept(r);
        });
    }
}