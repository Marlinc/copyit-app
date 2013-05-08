package net.mms_projects.copyit.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthService extends Service {

	private Authenticator auth;

	@Override
	public void onCreate() {
		super.onCreate();

		this.auth = new Authenticator(this);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return this.auth.getIBinder();
	}

}
