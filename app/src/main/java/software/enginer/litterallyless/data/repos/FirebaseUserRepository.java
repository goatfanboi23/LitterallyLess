package software.enginer.litterallyless.data.repos;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
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
    private final ReentrantLock savingTaskStatusLock = new ReentrantLock();
    private Task<Void> savingTask = null;
    public void setUser(FirebaseUser user) {
        if (user == null || this.user.get() == null || !user.equals(this.user.get())){
            this.user.set(user);
            if (user == null){
                // reset user variables
                detections.set(0);
            }else {
                //initalize user
                initUser(user);

            }
        }
    }

    private void initUser(FirebaseUser user) {
        //create user in database if not exist
        dataSource.addUserIfNotExist(user,0);
        //fetch data for new user
        lookupUserDetections(detections::set);
    }

    public void setUsername(@NotNull String username, @Nullable Runnable callback){
        FirebaseUser firebaseUser = user.get();
        if (firebaseUser != null){
            dataSource.setUsername(firebaseUser, username).addOnCompleteListener(task -> {
                if (callback != null){
                    callback.run();
                }
            });
        }else if (callback != null){
            callback.run();
        }
    }

    public Task<DocumentSnapshot> queryUserById(String id){
        return dataSource.queryUserById(id);
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
    }

    public void queryUsersByDetections(Consumer<List<UserDocument>> queryCallback) {
        queryNetworkForSortedDetections(queryCallback);
    }

    private void queryNetworkForSortedDetections(Consumer<List<UserDocument>> queryCallback){
        dataSource.queryUsersByDetections().addOnSuccessListener(queryDocumentSnapshots -> {
            List<UserDocument> detects = queryDocumentSnapshots.getDocuments()
                    .stream()
                    .map(UserDocument.documentMapper)
                    .collect(Collectors.toList());
            queryCallback.accept(detects);
        });
    }

    public int getUserDetections() {
        return detections.get();
    }
}
