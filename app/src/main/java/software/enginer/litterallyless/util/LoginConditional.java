package software.enginer.litterallyless.util;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class LoginConditional extends ConditionalFunction {
    private final View view;
    private final BooleanSupplier isLoggedIn;

    public LoginConditional(View view, BooleanSupplier isLoggedIn) {
        this.view = view;

        this.isLoggedIn = isLoggedIn;
    }

    @Override
    public void onFailure() {
        Snackbar.make(
                view,
                "Please Login First",
                Snackbar.LENGTH_SHORT
        ).setAnchorView(view).show();
    }

    @Override
    public boolean evaluate() {
        return isLoggedIn.getAsBoolean();
    }
}
