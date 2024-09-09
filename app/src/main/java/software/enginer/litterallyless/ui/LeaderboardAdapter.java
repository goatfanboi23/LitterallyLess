package software.enginer.litterallyless.ui;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import lombok.Data;
import lombok.Getter;
import software.enginer.litterallyless.R;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>{

    private final ReentrantLock dataSetLock = new ReentrantLock();
    private final List<String> localDataSet;

    public LeaderboardAdapter(List<String> localDataSet) {
        this.localDataSet = localDataSet;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setLocalDataSet(List<String> localDataSet) {
        dataSetLock.lock();
        this.localDataSet.clear();
        this.localDataSet.addAll(localDataSet);
        Log.i(LeaderboardAdapter.class.getSimpleName(),"LEADERBOARD DATASET SIZE: " + localDataSet.size());
        dataSetLock.unlock();
        notifyDataSetChanged();
    }

    public void appendLocalDataSet(String item) {
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
        viewHolder.getTextView().setText(localDataSet.get(position));
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
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.leaderboard_item_text);
        }
    }
}
