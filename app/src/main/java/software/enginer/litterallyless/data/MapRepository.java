package software.enginer.litterallyless.data;


import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CameraState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;

public class MapRepository {
    private static final String styleURL = "mapbox://styles/hexcoded/clwmn05vp02hw01qogr66368v";
    private MapStyle mapStyle;
    private List<String> styleLayerIds;
    @Nullable
    private CameraState userInitialLocation;
    private final CameraOptions mapDefaultCameraOptions;
    private final TransitionSpec transitionSpec;

    private final ReentrantReadWriteLock styleLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock locationLock = new ReentrantReadWriteLock();

    public MapRepository() {
        this.mapStyle = new MapStyle(styleURL);
        this.userInitialLocation = null;
        this.styleLayerIds = new ArrayList<>();
        this.mapDefaultCameraOptions = new CameraOptions.Builder()
                .zoom(2d)
                .center(Point.fromLngLat(-100, 40))
                .bearing(8d)
                .pitch(0d)
                .build();
        transitionSpec = new DefaultTransitionSpec();
    }

    public void setStyleLayerIds(List<String> styleLayerIds) {
        styleLock.writeLock().lock();
        this.styleLayerIds = styleLayerIds;
        styleLock.writeLock().unlock();
    }

    public List<String> getStyleLayerIds() {
        styleLock.readLock().lock();
        List<String> ids = new ArrayList<>(styleLayerIds);
        styleLock.readLock().unlock();
        return ids;
    }

    public void setUserInitialLocation(CameraState userInitialLocation) {
        locationLock.writeLock().lock();
        this.userInitialLocation = userInitialLocation;
        locationLock.writeLock().unlock();
    }

    public CameraState getUserInitialLocation() {
        locationLock.readLock().lock();
        CameraState state = null;
        if (userInitialLocation != null){
            state = new CameraState(
                    userInitialLocation.getCenter(),
                    userInitialLocation.getPadding(),
                    userInitialLocation.getZoom(),
                    userInitialLocation.getBearing(),
                    userInitialLocation.getPitch()
            );
        }
        locationLock.readLock().unlock();
        return state;
    }

    public MapStyle getMapStyle() {
        return mapStyle;
    }

    public CameraOptions getDefaultCameraOptions() {
        return mapDefaultCameraOptions;
    }

    public TransitionSpec getTransitionSpec() {
        return transitionSpec;
    }
}
