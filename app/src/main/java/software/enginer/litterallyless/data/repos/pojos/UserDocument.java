package software.enginer.litterallyless.data.repos.pojos;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Data;

@Data
public class UserDocument {
    private final String userId;
    private final String username;
    private final Integer detections;

    public static final Function<DocumentSnapshot, UserDocument> documentMapper = d -> new UserDocument(
            d.getId(),
            d.get("username", String.class),
            d.get("detections", Integer.class)
    );

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("detections", detections);
        return map;
    }
}
