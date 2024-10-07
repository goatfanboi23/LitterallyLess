package software.enginer.litterallyless.ui.models;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import software.enginer.litterallyless.data.repos.FirebaseUserRepository;
import software.enginer.litterallyless.data.repos.pojos.UserDocument;
import software.enginer.litterallyless.ui.state.FirebaseState;
import software.enginer.litterallyless.ui.state.LeaderboardState;

public class FirebaseViewModel extends ViewModel {
    private final FirebaseUserRepository repository;
    private final MutableLiveData<FirebaseState> uiState = new MutableLiveData<>(new FirebaseState());

    public FirebaseViewModel(FirebaseUserRepository repository) {
        this.repository = repository;
    }

    public void provideAuthenticationResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            repository.setUser(user);
            if (user != null) {
                FirebaseState state = stateFromUser(user, user.getDisplayName(), repository.getUserDetections());
                uiState.postValue(state);
            }
        } else {
            if (response != null) {
                Log.e(FirebaseViewModel.class.getSimpleName(), "Sign in failed", response.getError());
            } else {
                Log.e(FirebaseViewModel.class.getSimpleName(), "Did not receive login response");
            }
        }

    }

    public MutableLiveData<FirebaseState> getUIState() {
        return uiState;
    }

    public boolean isUserLogged() {
        return repository.getUser() != null;
    }

    public void queryLeaderboard(Consumer<List<UserDocument>> callback) {
        repository.queryUsersByDetections(callback);
    }

    public boolean updateLoginInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        repository.setUser(user);
        if (user != null) {
            FirebaseState curState = uiState.getValue();
            if (curState == null || curState.getUsername().isEmpty()) {
                repository.queryUserById(user.getUid()).addOnSuccessListener(documentSnapshot -> {
                    String username = documentSnapshot.getString("username");
                    FirebaseState state = stateFromUser(user, username, repository.getUserDetections());
                    uiState.postValue(state);
                });
            } else {
                FirebaseState state = stateFromUser(user, curState.getUsername(), repository.getUserDetections());
                if (!curState.equals(state)) {
                    uiState.postValue(state);
                }
            }
            return true;
        }
        return false;
    }

    @Nullable
    public FirebaseState stateFromUser(@Nullable FirebaseUser user, @Nullable String username, int detections) {
        if (user == null) return new FirebaseState();
        Uri photoUrl = user.getPhotoUrl();
        if (photoUrl == null) return new FirebaseState();
        if (username == null) return new FirebaseState();
        return new FirebaseState(username, photoUrl, true, detections);
    }

    public void signOut(Context context) {
        AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener(task -> {
                    FirebaseState state = new FirebaseState();
                    uiState.postValue(state);
                });
        repository.setUser(null);
    }

    public void setUsername(String username) {
        repository.setUsername(username, () -> {
            FirebaseUser user = repository.getUser();
            FirebaseState state = stateFromUser(user, username, repository.getUserDetections());
            uiState.postValue(state);
        });
    }
}
