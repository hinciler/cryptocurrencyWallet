package com.sikke.app.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sikke.app.R;
import com.sikke.app.items.WalletItem;

import java.util.ArrayList;

public class WalletPagerAdapter extends PagerAdapter {
	// Declare Variables
	Context context;
	LayoutInflater inflater;

    private final ArrayList<WalletItem> items;

	public WalletPagerAdapter(Context context, ArrayList<WalletItem> items) {
		this.context = context;
		this.items = items;
    }

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == (object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {

		// Declare Variables
		TextView publicKey;
		TextView balance;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.wallet_pager_item, container,false);

		final WalletItem item = items.get(position);

		// Locate the TextViews in wallet_pager_item.xmlxml
		publicKey = itemView.findViewById(R.id.public_key);
		balance = itemView.findViewById(R.id.balance);

		// Capture position and set to the TextViews
		if (item.AliasName.equals("")){
			publicKey.setText(item.WalletNumber);
		}else {
			publicKey.setText(item.AliasName);
		}

		double dbbalance = Double.parseDouble(item.Balance== null ? "0" : item.Balance );

		balance.setText(item.Balance == null ? "0 SKK" : String.format("%.6f", dbbalance) + " SKK");

		// Add wallet_pager item.xml to ViewPager
		(container).addView(itemView);

		return itemView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// Remove wallet_pager_itemtem.xml from ViewPager
		((ViewPager) container).removeView((LinearLayout) object);

	}
}
