package com.keithvongola.android.moneydiary.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.facebook.stetho.Stetho;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.keithvongola.android.moneydiary.Backable;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.Utility;
import com.keithvongola.android.moneydiary.service.ExRateTaskService;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.Utility.FRAG_TAG_EDIT;
import static com.keithvongola.android.moneydiary.Utility.FRAG_TAG_NAV;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        FragmentManager.OnBackStackChangedListener{
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.appbar) AppBarLayout appBarLayout;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;

    private FragmentManager fragmentManager;
    private ActionBarDrawerToggle toggle;
    boolean isConnected;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_main_container, new MainFragment(),FRAG_TAG_NAV)
                    .commit();
        }

        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        if (savedInstanceState == null) {
            if (isConnected) {
                Utility.startExRateIntent(this);
                startPeriodicTask();
            }
        }

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                finish();
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;
        Class fragmentClass = null;

        switch (item.getItemId()) {
            case R.id.nav_home:
                fragmentClass = MainFragment.class;
                break;
            case R.id.nav_accounts:
                fragmentClass = AccountsFragment.class;
                break;
            case R.id.nav_budget:
                fragmentClass = BudgetsFragment.class;
                break;
            case R.id.nav_plan:
                fragmentClass = PlansFragment.class;
                break;
            case R.id.nav_records:
              fragmentClass = RecordsFragment.class;
                break;
            case R.id.nav_reports:
                fragmentClass = ReportsFragment.class;
                break;
            case R.id.nav_setting:
                fragmentClass = SettingsFragment.class;
                break;
        }

        if (fragmentManager.getBackStackEntryCount() == 0) {
            Class currentFragmentClass = fragmentManager.findFragmentByTag(FRAG_TAG_NAV).getClass();
            if (currentFragmentClass.equals(fragmentClass)) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        fragmentManager.beginTransaction()
        .setCustomAnimations(R.anim.slide_in_bottom, R.anim.abc_fade_out)
        .replace(R.id.content_main_container,fragment,FRAG_TAG_NAV)
        .commit();

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setDrawerEnabled(boolean enabled) {
        int lockMode = enabled ? DrawerLayout.LOCK_MODE_UNLOCKED :
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        drawer.setDrawerLockMode(lockMode);
        toggle.setDrawerIndicatorEnabled(enabled);
    }

    public void startPeriodicTask(){
        PeriodicTask periodicTask = new PeriodicTask.Builder()
                .setService(ExRateTaskService.class)
                .setPeriod(43200L)
                .setFlex(3600L)
                .setTag("periodic")
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setRequiresCharging(false)
                .build();

        GcmNetworkManager.getInstance(this).schedule(periodicTask);
    }

    @Override
    public void onBackStackChanged() {
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        if(backStackCount > 0){
            setDrawerEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragmentManager.popBackStack();
                }
            });
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            setDrawerEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        if (fragmentManager.getBackStackEntryCount() == 0) {
            Fragment currentFragment = fragmentManager.findFragmentByTag(FRAG_TAG_NAV);
            if (currentFragment != null && currentFragment instanceof Backable) {
                if(currentFragment instanceof AccountsFragment) {
                    if (!((AccountsFragment) currentFragment).onBackPressed())
                        return;
                } else if (currentFragment instanceof BudgetsFragment){
                    if (!((BudgetsFragment) currentFragment).onBackPressed())
                        return;
                } else if (currentFragment instanceof PlansFragment){
                    if (!((PlansFragment) currentFragment).onBackPressed())
                        return;
                } else if (currentFragment instanceof RecordsFragment){
                    if (!((RecordsFragment) currentFragment).onBackPressed())
                        return;
                }

                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.abc_fade_in, R.anim.slide_out_bottom)
                        .replace(R.id.content_main_container, new MainFragment(), FRAG_TAG_NAV)
                        .commit();
                navigationView.setCheckedItem(R.id.nav_home);
                return;
            } else if (currentFragment instanceof MainFragment) {
                if (!((MainFragment) currentFragment).collapse())
                    if (mInterstitialAd.isLoaded()) mInterstitialAd.show();
                    else finish();
                return;
            }
        } else {
            Fragment currentFragment = fragmentManager.findFragmentByTag(FRAG_TAG_EDIT);
            if (currentFragment instanceof AccountEditFragment){
                if (((AccountEditFragment) currentFragment).popupWindowIsShowing())
                    return;
            } else if (currentFragment instanceof BudgetsEditFragment){
                if (((BudgetsEditFragment) currentFragment).popupWindowIsShowing())
                    return;
            } else if (currentFragment instanceof ExRateEditFragment){
                if (((ExRateEditFragment) currentFragment).popupWindowIsShowing())
                    return;
            } else if (currentFragment instanceof ExpenseEditFragment){
                if (((ExpenseEditFragment) currentFragment).popupWindowIsShowing())
                    return;
            } else if (currentFragment instanceof IncomeEditFragment){
                if (((IncomeEditFragment) currentFragment).popupWindowIsShowing())
                    return;
            } else if (currentFragment instanceof TransferEditFragment){
                if (((TransferEditFragment) currentFragment).popupWindowIsShowing())
                    return;
            } else if (currentFragment instanceof SavingEditFragment){
                if (((SavingEditFragment) currentFragment).popupWindowIsShowing())
                    return;
            } else if (currentFragment instanceof PlansEditFragment){
                if (((PlansEditFragment) currentFragment).popupWindowIsShowing())
                    return;
            } else if (currentFragment instanceof BudgetsListFragment){
                if (!((BudgetsListFragment) currentFragment).onBackPressed())
                    return;
            } else if (currentFragment instanceof SubPlansFragment){
                if (!((SubPlansFragment) currentFragment).onBackPressed())
                    return;
            }
        }
        super.onBackPressed();
    }
}

