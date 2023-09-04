package com.kriztech.book.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Model.CommentModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Utils;

import java.util.List;
import java.util.Random;

public class MagazineCommentAdapter extends RecyclerView.Adapter<MagazineCommentAdapter.MyViewHolder> {

    private List<Result> commentList;
    Context mcontext;
    String from;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_user_name, txt_comment, txt_date, txt_tag;
        ImageView ivThumb;

        public MyViewHolder(View view) {
            super(view);
            txt_user_name = view.findViewById(R.id.txt_user_name);
            txt_comment = view.findViewById(R.id.txt_comment);
            txt_date = view.findViewById(R.id.txt_date);
            ivThumb = view.findViewById(R.id.ivThumb);
            txt_tag = view.findViewById(R.id.txt_tag);
        }
    }


    public MagazineCommentAdapter(Context context, List<Result> commentList, String from) {
        this.commentList = commentList;
        this.mcontext = context;
        this.from = from;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.txt_user_name.setText("" + commentList.get(position).getFullname());
        holder.txt_comment.setText("" + commentList.get(position).getComment());
        holder.txt_date.setText("" + Utils.DateFormat2(commentList.get(position).getCreatedAt()));
        holder.txt_tag.setText("" + commentList.get(position).getFullname().charAt(0));

        holder.ivThumb.setBackgroundColor(getRandomColor());

        holder.ivThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("click", "call");
//                Intent intent = new Intent(mcontext, BookDetails.class);
//                intent.putExtra("docID", commentList.get(position).getBId());
//                intent.putExtra("authorID", "" + commentList.get(position).getAuthorId());
//                mcontext.startActivity(intent);
            }
        });

    }

    public void addBook(List<Result> items) {
        this.commentList.addAll(items);
        Log.e("commentList", "" + commentList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (from.equalsIgnoreCase("Max_5")) {
            return 5;
        } else {
            return commentList.size();
        }
    }

    public int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

}