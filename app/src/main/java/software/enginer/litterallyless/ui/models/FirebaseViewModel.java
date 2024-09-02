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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import software.enginer.litterallyless.data.repos.FirebaseUserRepository;
import software.enginer.litterallyless.ui.state.FirebaseState;

public class FirebaseViewModel extends ViewModel {
    private final FirebaseUserRepository repository;
    private final MutableLiveData<FirebaseState> uiState = new MutableLiveData<>(new FirebaseState());


    public FirebaseViewModel() {
        this.repository = new FirebaseUserRepository();
    }


    public void provideAuthenticationResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            repository.setUser(user);
            if (user != null){
                FirebaseState state = stateFromUser(user);
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
    public boolean updateLoginInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        repository.setUser(user);
        if (user != null){
            FirebaseState curState = uiState.getValue();
            FirebaseState state = stateFromUser(user);
            if (curState != null && !curState.equals(state)){
                uiState.postValue(state);
            }
            return true;
        }
        return false;
    }
    @NotNull
    public FirebaseState stateFromUser(@Nullable FirebaseUser user){
        if (user == null) return new FirebaseState();
        Uri photoUrl = user.getPhotoUrl();
        return new FirebaseState(user.getDisplayName(), photoUrl, true);
    }

    public void signOut(Context context) {
        AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener(task -> {
                    FirebaseState state = stateFromUser(null);
                    uiState.postValue(state);
                });
        repository.setUser(null);
    }
}
