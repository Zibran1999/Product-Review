package com.pr.productkereview.activities;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pr.productkereview.R;
import com.pr.productkereview.activities.ui.main.SectionsPagerAdapter;
import com.pr.productkereview.databinding.ActivityHomeBinding;
import com.pr.productkereview.fragments.CategoryFragment;
import com.pr.productkereview.fragments.HomeFragment;
import com.pr.productkereview.fragments.TrendingProductFragment;
import com.pr.productkereview.models.UrlsModels.UrlModel;
import com.pr.productkereview.utils.ApiInterface;
import com.pr.productkereview.utils.ApiWebServices;
import com.pr.productkereview.utils.CommonMethods;
import com.pr.productkereview.utils.MyReceiver;

import java.io.UnsupportedEncodingException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String BroadCastStringForAction = "checkingInternet";
    private static final float END_SCALE = 0.7f;
    int count = 1;
    ImageView navMenu;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ConstraintLayout categoryContainer;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    String weburl, webUrlId;
    SectionsPagerAdapter sectionsPagerAdapter;
    ActivityHomeBinding binding;
    ApiInterface apiInterface;
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadCastStringForAction)) {
                if (intent.getStringExtra("online_status").equals("true")) {

                    Set_Visibility_ON();
                    count++;
                } else {
                    Set_Visibility_OFF();
                }
            }
        }
    };
    Bundle bundle;
    Dialog loading;
    FirebaseAnalytics mFirebaseAnalytics;
    private IntentFilter intentFilter;

    private void Set_Visibility_ON() {
        binding.lottieHomeNoInternet.setVisibility(View.GONE);
        binding.tvNotConnected.setVisibility(View.GONE);
        binding.viewPager.setVisibility(View.VISIBLE);
        binding.tabs.setVisibility(View.VISIBLE);
        enableNavItems();
        fetchUrls("tips");
        if (count == 2) {
            ViewPager viewPager = binding.viewPager;
            viewPager.setAdapter(sectionsPagerAdapter);
            TabLayout tabs = binding.tabs;
            tabs.setupWithViewPager(viewPager);
            navigationDrawer();
        }
    }

    private void Set_Visibility_OFF() {
        binding.lottieHomeNoInternet.setVisibility(View.VISIBLE);
        binding.tvNotConnected.setVisibility(View.VISIBLE);
        binding.viewPager.setVisibility(View.GONE);
        binding.tabs.setVisibility(View.GONE);
        disableNavItems();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiInterface = ApiWebServices.getApiInterface();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
        loading = CommonMethods.getLoadingDialog(HomeActivity.this);
        navigationView = binding.navigation;
        navMenu = binding.navMenu;
        drawerLayout = binding.drawerLayout;
        bundle = new Bundle();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        // Setting Version Code
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            String version = pInfo.versionName;
            binding.versionCode.setText(getString(R.string.version, version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //Internet Checking Condition
        intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastStringForAction);
        Intent serviceIntent = new Intent(this, MyReceiver.class);
        startService(serviceIntent);
        if (isOnline(HomeActivity.this)) {
            Set_Visibility_ON();
        } else {
            Set_Visibility_OFF();
        }

        binding.lottieContact.setOnClickListener(view -> {
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Contact Home Top");
            mFirebaseAnalytics.logEvent("Clicked_On_Contact_Home_Top", bundle);

            try {
                CommonMethods.whatsApp(HomeActivity.this);
            } catch (UnsupportedEncodingException | PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        });

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        sectionsPagerAdapter.addFragments(new HomeFragment(), "Home");
        sectionsPagerAdapter.addFragments(new CategoryFragment(), "Category");
        sectionsPagerAdapter.addFragments(new TrendingProductFragment(), "Trending");


    }

    public void fetchUrls(String tips) {
        Call<UrlModel> call = apiInterface.getUrls(tips);
        call.enqueue(new Callback<UrlModel>() {
            @Override
            public void onResponse(@NonNull Call<UrlModel> call, @NonNull Response<UrlModel> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    webUrlId = response.body().getId();
                    weburl = response.body().getUrl();
                    //Log.d("urls",weburl);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UrlModel> call, @NonNull Throwable t) {

            }
        });
    }

    public void navigationDrawer() {
        navigationView = findViewById(R.id.navigation);
        navigationView.bringToFront();
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                HomeActivity.this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(HomeActivity.this);
        navigationView.setCheckedItem(R.id.nav_home);
        categoryContainer = findViewById(R.id.container_layout);

        navMenu.setOnClickListener(view -> {
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        animateNavigationDrawer();
    }

    private void animateNavigationDrawer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            drawerLayout.setScrimColor(getColor(R.color.bg_color));
        }
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                // Scale the View based on current slide offset
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                categoryContainer.setScaleX(offsetScale);
                categoryContainer.setScaleY(offsetScale);

                // Translate the View, accounting for the scaled width
                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = categoryContainer.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                categoryContainer.setTranslationX(xTranslation);
            }
        });
    }

    public boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public void disableNavItems() {
        Menu navMenu = navigationView.getMenu();

        MenuItem nav_home = navMenu.findItem(R.id.nav_home);
        nav_home.setEnabled(false);

        MenuItem nav_expert_tips = navMenu.findItem(R.id.nav_expert_tips);
        nav_expert_tips.setEnabled(false);

        MenuItem nav_most_selling = navMenu.findItem(R.id.nav_most_selling);
        nav_most_selling.setEnabled(false);

        MenuItem nav_share = navMenu.findItem(R.id.nav_share);
        nav_share.setEnabled(false);

        MenuItem nav_rate = navMenu.findItem(R.id.nav_rate);
        nav_rate.setEnabled(false);

        MenuItem nav_contact = navMenu.findItem(R.id.nav_contact);
        nav_contact.setEnabled(false);

        MenuItem nav_favorite = navMenu.findItem(R.id.nav_contact);
        nav_favorite.setEnabled(false);

        MenuItem nav_policy = navMenu.findItem(R.id.nav_privacy);
        nav_policy.setEnabled(false);

        MenuItem nav_disclaimer = navMenu.findItem(R.id.nav_disclaimer);
        nav_disclaimer.setEnabled(false);

//        MenuItem nav_signOut = navMenu.findItem(R.id.nav_signOut);
//        nav_signOut.setEnabled(false);
    }

    public void enableNavItems() {
        Menu navMenu = navigationView.getMenu();

        MenuItem nav_home = navMenu.findItem(R.id.nav_home);
        nav_home.setEnabled(true);

        MenuItem nav_expert_tips = navMenu.findItem(R.id.nav_expert_tips);
        nav_expert_tips.setEnabled(true);

        MenuItem nav_most_selling = navMenu.findItem(R.id.nav_most_selling);
        nav_most_selling.setEnabled(true);

        MenuItem nav_share = navMenu.findItem(R.id.nav_share);
        nav_share.setEnabled(true);

        MenuItem nav_rate = navMenu.findItem(R.id.nav_rate);
        nav_rate.setEnabled(true);

        MenuItem nav_contact = navMenu.findItem(R.id.nav_contact);
        nav_contact.setEnabled(true);

        MenuItem nav_favorite = navMenu.findItem(R.id.nav_contact);
        nav_favorite.setEnabled(true);

        MenuItem nav_policy = navMenu.findItem(R.id.nav_privacy);
        nav_policy.setEnabled(true);

        MenuItem nav_disclaimer = navMenu.findItem(R.id.nav_disclaimer);
        nav_disclaimer.setEnabled(true);

//        MenuItem nav_signOut = navMenu.findItem(R.id.nav_signOut);
//        nav_signOut.setEnabled(true);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Home Menu");
                mFirebaseAnalytics.logEvent("Clicked_On_Home_Menu", bundle);

                break;

            case R.id.nav_expert_tips:
                openWebPage(weburl,HomeActivity.this);
                break;

            case R.id.nav_most_selling:
                Intent mostSellingIntent = new Intent(HomeActivity.this,ShowAllItemsActivity.class);
                mostSellingIntent.putExtra("key","mostSelling");
                startActivity(mostSellingIntent);
                break;

            case R.id.nav_share:
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Share Menu");
                mFirebaseAnalytics.logEvent("Clicked_On_Share_Menu", bundle);
                CommonMethods.shareApp(HomeActivity.this);
                break;

            case R.id.nav_rate:
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Rate Menu");
                mFirebaseAnalytics.logEvent("Clicked_On_Rate_Menu", bundle);
                CommonMethods.rateApp(HomeActivity.this);
                break;

            case R.id.nav_contact:
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Contact Menu");
                mFirebaseAnalytics.logEvent("Clicked_On_Contact_Menu", bundle);
                CommonMethods.contactUs(HomeActivity.this);
                break;

            case R.id.nav_privacy:
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Privacy Menu");
                mFirebaseAnalytics.logEvent("Clicked_On_Privacy_Menu", bundle);
                Intent intent = new Intent(HomeActivity.this, PrivacyPolicy.class);
                intent.putExtra("key", "policy");
                startActivity(intent);
                break;

            case R.id.nav_disclaimer:
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Disclaimer Menu");
                mFirebaseAnalytics.logEvent("Clicked_On_Disclaimer_Menu", bundle);
                disclaimerDialog();
                break;

//            case R.id.nav_signOut:
//                loading.show();
//                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "SignOut Menu");
//                mFirebaseAnalytics.logEvent("Clicked_On_SignOut_Menu", bundle);
//                // Sign Out for google user
//                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//                if (account != null) {
//                    googleSignOut();
//                }
//
//                break;
            default:
        }
        return true;
    }

    public void disclaimerDialog() {
        Dialog dialog = new Dialog(HomeActivity.this);
        dialog.setContentView(R.layout.disclaimer_layout);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);
        dialog.show();
    }

//    public void googleSignOut() {
//        gsc.signOut().addOnCompleteListener(task -> {
//            finish();
//            loading.dismiss();
//            startActivity(new Intent(HomeActivity.this, SignupActivity.class));
//        });
//    }

    @Override
    protected void onRestart() {
        super.onRestart();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // IronSource.onPause(this);
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // IronSource.onResume(this);
        registerReceiver(receiver, intentFilter);
    }

    @SuppressLint("QueryPermissionsNeeded")
    public void openWebPage(String url, Context context) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            startActivity(new Intent(HomeActivity.this, WelcomeActivity.class));
            super.onBackPressed();
            // ads.destroyBanner();
        }
    }
}