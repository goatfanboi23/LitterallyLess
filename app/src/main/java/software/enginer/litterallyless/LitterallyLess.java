package software.enginer.litterallyless;

import android.app.Application;

import lombok.Getter;
import software.enginer.litterallyless.data.repos.FirebaseUserRepository;

@Getter
public class LitterallyLess extends Application {
    private FirebaseUserRepository firebaseUserRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseUserRepository = new FirebaseUserRepository();
    }
}
