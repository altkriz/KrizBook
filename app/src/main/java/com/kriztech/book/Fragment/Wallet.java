package com.kriztech.book.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.kriztech.book.Activity.MainActivity;
import com.kriztech.book.Activity.Subscription;
import com.kriztech.book.Adapter.TabPagerAdapter;
import com.kriztech.book.Model.ProfileModel.ProfileModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeBannerAd;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.material.tabs.TabLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Wallet extends Fragment implements View.OnClickListener {

    public Wallet() {
    }

    private PrefManager prefManager;
    private ShimmerFrameLayout shimmer;

    private TabLayout tabLayout;
    private ViewPager tab_viewpager;
    private TextView txtPoints;
    private LinearLayout lyAddMoney;

    private TemplateView nativeTemplate = null;
    private NativeBannerAd fbNativeBannerAd = null;
    private NativeAdLayout fbNativeTemplate = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wallet, container, false);
        PrefManager.forceRTLIfSupported(getActivity().getWindow(), getActivity());

        init(root);
        AdInit();

        return root;
    }

    private void init(View root) {
        try {
            prefManager = new PrefManager(getActivity());
            shimmer = root.findViewById(R.id.shimmer);
            MainActivity.appbar.setVisibility(View.VISIBLE);

            nativeTemplate = root.findViewById(R.id.nativeTemplate);
            fbNativeTemplate = root.findViewById(R.id.fbNativeTemplate);

            lyAddMoney = root.findViewById(R.id.lyAddMoney);
            txtPoints = root.findViewById(R.id.txtPoints);

            tabLayout = root.findViewById(R.id.tabLayout);
            tab_viewpager = root.findViewById(R.id.tab_viewpager);
            setupViewPager(tab_viewpager);
            tabLayout.setupWithViewPager(tab_viewpager);
            tab_viewpager.setOffscreenPageLimit(1);

            lyAddMoney.setOnClickListener(this);
        } catch (Exception e) {
            Log.e("init Exception =>", "" + e);
        }
    }

    private void AdInit() {
        Log.e("native_ad", "" + prefManager.getValue("native_ad"));
        if (prefManager.getValue("native_ad").equalsIgnoreCase("yes")) {
            nativeTemplate.setVisibility(View.VISIBLE);
            Utils.NativeAds(getActivity(), nativeTemplate, "" + prefManager.getValue("native_adid"));
        } else {
            nativeTemplate.setVisibility(View.GONE);
        }

        Log.e("fb_native_status", "" + prefManager.getValue("fb_native_status"));
        if (prefManager.getValue("fb_native_status").equalsIgnoreCase("on")) {
            fbNativeTemplate.setVisibility(View.VISIBLE);
            Utils.FacebookNativeAdSmall(getActivity(), fbNativeBannerAd, fbNativeTemplate, "" + prefManager.getValue("fb_native_id"));
        } else {
            fbNativeTemplate.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lyAddMoney:
                Package();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        GetProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void GetProfile() {
        Utils.shimmerShow(shimmer);

        Call<ProfileModel> call = BaseURL.getVideoAPI().profile("" + prefManager.getLoginId());
        call.enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("==>profile", "" + response.body().getResult());
                        txtPoints.setText("" + response.body().getResult().get(0).getCoinBalance());

                        Utils.storeUserCred(getActivity(),
                                "" + response.body().getResult().get(0).getId(),
                                "" + response.body().getResult().get(0).getType(),
                                "" + response.body().getResult().get(0).getEmail(),
                                "" + response.body().getResult().get(0).getFullname(),
                                "" + response.body().getResult().get(0).getMobile());
                    }
                } catch (Exception e) {
                    Log.e("profile", "Exception => " + e);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
                Log.e("profile", "onFailure => " + t.getMessage());
                Utils.shimmerHide(shimmer);
            }
        });
    }

    //Tab With ViewPager
    private void setupViewPager(ViewPager viewPager) {
        TabPagerAdapter adapter = new TabPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new WalletHistory(), "" + getResources().getString(R.string.wallet_history));
        adapter.addFragment(new TransactionHistory(), "" + getResources().getString(R.string.transaction_history));
        viewPager.setAdapter(adapter);
    }

    private void Package() {
        startActivity(new Intent(getActivity(), Subscription.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.shimmerHide(shimmer);
        if (fbNativeBannerAd != null) {
            fbNativeBannerAd.destroy();
        }
    }

}
