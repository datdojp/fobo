package com.forboss;

import com.forboss.utils.ForBossUtils;

public class C360ListActivity extends SpecialArticleListActivity {
	@Override
	protected int getLayoutId() {
		return R.layout.c360_list;
	}

	@Override
	protected int getWrapperId() {
		return R.id.c360ListWrapper;
	}

	@Override
	protected String getCategory() {
		return ForBossUtils.getC360Category();
	}
}
