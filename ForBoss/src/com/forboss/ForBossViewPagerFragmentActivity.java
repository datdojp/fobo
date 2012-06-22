package com.forboss;

import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.forboss.adapter.PagerAdapter;
import com.forboss.data.model.Article;
import com.forboss.data.model.Event;

public class ForBossViewPagerFragmentActivity extends FragmentActivity {
	// pager
	private PagerAdapter mPagerAdapter;
	private ViewPager viewPager;
	
	// wrappers
	private View articleListWrapper;
	private View eventListWrapper;
	private View productListWrapper;
	
	// data
	private Map<String, List<Article>> articleData;
	private List<Event> eventData;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		super.setContentView(R.layout.main);

		// get wrapper from layout
		articleListWrapper = findViewById(R.id.articleListWrapper);
		eventListWrapper = findViewById(R.id.eventListWrapper);
		productListWrapper = findViewById(R.id.productListWrapper);

		initTabHeader();
		
		
	}

	private void initTabHeader() {
		Button articleButton = (Button) findViewById(R.id.contentButton);
		articleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				articleListWrapper.setVisibility(View.VISIBLE);
				eventListWrapper.setVisibility(View.GONE);
				productListWrapper.setVisibility(View.GONE);
			}
		});
		Button eventButton = (Button) findViewById(R.id.eventButton);
		eventButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				articleListWrapper.setVisibility(View.GONE);
				eventListWrapper.setVisibility(View.VISIBLE);
				productListWrapper.setVisibility(View.GONE);
			}
		});
		Button moreButton = (Button) findViewById(R.id.moreButton);
		moreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				articleListWrapper.setVisibility(View.GONE);
				eventListWrapper.setVisibility(View.GONE);
				productListWrapper.setVisibility(View.VISIBLE);
			}
		});
		articleButton.performClick();
	}

}
