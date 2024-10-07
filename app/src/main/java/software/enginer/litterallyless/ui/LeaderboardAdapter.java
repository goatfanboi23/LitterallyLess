package software.enginer.litterallyless.ui;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.data.model.User;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

import lombok.Getter;
import software.enginer.litterallyless.R;
import software.enginer.litterallyless.data.repos.pojos.UserDocument;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>{

    private final ReentrantLock dataSetLock = new ReentrantLock();
    private final List<UserDocument> localDataSet;

    public LeaderboardAdapter(List<UserDocument> localDataSet) {
        this.localDataSet = localDataSet;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setLocalDataSet(List<UserDocument> localDataSet) {
        dataSetLock.lock();
        this.localDataSet.clear();
        this.localDataSet.addAll(localDataSet);
        Log.i(LeaderboardAdapter.class.getSimpleName(),"LEADERBOARD DATASET SIZE: " + localDataSet.size());
        dataSetLock.unlock();
        notifyDataSetChanged();
    }

    public void appendLocalDataSet(UserDocument item) {
        dataSetLock.lock();
        this.localDataSet.add(item);
        int pos = localDataSet.size() - 1;
        dataSetLock.unlock();
        notifyItemInserted(pos);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.leaderboard_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        dataSetLock.lock();
        viewHolder.getUsernameTextView().setText(localDataSet.get(position).getUsername());
        viewHolder.getPlacementTextView().setText(String.format(Locale.getDefault(),"#%d", position));
        dataSetLock.unlock();
    }

    @Override
    public int getItemCount() {
        dataSetLock.lock();
        int size = localDataSet.size();
        dataSetLock.unlock();
        return size;
    }

    @Getter
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView placementTextView;
        private final TextView usernameTextView;

        public ViewHolder(View view) {
            super(view);
            placementTextView = view.findViewById(R.id.leaderboard_placement);
            usernameTextView = view.findViewById(R.id.leaderboard_username);
        }
    }
}
