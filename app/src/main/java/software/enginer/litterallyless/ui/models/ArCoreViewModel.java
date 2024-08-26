package software.enginer.litterallyless.ui.models;

import android.app.Application;
import android.graphics.Paint;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.ar.core.Anchor;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Pose;
import com.google.mediapipe.tasks.components.containers.Category;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import software.enginer.litterallyless.opengl.TextTextureCache;
import software.enginer.litterallyless.data.repos.ARDetectorRepository;
import software.enginer.litterallyless.data.AnchorProximityResult;
import software.enginer.litterallyless.data.DetectionResult;
import software.enginer.litterallyless.ui.state.LabeledAnchor;
import software.enginer.litterallyless.ui.state.ArCoreUIState;
import software.enginer.litterallyless.util.Degradable;
import software.enginer.litterallyless.util.ResettableCountdownLatch;
import software.enginer.litterallyless.util.convertors.YuvConverter;

public class ArCoreViewModel extends AndroidViewModel {
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private final MutableLiveData<ArCoreUIState> uiState = new MutableLiveData<>(new ArCoreUIState());
    private final ARDetectorRepository detectorRepository;
    private Frame frame;
    private final ResettableCountdownLatch latch;

    public ArCoreViewModel(@NonNull Application application) {
        super(application);
        latch = new ResettableCountdownLatch(1);
        latch.countDown();
        detectorRepository = new ARDetectorRepository(application.getApplicationContext(), this::onResult);
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
        if (!hits.isEmpty()){
            HitResult result = hits.get(0);
            Anchor anchor = result.getTrackable().createAnchor(result.getHitPose());
            return anchor;
        }
        return null;
    }

    private void onResult(DetectionResult result) {
        ObjectDetectorResult objectDetectorResult = result.getResults().get(0);
        List<LabeledAnchor> labeledAnchorList = new ArrayList<>();
        long start = System.nanoTime();
        try {
            //TODO: PROCESS MULTIPLE DETECTIONS AT A TIME

            // iterate over detections from ai model
            labeledAnchorList = objectDetectorResult.detections().stream().map(rect -> {
                Anchor anchor = createAnchor(rect.boundingBox().centerX(), rect.boundingBox().centerY(), frame);
                if (anchor != null) {
                    Pose anchorPose = anchor.getPose();
                    // find the closest anchor to the detection (TODO: account for velocity)
                    AnchorProximityResult closestAnchor = detectorRepository.getClosestAnchor(anchorPose);
                    Degradable<Pose> degradablePose;
//                    Log.i(ArCoreViewModel.class.getSimpleName(), "ANCHOR SIZE: " + detectorRepository.anchorCount());

                    // purple will mean that their is not enough information to detect if object is moving
                    Paint paint = TextTextureCache.purpleTextPaint;
                    if (closestAnchor != null && closestAnchor.getAnchor() != null){
                        //offset proximity by how much we would have moved given our velocity
                        Double velocity = detectorRepository.getVelocity(closestAnchor.getAnchor());
                        double proximity = closestAnchor.getProximity();
                        if (!velocity.isNaN()){
                            proximity -= Math.sin(proximity) * (result.getInferenceTime()*(Math.abs(velocity)/1e+5));
                        }
                        // 0.23 meter = ~ 9 inches
                        // if delta is > 9 inches since last frame, create a new anchor
                        if (proximity > 0.23){
                            Log.i(ARDetectorRepository.class.getSimpleName(), "ADDING ANCHOR");
                            degradablePose = detectorRepository.addAnchor(anchorPose);
                        }else{
                            degradablePose = closestAnchor.getAnchor();
                        }
                        //get velocity of anchor
                        Double vel = detectorRepository.getVelocity(degradablePose);
                        if (!vel.isNaN()){
                            Log.i(ArCoreViewModel.class.getSimpleName(), "VELOCITY: " + vel);
                            //if moving (more than 5 cm per sec)[~2 inches per second], then label object with green
                            paint = Math.abs(vel) > 3 ? TextTextureCache.greenTextPaint : TextTextureCache.redTextPaint;
                        }
                    }else{
                       detectorRepository.addAnchor(anchorPose);
                    }
                    Category category = rect.categories().get(0);
                    String drawableText = category.categoryName() + " " + df.format(category.score());
                    return new LabeledAnchor(anchorPose, drawableText, paint);
                } else {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }catch (Exception e) {
            Log.e(ArCoreViewModel.class.getSimpleName(), "Error creating Anchor", e);
        }finally {
            latch.countDown();
            long end = System.nanoTime();
            Log.i(ArCoreViewModel.class.getSimpleName(),"ANCHOR CALC TIME:" + ((end-start)/1e+6));
        }
        double fps = 1000.0 / result.getInferenceTime();
        detectorRepository.getDetectionFpsMonitor().add(fps);
        double avgFPS = detectorRepository.getDetectionFpsMonitor().averageDouble(Double::doubleValue);
        String inferenceLabel = "AVG FPS (30 FRAMES): " + df.format(avgFPS);
        ArCoreUIState arCoreUIState = new ArCoreUIState(labeledAnchorList, inferenceLabel);
        uiState.postValue(arCoreUIState);
    }

    public LiveData<ArCoreUIState> getUiState() {
        return uiState;
    }

    public void detectLivestreamFrame(Image image, int rotation, @Nullable YuvConverter converter) {
       try{
           latch.await();
       }catch (InterruptedException e){
           throw new RuntimeException(e);
       }finally {
           latch.reset();
       }
        boolean success = detectorRepository.detectLivestreamFrame(image, rotation, converter);
        if (!success){
            latch.countDown();
        }
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public void awaitDetection() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void onEndDrawFrame(){
        detectorRepository.degradeAnchors();
    }
}