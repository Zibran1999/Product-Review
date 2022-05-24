package com.pr.productkereview.fragments;

import static com.pr.productkereview.activities.ItemDetailsActivity.productModel;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.pr.productkereview.R;
import com.pr.productkereview.databinding.FragmentRatingsBinding;
import com.pr.productkereview.utils.Prevalent;
import com.pr.productkereview.utils.ShowAds;

import io.paperdb.Paper;

public class RatingsFragment extends Fragment {
    FragmentRatingsBinding binding;
    MaterialButtonToggleGroup materialButtonToggleGroup;
    ShowAds showAds = new ShowAds();

    @SuppressLint("NonConstantResourceId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRatingsBinding.inflate(inflater, container, false);
        materialButtonToggleGroup = binding.materialButtonToggleGroup;
//        Glide.with(requireActivity()).load(ApiWebServices.base_url + "all_products_images/"
//                        + productModel.getBanner())
//                .placeholder(R.drawable.banner_picture)
//                .into(binding.ratingBannerImg);

        setShowAds();
        binding.ratingWebView.loadData(productModel.getRatingEnglish(), "text/html", "UTF-8");
        materialButtonToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                switch (checkedId) {
                    case R.id.ratingEnglishPreview:
                        setShowAds();
                        binding.ratingWebView.loadData(productModel.getRatingEnglish(), "text/html", "UTF-8");
                        break;
                    case R.id.ratingHindiPreview:
                        setShowAds();
                        binding.ratingWebView.loadData(productModel.getRatingHindi(), "text/html", "UTF-8");
                        break;

                }
            }
        });
        return binding.getRoot();
    }

    void setShowAds() {
        getLifecycle().addObserver(showAds);
        if (Paper.book().read(Prevalent.bannerTopNetworkName).equals("IronSourceWithMeta")) {
            binding.adViewTop.setVisibility(View.GONE);
            showAds.showBottomBanner(requireActivity(), binding.adViewBottom);

        } else if (Paper.book().read(Prevalent.bannerBottomNetworkName).equals("IronSourceWithMeta")) {
            binding.adViewBottom.setVisibility(View.GONE);
            showAds.showTopBanner(requireActivity(), binding.adViewTop);

        } else {
            showAds.showTopBanner(requireActivity(), binding.adViewTop);
            showAds.showBottomBanner(requireActivity(), binding.adViewBottom);
        }
    }
}