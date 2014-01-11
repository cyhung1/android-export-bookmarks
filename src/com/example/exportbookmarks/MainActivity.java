package com.example.exportbookmarks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Browser;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Export Android Browser bookmarks to a text file on sdcard.
 * 
 * @author victor
 * 
 */
public class MainActivity extends Activity {

	private static final String LOG_TAG = null;
	private static final String BOOKMARK_FILENAME = "bookmarks.txt";

	ListView bookmarkList;

	ArrayList<String> bookmarks = new ArrayList<String>();

	private View exportButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		makeBookmarkList();

		exportButton = findViewById(R.id.exportButton);
		exportButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// concatenate all url into one giant string and save to file
				StringBuffer buffer = new StringBuffer();
				for (String url : bookmarks)
					buffer.append(url + "\n");

				writeTextToFile(BOOKMARK_FILENAME, buffer.toString());
			}
		});

	}

	/**
	 * Fetch all bookmarks from Browser app and show to the user in list format.
	 * 
	 */
	private void makeBookmarkList() {

		getBrowserBookmarks(bookmarks);

		// populate the list adapter with bookmarks
		bookmarkList = (ListView) findViewById(R.id.bookmarkList);

		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < bookmarks.size(); ++i)
			list.add(bookmarks.get(i));

		final BookmarkListAdapter adapter = new BookmarkListAdapter(this,
				android.R.layout.simple_list_item_1, list);
		bookmarkList.setAdapter(adapter);

		// do nothing in the onClick() :)
		bookmarkList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent,
							final View view, int position, long id) {
						// final String item = (String)
						// parent.getItemAtPosition(position);
					}

				});

		// display total number of bookmarks
		TextView infoText = (TextView) findViewById(R.id.info_text);
		infoText.setText("" + bookmarks.size() + " bookmarks.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Query Android Browser app for the user bookmarks.
	 * <p>
	 * 
	 * @param bookmarks
	 */
	private void getBrowserBookmarks(ArrayList<String> bookmarks) {

		String[] requestedColumns = { Browser.BookmarkColumns.TITLE,
				Browser.BookmarkColumns.URL };
		Cursor resultSet = managedQuery(Browser.BOOKMARKS_URI,
				requestedColumns, Browser.BookmarkColumns.BOOKMARK + "=1",
				null, null);

		Log.d(LOG_TAG, "Bookmarks count: " + resultSet.getCount());

		// don't care about titles, only URLs!
		// int title = resultSet.getColumnIndex(Browser.BookmarkColumns.TITLE);
		int url = resultSet.getColumnIndex(Browser.BookmarkColumns.URL);

		resultSet.moveToFirst();
		while (!resultSet.isAfterLast()) {
			if (resultSet.getString(url) != null)
				bookmarks.add(resultSet.getString(url));
			resultSet.moveToNext();
		}
	}

	private class BookmarkListAdapter extends ArrayAdapter<String> {

		HashMap<String, Integer> hashMap = new HashMap<String, Integer>();

		public BookmarkListAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				System.err.println("" + i + ":" + objects.get(i));
				if (objects.get(i) != null)
					hashMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			return hashMap.get(item);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public int getCount() {
			return hashMap.size();
		}
	}

	/**
	 * Create a specified text file.
	 * <p>
	 * 
	 * @param fileName
	 * @param text
	 */
	private void writeTextToFile(String fileName, String text) {

		// "sdcard" present?
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String msg = "No SD card present.";
			Log.d(LOG_TAG, msg);
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			return;
		}

		try {
			File root = new File(Environment.getExternalStorageDirectory(), "");
			if (!root.exists()) {
				root.mkdirs();
			}

			FileWriter writer = new FileWriter(new File(root, fileName));
			writer.append(text);
			writer.flush();
			writer.close();

			Toast.makeText(this, "Bookmars saved to " + root + "/" + fileName,
					Toast.LENGTH_SHORT).show();

		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, "Unable to save bookmarks!",
					Toast.LENGTH_SHORT).show();
		}

	}
}
