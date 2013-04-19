package net.mms_projects.copyit.ui.android;

import net.mms_projects.copy_it.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.widget.TextView;

public class DebugActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);
		
		TextView baseUrl = (TextView) findViewById(R.id.info_server_baseurl);
		baseUrl.setText(preferences.getString("server.baseurl", this.getResources().getString(R.string.default_baseurl)));
		
		TextView deviceId = (TextView) findViewById(R.id.info_device_id);
		deviceId.setText(preferences.getString("device.id", "None"));
		
		TextView devicePassword = (TextView) findViewById(R.id.info_device_password);
		devicePassword.setText(preferences.getString("device.password", "None"));
	}

	public static class Launch extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent launchingIntent) {
			Intent intent = new Intent(context, DebugActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
		
	}
	
}