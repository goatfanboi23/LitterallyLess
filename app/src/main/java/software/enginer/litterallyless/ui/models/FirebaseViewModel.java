package software.enginer.litterallyless.ui.models;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import software.enginer.litterallyless.data.repos.FirebaseUserRepository;
import software.enginer.litterallyless.data.repos.pojos.UserDocument;
import software.enginer.litterallyless.ui.state.FirebaseState;

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
            if (user != null){
                FirebaseState state = stateFromUser(user, repository.getUserDetections());
                uiState.postValue(state);
            }
        } else {
            if (response != null){
                Log.e(FirebaseViewModel.class.getSimpleName(),"Sign in failed",response.getError());
            }else{
                Log.e(FirebaseViewModel.class.getSimpleName(),"Did not receive login response");
            }
        }

    }

    public MutableLiveData<FirebaseState> getUIState() {
        return uiState;
    }

    public boolean isUserLogged(){
        return repository.getUser() != null;
    }

    public void queryLeaderboard(Consumer<List<UserDocument>> callback){
        repository.queryUsersByDetections(userDocuments -> {
            for (UserDocument doc: userDocuments){
                Log.i(FirebaseViewModel.class.getSimpleName(),"USER ID: " + doc.getUserId());
            }
            callback.accept(userDocuments);
        });
    }

    public boolean updateLoginInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        repository.setUser(user);
        if (user != null){
            FirebaseState curState = uiState.getValue();
            FirebaseState state = stateFromUser(user, repository.getUserDetections());
            if (curState != null && !curState.equals(state)){
                uiState.postValue(state);
            }
            return true;
        }
        return false;
    }
    @NotNull
    public FirebaseState stateFromUser(@Nullable FirebaseUser user, int detections){
        if (user == null) return new FirebaseState();
        Uri photoUrl = user.getPhotoUrl();
        return new FirebaseState(user.getDisplayName(), photoUrl, true, detections);
    }

    public void signOut(Context context) {
        AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener(task -> {
                    FirebaseState state = stateFromUser(null,0);
                    uiState.postValue(state);
                });
        repository.setUser(null);
    }
}
