package software.enginer.litterallyless.ui.models.factories;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import software.enginer.litterallyless.data.repos.FirebaseUserRepository;
import software.enginer.litterallyless.ui.models.FirebaseViewModel;

public class FirebaseModelFactory implements ViewModelProvider.Factory {

    private final FirebaseUserRepository repository;

    public FirebaseModelFactory(FirebaseUserRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        FirebaseViewModel firebaseViewModel = new FirebaseViewModel(repository);
        T cast = modelClass.cast(firebaseViewModel);
        if (cast != null){
            return cast;
        }
        throw new RuntimeException();
    }
}
