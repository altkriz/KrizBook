package com.kriztech.book.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kriztech.book.Activity.BookDetails;
import com.kriztech.book.Model.BookModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RelatedAdapter extends RecyclerView.Adapter<RelatedAdapter.MyViewHolder> {

    private List<Result> relatedList;
    private Context mcontext;

    public RelatedAdapter(Context context, List<Result> relatedList) {
        this.relatedList = relatedList;
        this.mcontext = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtDocName, txtSellCount;
        ImageView ivThumb;
        LinearLayout lySellCount;

        public MyViewHolder(View view) {
            super(view);
            txtDocName = view.findViewById(R.id.txtDocName);
            ivThumb = view.findViewById(R.id.ivThumb);
            lySellCount = view.findViewById(R.id.lySellCount);
            txtSellCount = view.findViewById(R.id.txtSellCount);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.related_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.txtDocName.setText("" + relatedList.get(position).getTitle());

        if (relatedList.get(position).getIsPaid().equalsIgnoreCase("1")) {
            holder.lySellCount.setVisibility(View.VISIBLE);
            holder.txtSellCount.setText("" + Utils.changeToK(Long.parseLong("" + relatedList.get(position).getTotalSell())));
        } else {
            holder.lySellCount.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(relatedList.get(position).getImage()))
            Picasso.get().load(relatedList.get(position).getImage()).into(holder.ivThumb);

        holder.ivThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("click", "call");
                Intent intent = new Intent(mcontext, BookDetails.class);
                intent.putExtra("docID", relatedList.get(position).getId());
                intent.putExtra("authorID", "" + relatedList.get(position).getAuthorId());
                mcontext.startActivity(intent);
            }
        });

    }

    public void addBook(List<Result> items) {
        this.relatedList.addAll(items);
        Log.e("relatedList", "" + relatedList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return relatedList.size();
    }

}