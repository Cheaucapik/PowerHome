package iut.dam.powerhome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import org.w3c.dom.Text;

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

        View headerView = navNV.getHeaderView(0);
        TextView name_tv = headerView.findViewById(R.id.name_tv);
        TextView mail_tv = headerView.findViewById(R.id.mail_tv);

        SharedPreferences sp = this.getSharedPreferences("UserSession", MODE_PRIVATE);
        String nom = sp.getString("firstname", "") + " " + sp.getString("lastname", "");
        name_tv.setText(nom);

        mail_tv.setText(sp.getString("email", ""));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return toggle.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        int id = item.getItemId();
        item.setChecked(true);

        if (id == R.id.nav_first){
            fm.beginTransaction().replace(R.id.contentFL,
                    new HabitatsFragment()).commit();
            setTitle(R.string.Accommodations);
        } else if (id == R.id.nav_second) {
            fm.beginTransaction().replace(R.id.contentFL,
                    new MonHabitatFragment()).commit();
            setTitle(R.string.myAccomodation);
        }
        else if (id == R.id.nav_third) {
            fm.beginTransaction().replace(R.id.contentFL,
                    new RequetesFragment()).commit();
            setTitle(R.string.request);
        }
        else if (id == R.id.nav_forth) {
            fm.beginTransaction().replace(R.id.contentFL,
                    new ParamFragment()).commit();
            setTitle(R.string.Settings);
        }
        else if (id == R.id.nav_fifth) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);

            startActivity(intent);
            finish();
        }

        drawerDL.closeDrawer(GravityCompat.START);
        return true;
    }
}
