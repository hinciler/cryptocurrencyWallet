package com.sikke.app.fragments;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.sikke.app.R;
import com.sikke.app.items.TransactionItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class ShowTxItemDialogFragment extends DialogFragment {

    private Unbinder unbinder;

    @BindView(R.id.txt_group_id)
    TextView txt_group_id;
    @BindView(R.id.txt_seq)
    TextView txt_seq;
    @BindView(R.id.txt_from)
    TextView txt_from;
    @BindView(R.id.txt_to)
    TextView txt_to;
    @BindView(R.id.txt_amount)
    TextView txt_amount;
    @BindView(R.id.txt_desc)
    TextView txt_desc;
    @BindView(R.id.txt_action_time)
    TextView txt_action_time;
    @BindView(R.id.txt_complete_time)
    TextView txt_complete_time;
    @BindView(R.id.txt_hash)
    TextView txt_hash;
    @BindView(R.id.txt_prev_hash)
    TextView txt_prev_hash;

    TransactionItem item ;

    public ShowTxItemDialogFragment(TransactionItem item) {
        this.item = item;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.tx_item_detail_dialog, null);
        return new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setTitle(R.string.tx_details)
                .setCancelable(true)
                .setView(v)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        unbinder = ButterKnife.bind(this, getDialog());
        txt_group_id.setText(item.TxGroup);
        txt_seq.setText(item.TxNumber);
        txt_from.setText(item.From);
        txt_to.setText(item.To);
        txt_amount.setText(item.Amount);
        txt_desc.setText(item.Description);
        txt_action_time.setText(item.ActionTime);
        txt_complete_time.setText(item.CompleteTime);
        txt_group_id.setText(item.TxGroup);
        txt_hash.setText(item.Hash);
        txt_prev_hash.setText(item.PrevHash);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}

