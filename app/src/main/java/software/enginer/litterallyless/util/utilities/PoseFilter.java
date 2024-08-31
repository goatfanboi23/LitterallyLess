package software.enginer.litterallyless.util.utilities;

import com.google.ar.core.Pose;

public class PoseFilter {
    MovingAverageFilter xFilter;
    MovingAverageFilter yFilter;
    MovingAverageFilter zFilter;

    public PoseFilter(float smoothingFactor){
        this.xFilter = new MovingAverageFilter(smoothingFactor);
        this.yFilter = new MovingAverageFilter(smoothingFactor);
        this.zFilter = new MovingAverageFilter(smoothingFactor);
    }
    public PoseFilter update(Pose pose){
        xFilter.filter(pose.tx());
        yFilter.filter(pose.ty());
        zFilter.filter(pose.tz());
        return this;
    }

    public Pose getPose(){
        return Pose.makeTranslation(new float[]{xFilter.getValue(), yFilter.getValue(), zFilter.getValue()});
    }
}
