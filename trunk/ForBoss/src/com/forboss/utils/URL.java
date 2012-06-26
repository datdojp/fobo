package com.forboss.utils;

public class URL {
	public static final String LOGIN_URL = ForBossUtils.getConfig("API_URL") + "/" + "api/mobile/sendemail";
	public static final String GET_ARTICLE_URL = ForBossUtils.getConfig("API_URL") + "/" + "api/mobile/post";
}
