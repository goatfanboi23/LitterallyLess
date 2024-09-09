package software.enginer.litterallyless.ui;

import static androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.os.StrictMode;

import com.google.ar.core.ArCoreApk;
import com.mapbox.common.MapboxOptions;
import com.squareup.picasso.Picasso;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import software.enginer.litterallyless.BuildConfig;
import software.enginer.litterallyless.LitterallyLess;
import software.enginer.litterallyless.data.repos.FirebaseUserRepository;
import software.enginer.litterallyless.perms.TransitionPermissionRequester;
import software.enginer.litterallyless.ui.models.factories.ArCoreModelFactory;
import software.enginer.litterallyless.ui.fragments.FirebaseUIFragment;
import software.enginer.litterallyless.ui.fragments.LeaderboardFragment;
import software.enginer.litterallyless.ui.fragments.MapFragment;
import software.enginer.litterallyless.R;
import software.enginer.litterallyless.databinding.ActivityMainBinding;
import software.enginer.litterallyless.perms.CameraPermTransition;
import software.enginer.litterallyless.perms.LocationPermTransition;
import software.enginer.litterallyless.ui.fragments.ArCoreFragment;
import software.enginer.litterallyless.ui.fragments.SettingsFragment;
import software.enginer.litterallyless.ui.models.FirebaseViewModel;
import software.enginer.litterallyless.ui.models.MapViewModel;
import software.enginer.litterallyless.ui.models.factories.FirebaseModelFactory;
import software.enginer.litterallyless.util.ConditionalFunction;
import software.enginer.litterallyless.util.LoginConditional;
import software.enginer.litterallyless.util.NavItemSectionListener;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;

    private TransitionPermissionRequester<?> locationPermRequest;
    private TransitionPermissionRequester<String> arPermRequest;
    private FirebaseViewModel firebaseViewModel;
    private MapViewModel mapViewModel;
    private final AtomicBoolean userStateHasInitialized = new AtomicBoolean(false);
    private final NavItemSectionListener navItemSectionListener = new NavItemSectionListener();
    private ConditionalFunction mapConditional, arConditional, homeConditional, settingsConditional;
    private BooleanSupplier loggedInSupplier;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableFullscreen();
        super.onCreate(savedInstanceState);
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build());

        LitterallyLess app = (LitterallyLess)getApplication();
        FirebaseUserRepository repo = app.getFirebaseUserRepository();
        firebaseViewModel = new ViewModelProvider(this, new FirebaseModelFactory(repo)).get(FirebaseViewModel.class);

        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        // create binding
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        // initialize mapbox
        MapboxOptions.setAccessToken(BuildConfig.MapboxAccessToken);

        ArCoreApk.getInstance().checkAvailabilityAsync(this, availability -> {
            if (!availability.isSupported()) {
                activityMainBinding.navBar.removeView(activityMainBinding.navBar.findViewById(R.id.home_menu_item));
            }
        });

        boolean loggedIn = firebaseViewModel.updateLoginInfo();
        if (!loggedIn) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FirebaseUIFragment())
                    .commitNow();
        }

        firebaseViewModel.getUIState().observe(this, state -> {
            if (!userStateHasInitialized.getAndSet(true)) return;
            activityMainBinding.username.setText(state.getUsername());
            Picasso.get().load(state.getProfileURI()).transform(new RoundedCornersTransformation(20,0)).into(activityMainBinding.userIcon);
            activityMainBinding.navBar.setSelectedItemId(R.id.home_menu_item);
        });


        // create permission request helpers
        locationPermRequest = new LocationPermTransition(this, R.id.fragment_container, MapFragment.class);
        arPermRequest = new CameraPermTransition(this, R.id.fragment_container, ArCoreFragment.class);
        initializeConditionals();
        // configure user interactions
        navItemSectionListener.addMapping(R.id.map_menu_item, mapConditional)
                .addMapping(R.id.home_menu_item, homeConditional)
                .addMapping(R.id.ar_menu_item, arConditional)
                .addMapping(R.id.settings_menu_item, settingsConditional);
        activityMainBinding.navBar.setOnItemSelectedListener(navItemSectionListener);
        activityMainBinding.navBar.setSelectedItemId(R.id.home_menu_item);
    }

    private void initializeConditionals() {
        loggedInSupplier = () -> firebaseViewModel.isUserLogged();
        homeConditional = new ConditionalFunction() {
            @Override
            public void onSuccess() {
                if (firebaseViewModel.updateLoginInfo()) {
                    runOnUiThread(() -> {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new LeaderboardFragment())
                                .commitNow();
                    });
                } else {
                    runOnUiThread(() -> {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new FirebaseUIFragment())
                                .commitNow();
                    });
                }
            }
        };
        settingsConditional = new LoginConditional(activityMainBinding.navBar, loggedInSupplier) {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragment_container, SettingsFragment.class, null)
                            .commitNow();
                });
            }
        };
        arConditional = new LoginConditional(activityMainBinding.navBar, loggedInSupplier) {
            @Override
            public void onSuccess() {
                arPermRequest.request();
            }
        };
        mapConditional = new LoginConditional(activityMainBinding.navBar, loggedInSupplier) {
            @Override
            public void onSuccess() {
                locationPermRequest.request();
            }
        };
    }

    private void enableFullscreen() {
        try {
            EdgeToEdge.enable(this);
        } catch (Exception e) {
            WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            // Configure the behavior of the hidden system bars.
            windowInsetsController.setSystemBarsBehavior(BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        }
    }

}