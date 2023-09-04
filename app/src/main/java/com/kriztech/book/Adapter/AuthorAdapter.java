package com.kriztech.book.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Activity.AuthorPortfolio;
import com.kriztech.book.Model.AuthorModel.Result;
import com.kriztech.book.R;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AuthorAdapter extends RecyclerView.Adapter<AuthorAdapter.MyViewHolder> {

    private List<Result> authorList;
    private Context mcontext;
    private String from;

    public AuthorAdapter(Context context, List<Result> authorList, String from) {
        this.mcontext = context;
        this.authorList = authorList;
        this.from = from;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtAuthorName;
        RoundedImageView ivThumb;

        public MyViewHolder(View view) {
            super(view);
            txtAuthorName = view.findViewById(R.id.txtAuthorName);
            ivThumb = view.findViewById(R.id.ivThumb);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (from.equalsIgnoreCase("Home")) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.author_item, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.author_item2, parent, false);
        }

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.txtAuthorName.setText("" + authorList.get(position).getName());

        if (!TextUtils.isEmpty(authorList.get(position).getImage()))
            Picasso.get().load(authorList.get(position).getImage()).into(holder.ivThumb);

        holder.ivThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("click", "call");
                Intent intent = new Intent(mcontext, AuthorPortfolio.class);
                intent.putExtra("authorID", "" + authorList.get(position).getId());
                mcontext.startActivity(intent);
            }
        });

    }

    public void addAuthor(List<Result> items) {
        this.authorList.addAll(items);
        Log.e("authorList", "" + authorList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return authorList.size();
    }

}