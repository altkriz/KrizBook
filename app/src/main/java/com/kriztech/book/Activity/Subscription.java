package com.kriztech.book.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Adapter.PackageAdapter;
import com.kriztech.book.Model.PackageModel.PackageModel;
import com.kriztech.book.Model.PackageModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Interface.ItemClickListener;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Subscription extends AppCompatActivity implements ItemClickListener {

    private PrefManager prefManager;

    private ShimmerFrameLayout shimmer;
    private TextView txtToolbarTitle;
    private LinearLayout lyToolbar, lyBack, lyNoData, lyAddCustomAmount, lyAddMoney;
    private EditText etAmount;
    private RecyclerView rvPackage;

    private List<Result> packageList;
    private PackageAdapter packageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(Subscription.this);
        setContentView(R.layout.activity_subscription);
        PrefManager.forceRTLIfSupported(getWindow(), Subscription.this);

        prefManager = new PrefManager(Subscription.this);

        shimmer = findViewById(R.id.shimmer);
        lyToolbar = findViewById(R.id.lyToolbar);
        lyBack = findViewById(R.id.lyBack);
        txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
        rvPackage = findViewById(R.id.rvPackage);
        lyNoData = findViewById(R.id.lyNoData);
        etAmount = findViewById(R.id.etAmount);
        lyAddCustomAmount = findViewById(R.id.lyAddCustomAmount);
        lyAddMoney = findViewById(R.id.lyAddMoney);

        GetPackage();

        txtToolbarTitle.setText("" + getResources().getString(R.string.my_package));
        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    lyAddMoney.setVisibility(View.VISIBLE);
                } else {
                    lyAddMoney.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        lyAddMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.checkMissingData(Subscription.this, "" + prefManager.getValue("userType"))) {
                    Intent intent = new Intent(Subscription.this, AllPaymentActivity.class);
                    intent.putExtra("TYPE", "Package");
                    intent.putExtra("itemId", "");
                    intent.putExtra("title", "Default");
                    intent.putExtra("productPackage", "");
                    intent.putExtra("price", "" + etAmount.getText().toString().trim());
                    intent.putExtra("desc", "Default amount");
                    intent.putExtra("date", "" + (new Date(System.currentTimeMillis()).getDate()));
                    startActivity(intent);
                    finish();
                } else {
                    Utils.getMissingDataFromUser(Subscription.this, "" + prefManager.getValue("userType"));
                }
            }
        });

    }

    /* get_package API */
    private void GetPackage() {
        Utils.shimmerShow(shimmer);

        Call<PackageModel> call = BaseURL.getVideoAPI().get_package();
        call.enqueue(new Callback<PackageModel>() {
            @Override
            public void onResponse(Call<PackageModel> call, Response<PackageModel> response) {
                try {
                    Log.e("get_package", "Status => " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult() != null) {
                            if (response.body().getResult().size() > 0) {
                                packageList = new ArrayList<>();
                                packageList = response.body().getResult();
                                Log.e("==>packageList", "" + packageList.size());

                                packageAdapter = new PackageAdapter(Subscription.this, packageList, "activity", Subscription.this, prefManager.getValue("currency_symbol"));
                                rvPackage.setLayoutManager(new GridLayoutManager(Subscription.this, 1));
                                rvPackage.setAdapter(packageAdapter);
                                packageAdapter.notifyDataSetChanged();

                                rvPackage.setVisibility(View.VISIBLE);
                                lyNoData.setVisibility(View.GONE);
                            } else {
                                rvPackage.setVisibility(View.GONE);
                                lyNoData.setVisibility(View.VISIBLE);
                            }
                        } else {
                            rvPackage.setVisibility(View.GONE);
                            lyNoData.setVisibility(View.VISIBLE);
                            Log.e("get_package", "Message => " + response.body().getMessage());
                        }

                    } else {
                        rvPackage.setVisibility(View.GONE);
                        lyNoData.setVisibility(View.VISIBLE);
                        Log.e("get_package", "Message => " + response.body().getMessage());
                    }
                } catch (Exception e) {
                    Log.e("get_package", "Exception => " + e);
                    rvPackage.setVisibility(View.GONE);
                    lyNoData.setVisibility(View.VISIBLE);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<PackageModel> call, Throwable t) {
                Utils.shimmerHide(shimmer);
                rvPackage.setVisibility(View.GONE);
                lyNoData.setVisibility(View.VISIBLE);
                Log.e("get_package", "onFailure => " + t.getMessage());
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Log.e("==>payckage_id", "" + packageList.get(position).getId());
        Log.e("==>amount", "" + packageList.get(position).getPrice());
        Log.e("==>short_description", "" + packageList.get(position).getPackageName());
        Log.e("==>currency_code", "" + prefManager.getValue("currency_code"));

        if (Utils.checkMissingData(Subscription.this, "" + prefManager.getValue("userType"))) {
            Intent intent = new Intent(Subscription.this, AllPaymentActivity.class);
            intent.putExtra("TYPE", "Package");
            intent.putExtra("itemId", "" + packageList.get(position).getId());
            intent.putExtra("title", "" + packageList.get(position).getPackageName());
            intent.putExtra("productPackage", "" + packageList.get(position).getProductPackage());
            intent.putExtra("price", "" + packageList.get(position).getPrice());
            intent.putExtra("desc", "" + packageList.get(position).getPackageName());
            intent.putExtra("date", "" + packageList.get(position).getCreatedAt());
            startActivity(intent);
            finish();
        } else {
            Utils.getMissingDataFromUser(Subscription.this, "" + prefManager.getValue("userType"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.shimmerHide(shimmer);
    }

}
