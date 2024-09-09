package software.enginer.litterallyless.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import software.enginer.litterallyless.LitterallyLess;
import software.enginer.litterallyless.data.repos.FirebaseUserRepository;
import software.enginer.litterallyless.data.repos.pojos.UserDocument;
import software.enginer.litterallyless.databinding.FragmentLeaderboardBinding;
import software.enginer.litterallyless.ui.LeaderboardAdapter;
import software.enginer.litterallyless.ui.models.FirebaseViewModel;
import software.enginer.litterallyless.ui.models.factories.FirebaseModelFactory;
import software.enginer.litterallyless.ui.state.FirebaseState;

public class LeaderboardFragment extends Fragment {
    private FragmentLeaderboardBinding binding;
    private LeaderboardAdapter leaderboardAdapter;
    private FirebaseViewModel firebaseViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LitterallyLess app = (LitterallyLess)requireActivity().getApplication();
        FirebaseUserRepository repo = app.getFirebaseUserRepository();
        firebaseViewModel = new ViewModelProvider(this, new FirebaseModelFactory(repo)).get(FirebaseViewModel.class);

        leaderboardAdapter = new LeaderboardAdapter(new ArrayList<>());
        binding.leaderboardView.setAdapter(leaderboardAdapter);
        binding.leaderboardView.setLayoutManager(new LinearLayoutManager(requireContext()));
        firebaseViewModel.getUIState().observe(getViewLifecycleOwner(), new Observer<FirebaseState>() {
            @Override
            public void onChanged(FirebaseState state) {
                firebaseViewModel.queryLeaderboard(userDocuments -> {
                    List<String> items = userDocuments.stream().map(d -> d.getUserId() + ":" + d.getDetections()).collect(Collectors.toList());
                    leaderboardAdapter.setLocalDataSet(items);
                });
            }
        });
    }
}