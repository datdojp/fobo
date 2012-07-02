package com.forboss;

import android.os.Bundle;

import com.forboss.utils.ForBossUtils;

public class ProductListActivity extends TracableActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.product_list);
		
		ForBossUtils.initTabHeader(this);
	}
}
