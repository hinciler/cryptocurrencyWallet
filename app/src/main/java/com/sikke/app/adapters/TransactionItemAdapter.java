package com.sikke.app.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sikke.app.R;
import com.sikke.app.activities.MainActivity;
import com.sikke.app.fragments.ShowTxItemDialogFragment;
import com.sikke.app.items.TransactionItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransactionItemAdapter extends RecyclerView.Adapter<TransactionItemAdapter.ViewHolder> {

    private ArrayList<TransactionItem> items;
    private Context mCtx;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClicked(View v, TransactionItem item);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data country_item is just a string in this case

        @BindView(R.id.date)
        public TextView date;
        @BindView(R.id.hour)
        public TextView hour;
        @BindView(R.id.description)
        public TextView description;
        @BindView(R.id.amount)
        public TextView amount;
        @BindView(R.id.tx_item_layout)
        public RelativeLayout tx_item_layout;

        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            ButterKnife.bind(this, v);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TransactionItemAdapter(OnItemClickListener listener,ArrayList<TransactionItem> myDataset, Context mCtx) {
        super();
        this.listener = listener;
        this.items = myDataset;
        this.mCtx = mCtx;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TransactionItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.tx_item, parent, false);

        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final TransactionItem item = items.get(position);

        holder.date.setText(item.Date == null ? "" : item.Date);
        holder.hour.setText(item.Hour == null ? "" : item.Hour);
        holder.description.setText(item.Description == null ? "" : item.Description);
        double amount = Double.parseDouble(item.Amount);
        holder.amount.setText(item.Amount == null ? "0" :  String.format("%.6f", amount));

        holder.tx_item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClicked(v, item);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }
}
