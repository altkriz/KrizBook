package com.kriztech.book.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Interface.ItemClickListener;
import com.kriztech.book.Model.PackageModel.Result;
import com.kriztech.book.R;

import java.util.List;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.MyViewHolder> {

    private List<Result> packageList;
    private Context mcontext;
    private String type, currencySymbol;
    private ItemClickListener itemClickListener;

    public PackageAdapter(Context context, List<Result> packageList, String type, ItemClickListener itemClickListener, String currencySymbol) {
        this.mcontext = context;
        this.packageList = packageList;
        this.type = type;
        this.itemClickListener = itemClickListener;
        this.currencySymbol = currencySymbol;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rlBg;
        LinearLayout ly_package;
        TextView txt_title, txt_price;
        Button btnGet;
        ImageView ivCoin;

        public MyViewHolder(View view) {
            super(view);
            rlBg = view.findViewById(R.id.rlBg);
            ly_package = view.findViewById(R.id.ly_package);
            ivCoin = view.findViewById(R.id.ivCoin);
            btnGet = view.findViewById(R.id.btnGet);
            txt_title = view.findViewById(R.id.txt_title);
            txt_price = view.findViewById(R.id.txt_price);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (type.equalsIgnoreCase("activity")) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.package_item_final, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.package_item_frg, parent, false);
        }
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.txt_title.setText("" + packageList.get(position).getPackageName());
        holder.txt_price.setText(currencySymbol + "" + packageList.get(position).getPrice());

        holder.ly_package.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("click", "" + position);
                itemClickListener.onItemClick(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return packageList.size();
    }

}