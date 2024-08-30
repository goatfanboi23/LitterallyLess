package software.enginer.litterallyless.util.utilities;

import com.google.ar.core.Pose;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class DetectionFilter {

    private static final double defaultMeasurementNoise = 0.5d;
    private static final double defaultProcessNoise = 0.05d;

    // State vector [x, y, z, vx, vy, vz, ax, ay, az] (X)
    private RealVector stateEstimation;

    //  Measurement -> State transition matrix (A)
    private RealMatrix stateTransitionMatrix;
    // State -> Measurement matrix (H)
    private RealMatrix measurementMatrix;
    // process noise covariance matrix (Q)
    private RealMatrix processNoiseMatrix;
    // measurement noise covariance matrix (R)
    private RealMatrix measurementNoiseMatrix;

    // error covariance matrix (P)
    private RealMatrix errorMatrix;
    private long lastTimeStep;

    public DetectionFilter(Pose initalPose) {
        this(defaultProcessNoise, defaultMeasurementNoise, initalPose);
    }

    public DetectionFilter(double processNoise, double measurementNoise, Pose initialPose) {
        this.stateTransitionMatrix = MatrixUtils.createRealIdentityMatrix(9); // dt = 0

        this.processNoiseMatrix = MatrixUtils.createRealIdentityMatrix(9).scalarMultiply(processNoise * processNoise);
        this.measurementNoiseMatrix = MatrixUtils.createRealIdentityMatrix(3).scalarMultiply(measurementNoise * measurementNoise);

        // state will have [x, y, z, vx, vy, vz, ax, ay, az], however measurements will only have [x, y, z] this matrix states that
        measurementMatrix = MatrixUtils.createRealMatrix(new double[][]{
                {1, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 0, 0}
        });

        stateEstimation = new ArrayRealVector(new double[]{
                initialPose.tx(), initialPose.ty(), initialPose.tz(), 0, 0, 0, 0 ,0 ,0
        });

        //pose within meter, velocity within 0.5 meters per second (~ 20 inches per second)
        errorMatrix = MatrixUtils.createRealMatrix(new double[][]{
                {1.0, 0.0, 0.0, 0.00, 0.00, 0.00},
                {0.0, 1.0, 0.0, 0.00, 0.00, 0.00},
                {0.0, 0.0, 1.0, 0.00, 0.00, 0.00},
                {0.0, 0.0, 0.0, 0.25, 0.00, 0.00},
                {0.0, 0.0, 0.0, 0.00, 0.25, 0.00},
                {0.0, 0.0, 0.0, 0.00, 0.00, 0.25}
        });
        lastTimeStep = System.nanoTime();
    }

    public void predict(long timestamp) {
        long dt = timestamp - lastTimeStep;
        double at = 0.5 * dt * dt;
        lastTimeStep = timestamp;
        stateTransitionMatrix = MatrixUtils.createRealMatrix(new double[][]{
                {1, 0, 0, dt, 0, 0 , at, 0, 0},
                {0, 1, 0, 0, dt, 0, 0, at, 0},
                {0, 0, 1, 0, 0, dt, 0, 0, at},
                {0, 0, 0, 1, 0, 0, dt, 0, 0},
                {0, 0, 0, 0, 1, 0, 0, dt, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, dt},
                {0, 0, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1}
        });
        stateEstimation = stateTransitionMatrix.operate(stateEstimation);
        errorMatrix = stateTransitionMatrix.multiply(errorMatrix).multiply(stateTransitionMatrix.transpose()).add(processNoiseMatrix);
    }

    public void update(Pose pose) {
        RealVector z = new ArrayRealVector(new double[]{pose.tx(), pose.ty(), pose.tz()});
        RealVector y = z.subtract(measurementMatrix.operate(stateEstimation));

        RealMatrix S = measurementMatrix.multiply(errorMatrix).multiply(measurementMatrix.transpose()).add(measurementNoiseMatrix);
        //efficiently solve for the inverse of S (invert the covariance matrix of the measurement prediction)
        DecompositionSolver solver = new LUDecomposition(S).getSolver();
        RealMatrix K = errorMatrix.multiply(measurementMatrix.transpose()).multiply(solver.getInverse());
        stateEstimation = stateEstimation.add(K.operate(y));

        // update error
        RealMatrix I = MatrixUtils.createRealIdentityMatrix(6);
        errorMatrix = I.subtract(K.multiply(measurementMatrix)).multiply(errorMatrix);
    }

    public RealVector getState() {
        return stateEstimation.copy();
    }
}
