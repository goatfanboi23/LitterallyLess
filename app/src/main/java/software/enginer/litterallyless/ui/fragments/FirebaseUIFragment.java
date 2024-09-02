package software.enginer.litterallyless.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;

import java.util.List;

import software.enginer.litterallyless.databinding.FragmentFirebaseBinding;
import software.enginer.litterallyless.ui.models.FirebaseViewModel;

// adapted from https://github.com/firebase/snippets-android/blob/9d886f75b6c5eea5f3366a515e74e8f394118f64/auth/app/src/main/java/com/google/firebase/quickstart/auth/FirebaseUIActivity.java#L49-L62
public class FirebaseUIFragment extends Fragment {
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(), this::onSignInResult
    );

    private FragmentFirebaseBinding binding;
    private FirebaseViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFirebaseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        binding.signInButton.setOnClickListener(v -> createSignInIntent());
    }

    public void createSignInIntent() {
        if (viewModel.updateLoginInfo()) return;
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(List.of(new AuthUI.IdpConfig.GoogleBuilder().build()))
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        viewModel.provideAuthenticationResult(result);
    }



}
