package com.kriztech.book.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.kriztech.book.Adapter.TabPagerAdapter;
import com.kriztech.book.Fragment.SearchBooks;
import com.kriztech.book.Fragment.SearchMagazines;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Constant;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

public class SearchActivity extends AppCompatActivity {

    private PrefManager prefManager;

    private SmartTabLayout tab_layout;
    private ViewPager tab_viewpager;
    private EditText searchView;
    private ImageButton IB_clear, IB_back;

    private String strSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(SearchActivity.this);
        setContentView(R.layout.activity_search);
        PrefManager.forceRTLIfSupported(getWindow(), SearchActivity.this);

        prefManager = new PrefManager(SearchActivity.this);

        IB_clear = findViewById(R.id.buttonClear);
        IB_back = findViewById(R.id.buttonBack);
        IB_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        IB_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setText("");
            }
        });

        searchView = findViewById(R.id.searchEditText);
        tab_layout = findViewById(R.id.tab_layout);
        tab_viewpager = findViewById(R.id.tab_viewpager);

        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    strSearch = "" + searchView.getText().toString();
                    Log.e("strSearch", "" + strSearch);
                    Constant.strSearch = strSearch;

                    setupViewPager(tab_viewpager);
                    tab_layout.setViewPager(tab_viewpager);
                    tab_viewpager.setOffscreenPageLimit(1);
                    return true;
                }
                return false;
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            strSearch = bundle.getString("search");
            Log.e("strSearch", "" + strSearch);
            searchView.setText(strSearch);
            Constant.strSearch = strSearch;

            setupViewPager(tab_viewpager);
            tab_layout.setViewPager(tab_viewpager);
            tab_viewpager.setOffscreenPageLimit(1);
        }

    }

    //Tab With ViewPager
    private void setupViewPager(ViewPager viewPager) {
        TabPagerAdapter adapter = new TabPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SearchBooks(), "" + getResources().getString(R.string.books));
        adapter.addFragment(new SearchMagazines(), "" + getResources().getString(R.string.magazines));
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
//        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_search:
                // search action
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
