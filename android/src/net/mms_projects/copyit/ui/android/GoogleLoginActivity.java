package net.mms_projects.copyit.ui.android;

import net.mms_projects.copy_it.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class GoogleLoginActivity extends ListActivity {

	protected AccountManager accountManager;
	protected Intent intent;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountManager = AccountManager.get(getApplicationContext());
        Account[] accounts = accountManager.getAccountsByType("com.google");
        this.setListAdapter(new ArrayAdapter(this, R.layout.list_item, accounts));        
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.google_login, menu);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Account account = (Account) getListView().getItemAtPosition(position);
		intent = new Intent(this, DoGoogleLoginActivity.class);
		intent.putExtra("account", account);
		startActivity(intent);
	}

}
