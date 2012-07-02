package com.forboss;

import com.forboss.utils.ForBossUtils;

public class EventListActivity extends SpecialArticleListActivity {

	@Override
	protected int getLayoutId() {
		return R.layout.event_list;
	}

	@Override
	protected int getWrapperId() {
		return R.id.eventListWrapper;
	}

	@Override
	protected String getCategory() {
		return ForBossUtils.getEventCategory();
	}

}
