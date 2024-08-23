package software.enginer.litterallyless.util;

import com.google.ar.core.Pose;

public class PoseUtils {
    public static double distance(Pose p1, Pose p2){
        float[] worldPoseP1 = p1.getTranslation();
        float[] worldPoseP2 = p2.getTranslation();

        float deltaX = worldPoseP2[0] -worldPoseP1[0];
        float deltaY = worldPoseP2[1] -worldPoseP1[1];
        float deltaZ = worldPoseP2[2] -worldPoseP1[2];

        return Math.sqrt((Math.pow(deltaX, 2) + Math.pow(deltaY, 2) + Math.pow( deltaZ, 2)));

    }
}
