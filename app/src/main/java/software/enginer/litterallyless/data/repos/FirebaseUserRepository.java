package software.enginer.litterallyless.data.repos;

import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

public class FirebaseUserRepository {

    private final AtomicReference<FirebaseUser> user = new AtomicReference<>(null);

    public void setUser(FirebaseUser user) {
        this.user.set(user);
    }

    @Nullable
    public FirebaseUser getUser() {
        return user.get();
    }
}
