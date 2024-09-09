package software.enginer.litterallyless.data.repos;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import software.enginer.litterallyless.data.repos.pojos.UserDocument;
import software.enginer.litterallyless.util.TimeExpirable;

public class FirebaseUserRepository {

    private final AtomicReference<FirebaseUser> user = new AtomicReference<>(null);
    private final AtomicInteger detections = new AtomicInteger();
    private final FireStoreDataSource dataSource = new FireStoreDataSource();
    private final ReadWriteLock userCacheLock = new ReentrantReadWriteLock();
    private TimeExpirable<List<UserDocument>> expirableUserList = null;
    private final ReentrantLock savingTaskStatusLock = new ReentrantLock();
    private Task<Void> savingTask = null;

    public void setUser(FirebaseUser user) {
        this.user.set(user);
        if (user == null){
            // reset user variables
            detections.set(0);
        }else {
            //fetch data for new user
            lookupUserDetections(detections::set);
        }
    }

    @Nullable
    public FirebaseUser getUser() {
        return user.get();
    }

    public void lookupUserDetections(Consumer<Integer> detectionQueryCallback){
        savingTaskStatusLock.lock();
        if (savingTask != null){
            savingTask.addOnCompleteListener(task -> {
                dataSource.getUserDetections(getUser(), detectionQueryCallback);
                savingTaskStatusLock.lock();
                savingTask = null;
                savingTaskStatusLock.unlock();
            });
            savingTaskStatusLock.unlock();
        }else{
            savingTaskStatusLock.unlock();
            dataSource.getUserDetections(getUser(), detectionQueryCallback);
        }
    }

    public int incrementDetections(int num){
        return detections.addAndGet(num);
    }

    public void saveUserDetections() {
        savingTaskStatusLock.lock();
        savingTask = dataSource.setUserDetections(getUser(), detections.get());
        savingTaskStatusLock.unlock();
        userCacheLock.writeLock().lock();
        expirableUserList = null;
        userCacheLock.writeLock().unlock();
    }

    public void queryUsersByDetections(Consumer<List<UserDocument>> queryCallback) {
        userCacheLock.readLock().lock();
        if (expirableUserList != null && !expirableUserList.isExpired()){
            List<UserDocument> value = expirableUserList.getValue();
            userCacheLock.readLock().unlock();
            if (value == null){
                queryNetworkForSortedDetections(queryCallback);
            }else{
                queryCallback.accept(value);
            }
        }else{
            userCacheLock.readLock().unlock();
            queryNetworkForSortedDetections(queryCallback);
        }
    }

    private void queryNetworkForSortedDetections(Consumer<List<UserDocument>> queryCallback){
        dataSource.queryUsersByDetections().addOnSuccessListener(queryDocumentSnapshots -> {
            List<UserDocument> detects = queryDocumentSnapshots.getDocuments()
                    .stream()
                    .map(d -> new UserDocument(
                            d.getId(),
                            d.get("detections", Integer.class)
                    ))
                    .collect(Collectors.toList());
            userCacheLock.writeLock().lock();
            expirableUserList = new TimeExpirable<>(detects, Duration.ofMinutes(1));
            userCacheLock.writeLock().unlock();
            queryCallback.accept(detects);
        });
    }

    public int getUserDetections() {
        return detections.get();
    }
}
