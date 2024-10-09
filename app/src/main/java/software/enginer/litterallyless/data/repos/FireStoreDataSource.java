package software.enginer.litterallyless.data.repos;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

import software.enginer.litterallyless.data.repos.pojos.UserDocument;

public class FireStoreDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getUserDetections(@Nullable FirebaseUser user, @NotNull Consumer<Integer> detectionQueryCallback) {
        if (user == null) {
            detectionQueryCallback.accept(0);
            return;
        }
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            Integer detections = -1;
            try {
                detections = documentSnapshot.get("detections", Integer.class);
            } catch (NullPointerException e) {
                Log.e(FireStoreDataSource.class.getSimpleName(), "Current user does not have field \"detections\"", e);
            } catch (ClassCastException e) {
                Log.e(FireStoreDataSource.class.getSimpleName(), "field \"detections\" is not of type integer", e);
            }
            if (detections != null) {
                detectionQueryCallback.accept(detections);
            } else {
                detectionQueryCallback.accept(0);
            }

        });
    }

    public void setUserDetections(@Nullable FirebaseUser user, int det, Runnable onCompletion) {
        if (user == null) {
            onCompletion.run();
            return;
        }
        db.collection("users")
                .document(user.getUid()).set(Map.of("detections", det), SetOptions.merge())
                .addOnCompleteListener(unused -> {
                    onCompletion.run();
                });
    }

    public Task<Void> setUserDetections(@Nullable FirebaseUser user, int det) {
        if (user == null) {
            return null;
        }
        return db.collection("users")
                .document(user.getUid())
                .set(Map.of("detections", det), SetOptions.merge());
    }

    public Task<QuerySnapshot> queryUsersByDetections() {
        return db.collection("users").orderBy("detections", Query.Direction.DESCENDING).get();
    }

    public Task<Void> setUsername(@NotNull FirebaseUser firebaseUser, @NotNull String username) {
        return db.collection("users").document(firebaseUser.getUid()).update("username", username);
    }

    public Task<DocumentSnapshot> queryUserById(String id) {
        return db.collection("users").document(id).get();
    }

    public void addUserIfNotExist(@NotNull FirebaseUser firebaseUser, int detections, @Nullable Runnable runnable) {
        UserDocument data = new UserDocument(firebaseUser.getUid(), firebaseUser.getDisplayName(), detections);
        db.runTransaction((transaction) -> {
            DocumentSnapshot snapshot = transaction.get(db.collection("users").document(firebaseUser.getUid()));
            if (!snapshot.exists()) {
                transaction.set(db.collection("users").document(firebaseUser.getUid()), data.toMap());
            }
            return null;
        }).addOnCompleteListener(task -> {
            if (runnable != null){
                runnable.run();
            }
        });
    }
}
