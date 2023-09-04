package com.kriztech.book.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Activity.MagazineDetails;
import com.kriztech.book.Model.MagazineModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MagazineRelatedAdapter extends RecyclerView.Adapter<MagazineRelatedAdapter.MyViewHolder> {

    private List<Result> magazineList;
    private Context mcontext;

    public MagazineRelatedAdapter(Context context, List<Result> magazineList) {
        this.mcontext = context;
        this.magazineList = magazineList;
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

        holder.txtDocName.setText("" + magazineList.get(position).getTitle());

        if (magazineList.get(position).getIsPaid().equalsIgnoreCase("1")) {
            holder.lySellCount.setVisibility(View.VISIBLE);
            holder.txtSellCount.setText("" + Utils.changeToK(Long.parseLong("" + magazineList.get(position).getTotalSell())));
        } else {
            holder.lySellCount.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(magazineList.get(position).getImage()))
            Picasso.get().load(magazineList.get(position).getImage()).into(holder.ivThumb);

        holder.ivThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("click", "call");
                Intent intent = new Intent(mcontext, MagazineDetails.class);
                intent.putExtra("docID", "" + magazineList.get(position).getId());
                intent.putExtra("authorID", "" + magazineList.get(position).getAuthorId());
                mcontext.startActivity(intent);
            }
        });

    }

    public void addMagazine(List<Result> items) {
        this.magazineList.addAll(items);
        Log.e("magazineList", "" + magazineList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return magazineList.size();
    }

}