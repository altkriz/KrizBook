package com.kriztech.book.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.kriztech.book.Adapter.TabPagerAdapter;
import com.kriztech.book.Fragment.PurchaseBooks;
import com.kriztech.book.Fragment.PurchaseMagazines;
import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

public class MyPurchase extends AppCompatActivity {

    private PrefManager prefManager;

    private SmartTabLayout tab_layout;
    private ViewPager tab_viewpager;
    private TextView txtToolbarTitle;
    private LinearLayout lyToolbar, lyBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(MyPurchase.this);
        setContentView(R.layout.activity_mypurchase);
        PrefManager.forceRTLIfSupported(getWindow(), MyPurchase.this);

        init();

        txtToolbarTitle.setText("" + getResources().getString(R.string.My_Purchase_Books));
        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void init() {
        try {
            prefManager = new PrefManager(MyPurchase.this);

            lyToolbar = findViewById(R.id.lyToolbar);
            lyBack = findViewById(R.id.lyBack);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);

            tab_layout = findViewById(R.id.tab_layout);
            tab_viewpager = findViewById(R.id.tab_viewpager);

            setupViewPager(tab_viewpager);
            tab_layout.setViewPager(tab_viewpager);
            tab_viewpager.setOffscreenPageLimit(1);
        } catch (Exception e) {
            Log.e("init Exception =>", "" + e);
        }
    }

    //Tab With ViewPager
    private void setupViewPager(ViewPager viewPager) {
        TabPagerAdapter adapter = new TabPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PurchaseBooks(), "" + getResources().getString(R.string.books));
        adapter.addFragment(new PurchaseMagazines(), "" + getResources().getString(R.string.magazines));
        viewPager.setAdapter(adapter);
    }

}
