package software.enginer.litterallyless.util;

import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationBarView;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NavItemSectionListener implements NavigationBarView.OnItemSelectedListener {

    private final HashMap<Integer, ConditionalFunction> itemSelectionMap = new HashMap<>();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        ConditionalFunction function = itemSelectionMap.get(itemId);
        if (function != null){
            return function.run();
        }
        return false;
    }

    public NavItemSectionListener addMapping(int i, ConditionalFunction c){
        itemSelectionMap.put(i, c);
        return this;
    }

}
