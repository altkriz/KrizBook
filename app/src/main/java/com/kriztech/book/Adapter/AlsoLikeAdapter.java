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

import com.kriztech.book.Activity.BookDetails;
import com.kriztech.book.Model.BookModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AlsoLikeAdapter extends RecyclerView.Adapter<AlsoLikeAdapter.MyViewHolder> {

    Context mcontext;
    private List<Result> alsoLikeList;

    public AlsoLikeAdapter(Context context, List<Result> alsoLikeList) {
        this.mcontext = context;
        this.alsoLikeList = alsoLikeList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout lyAlsoLike, lySellCount;
        public TextView txt_bookname, txtSellCount, txt_chapters, txt_category;
        ImageView ivThumb;

        public MyViewHolder(View view) {
            super(view);
            lyAlsoLike = view.findViewById(R.id.lyAlsoLike);
            txt_bookname = view.findViewById(R.id.txt_bookname);
            txt_chapters = view.findViewById(R.id.txt_chapters);
            txt_category = view.findViewById(R.id.txt_category);
            ivThumb = view.findViewById(R.id.ivThumb);
            lySellCount = view.findViewById(R.id.lySellCount);
            txtSellCount = view.findViewById(R.id.txtSellCount);
        }
    }

    @Override
    public AlsoLikeAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.also_like_item, parent, false);

        return new AlsoLikeAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AlsoLikeAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.txt_bookname.setText("" + alsoLikeList.get(position).getTitle());
        holder.txt_chapters.setText(mcontext.getResources().getString(R.string.chapter) + " " + alsoLikeList.get(position).getChapterCount());
        holder.txt_category.setText("" + alsoLikeList.get(position).getCategoryName());

        if (alsoLikeList.get(position).getIsPaid().equalsIgnoreCase("1")) {
            holder.lySellCount.setVisibility(View.VISIBLE);
            holder.txtSellCount.setText("" + Utils.changeToK(Long.parseLong("" + alsoLikeList.get(position).getTotalSell())));
        } else {
            holder.lySellCount.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(alsoLikeList.get(position).getImage()))
            Picasso.get().load(alsoLikeList.get(position).getImage()).into(holder.ivThumb);

        holder.lyAlsoLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("click", "call");
                Intent intent = new Intent(mcontext, BookDetails.class);
                intent.putExtra("docID", "" + alsoLikeList.get(position).getId());
                intent.putExtra("authorID", "" + alsoLikeList.get(position).getAuthorId());
                mcontext.startActivity(intent);
            }
        });

    }

    public void addBook(List<Result> items) {
        this.alsoLikeList.addAll(items);
        Log.e("alsoLikeList", "" + alsoLikeList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return alsoLikeList.size();
    }

}