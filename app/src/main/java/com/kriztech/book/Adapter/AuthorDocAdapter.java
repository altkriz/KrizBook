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
import com.kriztech.book.Activity.MagazineDetails;
import com.kriztech.book.Interface.AddNewItem;
import com.kriztech.book.Interface.OnEditVisibilityClick;
import com.kriztech.book.Model.BookModel.Result;
import com.kriztech.book.R;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AuthorDocAdapter extends RecyclerView.Adapter<AuthorDocAdapter.MyViewHolder> {

    private Context mcontext;
    private List<Result> authorDocList;
    private String viewFrom = "", savedAuthorID = "", currentAuthorID, authorStatus = "";
    private OnEditVisibilityClick onEditVisibilityClick;
    private AddNewItem addNewItem;

    public AuthorDocAdapter(Context context, List<Result> authorDocList, String viewFrom, OnEditVisibilityClick onEditVisibilityClick, AddNewItem addNewItem,
                            String savedAuthorID, String currentAuthorID, String authorStatus) {
        this.mcontext = context;
        this.authorDocList = authorDocList;
        this.viewFrom = viewFrom;
        this.onEditVisibilityClick = onEditVisibilityClick;
        this.addNewItem = addNewItem;
        this.savedAuthorID = savedAuthorID;
        this.currentAuthorID = currentAuthorID;
        this.authorStatus = authorStatus;
        Log.e("savedAuthorID", "==:=>> " + savedAuthorID);
        Log.e("currentAuthorID", "==:=>> " + currentAuthorID);
        Log.e("authorStatus", "==:=>> " + authorStatus);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtDocName, txtDocType, txtDocEditIcon, txtDocStatusIcon;
        ImageView ivThumb;
        LinearLayout lyDocStatus, lyDocument, lyAddDocument, lyEditVisibility, lyDocEdit;
        SimpleRatingBar ratingbar;

        public MyViewHolder(View view) {
            super(view);
            txtDocName = view.findViewById(R.id.txtDocName);
            txtDocType = view.findViewById(R.id.txtDocType);
            lyDocEdit = view.findViewById(R.id.lyDocEdit);
            txtDocEditIcon = view.findViewById(R.id.txtDocEditIcon);
            txtDocStatusIcon = view.findViewById(R.id.txtDocStatusIcon);
            ivThumb = view.findViewById(R.id.ivThumb);
            lyDocStatus = view.findViewById(R.id.lyDocStatus);
            lyAddDocument = view.findViewById(R.id.lyAddDocument);
            lyDocument = view.findViewById(R.id.lyDocument);
            lyEditVisibility = view.findViewById(R.id.lyEditVisibility);
            ratingbar = view.findViewById(R.id.ratingbar);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.author_doc_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        Log.e("both are equal ?", "===>> " + (savedAuthorID.equals(currentAuthorID)));
        if (position == 0 && savedAuthorID.equals("" + currentAuthorID)) {
            Log.e("savedAuthorID", "===>> " + savedAuthorID);
            Log.e("currentAuthorID", "===>> " + currentAuthorID);
            holder.lyAddDocument.setVisibility(View.VISIBLE);
            holder.lyDocument.setVisibility(View.GONE);
            if (viewFrom.equalsIgnoreCase("Magazines")) {
                holder.txtDocType.setText("" + mcontext.getResources().getString(R.string.add_magazines));
            } else {
                holder.txtDocType.setText("" + mcontext.getResources().getString(R.string.add_books));
            }

            holder.lyAddDocument.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("Click", "viewFrom ==> " + viewFrom);
                    Log.e("Click", "position => " + position);

                    addNewItem.addNewDoc("" + viewFrom);
                }
            });

        } else {
            holder.lyAddDocument.setVisibility(View.GONE);
            holder.lyDocument.setVisibility(View.VISIBLE);

            Log.e("position", "==> " + position);
            if (position > 0 && savedAuthorID.equals("" + currentAuthorID)) {

                if (savedAuthorID.equalsIgnoreCase("" + currentAuthorID)) {
                    holder.ratingbar.setVisibility(View.GONE);
                    holder.lyEditVisibility.setVisibility(View.VISIBLE);

                    if (authorStatus.equalsIgnoreCase("1")) {
                        holder.lyDocStatus.setVisibility(View.VISIBLE);
                        if (authorDocList.get(position - 1).getStatus().equalsIgnoreCase("1")) {
                            holder.txtDocStatusIcon.setBackground(mcontext.getResources().getDrawable(R.drawable.ic_visible));
                        } else {
                            holder.txtDocStatusIcon.setBackground(mcontext.getResources().getDrawable(R.drawable.ic_invisible));
                        }
                    } else {
                        holder.lyDocStatus.setVisibility(View.GONE);
                    }
                } else {
                    holder.ratingbar.setVisibility(View.VISIBLE);
                    holder.lyEditVisibility.setVisibility(View.GONE);
                    holder.lyDocStatus.setVisibility(View.GONE);
                }

                holder.txtDocName.setText("" + authorDocList.get(position - 1).getTitle());
                holder.ratingbar.setRating(Float.parseFloat("" + authorDocList.get(position - 1).getAvgRating()));
                if (!TextUtils.isEmpty(authorDocList.get(position - 1).getImage()))
                    Picasso.get().load(authorDocList.get(position - 1).getImage()).into(holder.ivThumb);

                holder.ivThumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("click", "call ==>> " + (position - 1));
                        Intent intent;
                        if (viewFrom.equalsIgnoreCase("Magazines")) {
                            intent = new Intent(mcontext, MagazineDetails.class);
                        } else {
                            intent = new Intent(mcontext, BookDetails.class);
                        }
                        intent.putExtra("docID", "" + authorDocList.get(position - 1).getId());
                        intent.putExtra("authorID", "" + authorDocList.get(position - 1).getAuthorId());
                        mcontext.startActivity(intent);
                    }
                });

                holder.lyDocStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("Click", "(position - 1) => " + (position - 1));
                        Log.e("Click", "BookID => " + authorDocList.get(position - 1).getId());
                        Log.e("Click", "AuthorID => " + authorDocList.get(position - 1).getAuthorId());
                        Log.e("currentAuthorID", "=> " + currentAuthorID);
                        Log.e("savedAuthorID", "=> " + savedAuthorID);

                        if (savedAuthorID.equalsIgnoreCase("" + currentAuthorID)) {
                            onEditVisibilityClick.OnVisibilityClick("" + authorDocList.get(position - 1).getId(), (position - 1));
                        }
                    }
                });

                holder.lyDocEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("Click", "(position - 1) => " + (position - 1));
                        Log.e("Click", "BookID => " + authorDocList.get(position - 1).getId());
                        Log.e("Click", "AuthorID => " + authorDocList.get(position - 1).getAuthorId());
                        Log.e("currentAuthorID", "=> " + currentAuthorID);
                        Log.e("savedAuthorID", "=> " + savedAuthorID);

                        if (savedAuthorID.equalsIgnoreCase("" + currentAuthorID)) {
                            onEditVisibilityClick.OnEditClick("" + authorDocList.get(position - 1).getId(), (position - 1));
                        }
                    }
                });

            } else {

                if (savedAuthorID.equalsIgnoreCase("" + currentAuthorID)) {
                    holder.ratingbar.setVisibility(View.GONE);
                    holder.lyEditVisibility.setVisibility(View.VISIBLE);

                    if (authorStatus.equalsIgnoreCase("1")) {
                        holder.lyDocStatus.setVisibility(View.VISIBLE);
                        if (authorDocList.get(position).getStatus().equalsIgnoreCase("1")) {
                            holder.txtDocStatusIcon.setBackground(mcontext.getResources().getDrawable(R.drawable.ic_visible));
                        } else {
                            holder.txtDocStatusIcon.setBackground(mcontext.getResources().getDrawable(R.drawable.ic_invisible));
                        }
                    } else {
                        holder.lyDocStatus.setVisibility(View.GONE);
                    }
                } else {
                    holder.ratingbar.setVisibility(View.VISIBLE);
                    holder.lyEditVisibility.setVisibility(View.GONE);
                    holder.lyDocStatus.setVisibility(View.GONE);
                }
                holder.txtDocName.setText("" + authorDocList.get(position).getTitle());
                holder.ratingbar.setRating(Float.parseFloat("" + authorDocList.get(position).getAvgRating()));
                if (!TextUtils.isEmpty(authorDocList.get(position).getImage()))
                    Picasso.get().load(authorDocList.get(position).getImage()).into(holder.ivThumb);

                holder.ivThumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("click", "call ==>> " + position);
                        Intent intent;
                        if (viewFrom.equalsIgnoreCase("Magazines")) {
                            intent = new Intent(mcontext, MagazineDetails.class);
                        } else {
                            intent = new Intent(mcontext, BookDetails.class);
                        }
                        intent.putExtra("docID", "" + authorDocList.get(position).getId());
                        intent.putExtra("authorID", "" + authorDocList.get(position).getAuthorId());
                        mcontext.startActivity(intent);
                    }
                });

                holder.lyDocStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("Click", "position => " + (position));
                        Log.e("Click", "docID => " + authorDocList.get(position).getId());
                        Log.e("Click", "AuthorID => " + authorDocList.get(position).getAuthorId());
                        Log.e("currentAuthorID", "=> " + currentAuthorID);
                        Log.e("savedAuthorID", "=> " + savedAuthorID);

                        if (savedAuthorID.equalsIgnoreCase("" + currentAuthorID)) {
                            onEditVisibilityClick.OnVisibilityClick("" + authorDocList.get(position).getId(), position);
                        }
                    }
                });

                holder.lyDocEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("Click", "position => " + (position));
                        Log.e("Click", "docID => " + authorDocList.get(position).getId());
                        Log.e("Click", "AuthorID => " + authorDocList.get(position).getAuthorId());
                        Log.e("currentAuthorID", "=> " + currentAuthorID);
                        Log.e("savedAuthorID", "=> " + savedAuthorID);

                        if (savedAuthorID.equalsIgnoreCase("" + currentAuthorID)) {
                            onEditVisibilityClick.OnEditClick("" + authorDocList.get(position).getId(), position);
                        }
                    }
                });

            }
        }

    }

    public void addBook(List<Result> items) {
        this.authorDocList.addAll(items);
        Log.e("authorDocList", "" + authorDocList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (savedAuthorID.equals("" + currentAuthorID))
            return authorDocList.size() + 1;
        else
            return authorDocList.size();
    }

}