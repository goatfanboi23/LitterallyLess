package software.enginer.litterallyless.ui.state;

import android.net.Uri;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@Data
public class FirebaseState {
    private final String username;
    private final Uri profileURI;
    private final boolean signedIn;
    private final int detections;
    private final String uuid;

    public FirebaseState() {
        username = "";
        profileURI = Uri.EMPTY;
        signedIn = false;
        detections = 0;
        uuid = "";
    }
}
