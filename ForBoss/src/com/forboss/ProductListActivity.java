package com.forboss;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.forboss.utils.ForBossUtils;

public class ProductListActivity extends TracableActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.product_list);
		
		ForBossUtils.initTabHeader(this);
				
		// Hide button
		ImageButton hideProductListButton = (ImageButton) findViewById(R.id.hideButton);
		hideProductListButton.setTag(this);
		hideProductListButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
