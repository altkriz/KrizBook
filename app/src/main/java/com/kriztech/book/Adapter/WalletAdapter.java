package com.kriztech.book.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Model.WalletHistoryModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Utils;

import java.util.List;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.MyViewHolder> {

    private List<Result> rewardList;
    Context mcontext;
    String currency_symbol;

    public WalletAdapter(Context context, List<Result> rewardList, String currency_symbol) {
        this.rewardList = rewardList;
        this.mcontext = context;
        this.currency_symbol = currency_symbol;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_title, txt_date, txt_desc, txt_price;

        public MyViewHolder(View view) {
            super(view);
            txt_title = view.findViewById(R.id.txt_title);
            txt_date = view.findViewById(R.id.txt_date);
            txt_desc = view.findViewById(R.id.txt_desc);
            txt_price = view.findViewById(R.id.txt_price);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.reward_item_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.txt_price.setText(currency_symbol + "" + rewardList.get(position).getAmount());
        holder.txt_title.setText("Payment ID :- " + rewardList.get(position).getPaymentId());
        holder.txt_date.setText("" + Utils.DateFormat2(rewardList.get(position).getCreatedAt()));
    }

    public void addData(List<Result> items) {
        this.rewardList.addAll(items);
        Log.e("rewardList", "" + rewardList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }

}