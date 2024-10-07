package software.enginer.litterallyless.ui.state;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import software.enginer.litterallyless.data.repos.pojos.UserDocument;

@AllArgsConstructor
@Data
public class LeaderboardState {
    private final List<UserDocument> documents;

    public LeaderboardState() {
        this.documents = new ArrayList<>();
    }
}
