package software.enginer.litterallyless.ui.models.factories;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import software.enginer.litterallyless.data.repos.MapRepository;
import software.enginer.litterallyless.ui.models.MapViewModel;

public class MapModelFactory implements ViewModelProvider.Factory {

    private final MapRepository repository;

    public MapModelFactory(MapRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        MapViewModel mapViewModel = new MapViewModel(repository);
        T cast = modelClass.cast(mapViewModel);
        if (cast != null){
            return cast;
        }
        throw new RuntimeException();
    }
}
