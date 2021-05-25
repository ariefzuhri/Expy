package com.xdev.expy.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayoutMediator;
import com.xdev.expy.R;
import com.xdev.expy.data.source.remote.entity.ProductEntity;
import com.xdev.expy.databinding.ActivityMainBinding;
import com.xdev.expy.ui.main.about.AboutFragment;
import com.xdev.expy.ui.main.management.AddUpdateFragment;
import com.xdev.expy.ui.main.profile.ProfileFragment;
import com.xdev.expy.viewmodel.ViewModelFactory;

import static com.xdev.expy.utils.AppUtils.loadImage;
import static com.xdev.expy.utils.DateUtils.getCurrentDate;
import static com.xdev.expy.utils.DateUtils.getFormattedDate;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MainCallback {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MainPagerAdapter pagerAdapter = new MainPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) ->
                tab.setText(pagerAdapter.TAB_TITLES[position])).attach();

        binding.tvDate.setText(getFormattedDate(getCurrentDate(), false));

        binding.btnAbout.setOnClickListener(this);
        binding.civProfile.setOnClickListener(this);

        ViewModelFactory factory = ViewModelFactory.getInstance(getApplication());
        MainViewModel viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);
        viewModel.getUser().observe(this, user -> {
            if (user != null) {
                loadImage(this, binding.civProfile, user.getPhotoUrl());
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == binding.btnAbout.getId()){
            AboutFragment.newInstance().show(getSupportFragmentManager(), AboutFragment.TAG);
        } else if (id == binding.civProfile.getId()){
            ProfileFragment.newInstance().show(getSupportFragmentManager(), ProfileFragment.TAG);
        }
    }

    @Override
    public void addUpdateProduct(ProductEntity product) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom)
                .replace(binding.container.getId(), AddUpdateFragment.newInstance(product), AddUpdateFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        Fragment addUpdateFragment = getSupportFragmentManager().findFragmentByTag(AddUpdateFragment.TAG);
        if (addUpdateFragment != null && addUpdateFragment.isVisible()) {
            new AlertDialog.Builder(this)
                    .setTitle("Batalkan perubahan")
                    .setMessage("Kamu belum menyimpan perubahan. Apakah kamu yakin ingin membatalkannya?")
                    .setNeutralButton("Batal", null)
                    .setPositiveButton("Ya", (dialogInterface, i) ->
                            super.onBackPressed())
                    .create().show();
        } else {
            super.onBackPressed();
        }
    }

    public void setContainerBackground(boolean isAddUpdateFragmentVisible){
        if (isAddUpdateFragmentVisible) {
            binding.container.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        } else {
            binding.container.setBackgroundColor(Color.TRANSPARENT);
        }
    }
}