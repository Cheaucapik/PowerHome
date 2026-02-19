package iut.dam.powerhome;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerDL;
    private ActionBarDrawerToggle toggle;
    private FragmentManager fm;
    private NavigationView navNV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerDL = findViewById(R.id.drawer);
        navNV = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        fm = getSupportFragmentManager();

        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(this, drawerDL, toolbar, R.string.open, R.string.close);
        drawerDL.addDrawerListener(toggle);
        toggle.syncState();


        navNV.setNavigationItemSelectedListener(this);
        if (savedInstanceState == null) {
            navNV.getMenu().performIdentifierAction(R.id.nav_first, 0);
        }

        navNV.setItemIconTintList(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return toggle.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        if (item.getItemId() == R.id.nav_first){
            fm.beginTransaction().replace(R.id.contentFL,
                    new HabitatsFragment()).commit();
            setTitle(R.string.Accommodations);
        } else if (item.getItemId() == R.id.nav_second) {
            fm.beginTransaction().replace(R.id.contentFL,
                    new MonHabitatFragment()).commit();
        }
        else if (item.getItemId() == R.id.nav_third) {
            fm.beginTransaction().replace(R.id.contentFL,
                    new MonHabitatFragment()).commit();
        }

        drawerDL.closeDrawer(GravityCompat.START);
        return true;
    }
}
