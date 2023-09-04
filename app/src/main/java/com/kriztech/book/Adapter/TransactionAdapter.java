package com.kriztech.book.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Model.TransactionModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyViewHolder> {

    private List<Result> transactionList;
    Context mcontext;
    String currency_symbol;

    public TransactionAdapter(Context context, List<Result> transactionList, String currency_symbol) {
        this.transactionList = transactionList;
        this.mcontext = context;
        this.currency_symbol = currency_symbol;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtTitle, txtDate, txtAuthor, txtCoins;
        ImageView ivThumb;

        public MyViewHolder(View view) {
            super(view);
            txtTitle = view.findViewById(R.id.txtTitle);
            txtDate = view.findViewById(R.id.txtDate);
            txtAuthor = view.findViewById(R.id.txtAuthor);
            txtCoins = view.findViewById(R.id.txtCoins);
            ivThumb = view.findViewById(R.id.ivThumb);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.withdrawal_item_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        if (transactionList.get(position).getBookId().equalsIgnoreCase("0")) {
            if (transactionList.get(position).getMagazineDetail() != null) {

                if (!TextUtils.isEmpty(transactionList.get(position).getMagazineDetail().getImage()))
                    Picasso.get().load(transactionList.get(position).getMagazineDetail().getImage()).into(holder.ivThumb);

                holder.txtTitle.setText(transactionList.get(position).getMagazineDetail().getTitle());
                holder.txtCoins.setText(currency_symbol + " " + transactionList.get(position).getMagazineDetail().getPrice());
            }
        } else {
            if (transactionList.get(position).getBookDetail() != null) {

                if (!TextUtils.isEmpty(transactionList.get(position).getBookDetail().getImage()))
                    Picasso.get().load(transactionList.get(position).getBookDetail().getImage()).into(holder.ivThumb);

                holder.txtTitle.setText(transactionList.get(position).getBookDetail().getTitle());
                holder.txtCoins.setText(currency_symbol + " " + transactionList.get(position).getBookDetail().getPrice());
            }
        }
        holder.txtAuthor.setText(transactionList.get(position).getAuthorName());
        holder.txtDate.setText("" + Utils.DateFormat3(transactionList.get(position).getTransactionDate()));
    }

    public void addData(List<Result> items) {
        this.transactionList.addAll(items);
        Log.e("transactionList", "" + transactionList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

}