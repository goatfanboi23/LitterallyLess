package software.enginer.litterallyless.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ResettableCountdownLatch {
    private final int initialCount;
    private final AtomicReference<CountDownLatch> atomicLatchReference;

    public ResettableCountdownLatch(int  count) {
        initialCount = count;
        atomicLatchReference = new AtomicReference<>(new CountDownLatch(count));
    }

    public void reset() {
        final CountDownLatch oldLatch = atomicLatchReference.getAndSet(new CountDownLatch(initialCount));
        // handel awaits
        if (oldLatch != null) {
            while ((long) 0 < oldLatch.getCount()) {
                oldLatch.countDown();
            }
        }
    }

    public void countDown() {
        atomicLatchReference.get().countDown();
    }

    public void await() throws InterruptedException {
        atomicLatchReference.get().await();
    }

    public int getCount() {
        return (int) atomicLatchReference.get().getCount();
    }
}
