package software.enginer.litterallyless.util.utilities;

import com.google.common.util.concurrent.AtomicDouble;

public class MovingAverageFilter {

    private final float smoothingFactor;
    private final AtomicDouble lastValue = new AtomicDouble();
    private boolean initalized = false;

    public MovingAverageFilter(float smoothingFactor){
        this.smoothingFactor = smoothingFactor;
    }


    public MovingAverageFilter filter(double measurement){
        if (!initalized){
            initalized = true;
            lastValue.set(measurement);
            return this;
        }
        lastValue.set(smoothingFactor * measurement + (1 - smoothingFactor) * lastValue.get());
        return this;
    }

    public float getValue() {
        return (float) lastValue.get();
    }
}
