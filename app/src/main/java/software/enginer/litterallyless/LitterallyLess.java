package software.enginer.litterallyless;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LitterallyLess extends Application {
    private final ExecutorService servicePool = Executors.newFixedThreadPool(4);

    public ExecutorService getServicePool() {
        return servicePool;
    }
}
