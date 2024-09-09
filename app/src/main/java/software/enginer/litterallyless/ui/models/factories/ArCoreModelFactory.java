package software.enginer.litterallyless.ui.models.factories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import software.enginer.litterallyless.data.repos.FirebaseUserRepository;
import software.enginer.litterallyless.ui.models.ArCoreViewModel;

public class ArCoreModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final FirebaseUserRepository repository;

    public ArCoreModelFactory(Application application, FirebaseUserRepository repository) {
        this.application = application;
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        ArCoreViewModel arCoreViewModel = new ArCoreViewModel(application, repository);
        T cast = modelClass.cast(arCoreViewModel);
        if (cast != null){
            return cast;
        }
        throw new RuntimeException();
    }
}
