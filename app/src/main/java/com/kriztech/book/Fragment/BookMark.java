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

public class BookMark extends Fragment {

    private PrefManager prefManager;

    private SmartTabLayout tab_layout;
    private ViewPager tab_viewpager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bookmark, container, false);
        PrefManager.forceRTLIfSupported(getActivity().getWindow(), getActivity());

        MainActivity.appbar.setVisibility(View.VISIBLE);
        prefManager = new PrefManager(getActivity());

        tab_layout = root.findViewById(R.id.tab_layout);
        tab_viewpager = root.findViewById(R.id.tab_viewpager);

        setupViewPager(tab_viewpager);
        tab_layout.setViewPager(tab_viewpager);
        tab_viewpager.setOffscreenPageLimit(1);

        return root;
    }

    //Tab With ViewPager
    private void setupViewPager(ViewPager viewPager) {
        TabPagerAdapter adapter = new TabPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new BookMarkBooks(), "" + getResources().getString(R.string.books));
        adapter.addFragment(new BookMarkMagazines(), "" + getResources().getString(R.string.magazines));
        viewPager.setAdapter(adapter);
    }

}