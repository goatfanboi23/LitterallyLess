package software.enginer.litterallyless.ui.state;

import android.net.Uri;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FirebaseState {
    private final String username;
    private final Uri profileURI;
    private final boolean signedIn;

    public FirebaseState() {
        username = "";
        profileURI = Uri.EMPTY;
        signedIn = false;
    }
}
