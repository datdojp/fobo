package com.forboss.data.utils;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.forboss.R;
import com.forboss.data.model.Article;
import com.forboss.data.model.Event;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "forboss.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 1;

	private static final AtomicInteger usageCounter = new AtomicInteger(0);
	private static DatabaseHelper helper;


	// the DAO object we use to access db
	private Dao<Article, String> articleDao = null;
	private Dao<Event, String> eventDao;


	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	/**
	 * Get the helper, possibly constructing it if necessary. For each call to this method, there should be 1 and only 1
	 * call to {@link #close()}.
	 */
	public static synchronized DatabaseHelper getHelper(Context context) {
		if (helper == null) {
			helper = new DatabaseHelper(context);
		}
		usageCounter.incrementAndGet();
		return helper;
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, Article.class);
			TableUtils.createTable(connectionSource, Event.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, Article.class, true);
			TableUtils.dropTable(connectionSource, Event.class, true);

			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the Database Access Object (DAO) for Agenda class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Article, String> getArticleDao() throws SQLException {
		if (articleDao == null) {
			articleDao = getDao(Article.class);
		}
		return articleDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for Speaker class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Event, String> getEventDao() throws SQLException {
		if (eventDao == null) {
			eventDao = getDao(Event.class);
		}
		return eventDao;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		if (usageCounter.decrementAndGet() == 0) {
			super.close();
			eventDao = null;
			articleDao = null;
		}
	}

}
