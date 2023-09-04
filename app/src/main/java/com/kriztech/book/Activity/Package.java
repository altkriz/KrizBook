package com.kriztech.book.Activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Adapter.PackageAdapter;
import com.kriztech.book.Interface.ItemClickListener;
import com.kriztech.book.Model.PackageModel.PackageModel;
import com.kriztech.book.Model.PackageModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Package extends BottomSheetDialogFragment implements View.OnClickListener, ItemClickListener {

    private PrefManager prefManager;
    private BottomSheetDialog dialog;

    private RecyclerView rvPackage;
    private List<Result> packageList;
    private PackageAdapter packageAdapter;

    private EditText etAmount;
    private LinearLayout lyAddMoney, lyAddCustomAmount;

    private BottomSheetBehavior mBehavior;

    public static Package newInstance() {
        return new Package();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        Utils.setTheme(getActivity());
        View view = View.inflate(getContext(), R.layout.package_dialog, null);

        PrefManager.forceRTLIfSupported(getActivity().getWindow(), getActivity());
        prefManager = new PrefManager(getActivity());
        rvPackage = view.findViewById(R.id.rvPackage);

        lyAddMoney = view.findViewById(R.id.lyAddMoney);
        lyAddCustomAmount = view.findViewById(R.id.lyAddCustomAmount);
        etAmount = view.findViewById(R.id.etAmount);

        GetPackage();

        dialog.setContentView(view);
        mBehavior = BottomSheetBehavior.from((View) view.getParent());

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
                Intent intent = new Intent(getActivity(), AllPaymentActivity.class);
                intent.putExtra("TYPE", "Package");
                intent.putExtra("itemId", "");
                intent.putExtra("productPackage", "");
                intent.putExtra("title", "Default");
                intent.putExtra("price", "" + etAmount.getText().toString().trim());
                intent.putExtra("desc", "Default amount");
                intent.putExtra("date", "" + (new Date(System.currentTimeMillis()).getDate()));
                startActivity(intent);
                dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Utils.ProgressbarHide();
            }
        });

        return dialog;
    }

    /* get_package API */
    private void GetPackage() {
        Utils.ProgressBarShow(getActivity());

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

                                packageAdapter = new PackageAdapter(getActivity(), packageList, "fragment", Package.this, prefManager.getValue("currency_symbol"));
                                rvPackage.setLayoutManager(new GridLayoutManager(getActivity(), 1));
                                rvPackage.setAdapter(packageAdapter);
                                packageAdapter.notifyDataSetChanged();

                                rvPackage.setVisibility(View.VISIBLE);
                            } else {
                                rvPackage.setVisibility(View.GONE);
                            }

                        } else {
                            rvPackage.setVisibility(View.GONE);
                            Log.e("get_package", "Message => " + response.body().getMessage());
                        }

                    } else {
                        rvPackage.setVisibility(View.GONE);
                        Log.e("get_package", "Message => " + response.body().getMessage());
                    }
                } catch (Exception e) {
                    Log.e("get_package", "Exception => " + e);
                    rvPackage.setVisibility(View.GONE);
                }
                Utils.ProgressbarHide();
            }

            @Override
            public void onFailure(Call<PackageModel> call, Throwable t) {
                Log.e("get_package", "onFailure => " + t.getMessage());
                rvPackage.setVisibility(View.GONE);
                Utils.ProgressbarHide();
            }
        });
    }

    @Override
    public void onClick(View v) {
        Utils.ProgressbarHide();
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.setDismissWithAnimation(true);
                dialog.dismiss();
            }
        }
    }

    @Override
    public void onItemClick(int position) {
        Log.e("==>payckage_id", "" + packageList.get(position).getId());
        Log.e("==>amount", "" + packageList.get(position).getPrice());
        Log.e("==>short_description", "" + packageList.get(position).getPackageName());
        Log.e("==>currency_code", "" + prefManager.getValue("currency_code"));

        Intent intent = new Intent(getActivity(), AllPaymentActivity.class);
        intent.putExtra("TYPE", "Package");
        intent.putExtra("itemId", "" + packageList.get(position).getId());
        intent.putExtra("title", "" + packageList.get(position).getPackageName());
        intent.putExtra("productPackage", "" + packageList.get(position).getProductPackage());
        intent.putExtra("price", "" + packageList.get(position).getPrice());
        intent.putExtra("desc", "" + packageList.get(position).getPackageName());
        intent.putExtra("date", "" + packageList.get(position).getCreatedAt());
        startActivity(intent);
        dismiss();
    }

}
