package com.kriztech.book.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.kriztech.book.Activity.MainActivity;
import com.kriztech.book.Adapter.TabPagerAdapter;
import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

public class Featured extends Fragment {

    private View root;

    SmartTabLayout tab_layout;
    ViewPager tab_viewpager;

    public Featured() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_featured, container, false);
        PrefManager.forceRTLIfSupported(getActivity().getWindow(), getActivity());

        MainActivity.appbar.setVisibility(View.VISIBLE);
        tab_layout = root.findViewById(R.id.tab_layout);
        tab_viewpager = root.findViewById(R.id.tab_viewpager);

        setupViewPager(tab_viewpager);
        tab_layout.setViewPager(tab_viewpager);
        tab_viewpager.setOffscreenPageLimit(2);

        return root;
    }

    //Tab With ViewPager
    private void setupViewPager(ViewPager viewPager) {
        TabPagerAdapter adapter = new TabPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new Home(), "" + getResources().getString(R.string.title_home));
        adapter.addFragment(new Genres(), "" + getResources().getString(R.string.title_genres));
        adapter.addFragment(new Magazines(), "" + getResources().getString(R.string.title_magazine));
        viewPager.setAdapter(adapter);
    }

}