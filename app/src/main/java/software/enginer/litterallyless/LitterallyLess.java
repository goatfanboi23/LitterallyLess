package software.enginer.litterallyless;

import android.app.Application;

import com.google.android.gms.location.LocationServices;

import lombok.Getter;
import software.enginer.litterallyless.data.repos.FirebaseUserRepository;
import software.enginer.litterallyless.data.repos.MapRepository;

@Getter
public class LitterallyLess extends Application {
    private FirebaseUserRepository firebaseUserRepository;
    private MapRepository mapRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseUserRepository = new FirebaseUserRepository();
        mapRepository = new MapRepository(LocationServices.getFusedLocationProviderClient(this));
    }
}
