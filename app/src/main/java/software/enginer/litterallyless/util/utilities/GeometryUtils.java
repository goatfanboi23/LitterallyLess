package software.enginer.litterallyless.util.utilities;


import android.util.Log;

import com.mapbox.geojson.Point;

import java.util.List;

import software.enginer.litterallyless.ui.models.ArCoreViewModel;

public class GeometryUtils {
    private static final String TAG = GeometryUtils.class.getSimpleName();

    public static boolean pointInPolygon(List<Point> points, Point target){
        int count = 0;
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1)% points.size());
            Log.d(TAG, "Point1 (long, lat)" + p1.longitude() +", " + p1.latitude());
            if (target.equals(p1) || target.equals(p2)){
                return true;
            }
            // check if point changes from being below or above then it crosses edge
            if ((p1.latitude() > target.latitude()) != (p2.latitude() > target.latitude())) {
//               slope = (x - ax) * (by - ay) - (bx - ax) * (y - ay)
                double slope = (target.longitude() - p1.longitude()) * (p2.latitude() - p1.latitude()) - (p2.longitude() - p1.longitude()) * (target.latitude() - p1.latitude());
                // found this online
                if (slope == 0) return true;
                if ((slope < 0) != (p2.latitude() < p1.latitude())){
                    count++;
                }
            }
        }
        return (count % 2) == 1;
    }
}
