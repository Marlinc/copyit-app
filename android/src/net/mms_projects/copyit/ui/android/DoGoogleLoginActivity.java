package net.mms_projects.copyit.ui.android;

import java.io.IOException;
import java.net.URLEncoder;

import net.mms_projects.copy_it.R;
import net.mms_projects.copyit.LoginResponse;
import net.mms_projects.copyit.PasswordGenerator;
import net.mms_projects.copyit.android.tasks.ClientLoginTask;
import net.mms_projects.copyit.android.tasks.ServerApiUiTask;
import net.mms_projects.copyit.android.tasks.TaskListener;
import net.mms_projects.copyit.api.ServerApi;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class DoGoogleLoginActivity extends Activity implements TaskListener<LoginResponse> {

	DefaultHttpClient http_client = new DefaultHttpClient();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_do_google_login);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		AccountManager accountManager = AccountManager
				.get(getApplicationContext());
		Account account = (Account) intent.getExtras().get("account");
		/*accountManager
				.getAuthToken(
						account,
						"oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile",
						false, new GetAuthTokenCallback(), null);*/
		GetTokenTask task = new GetTokenTask(this);
		task.execute(account);
	}

	
	
	class GetTokenTask extends AsyncTask<Account, Void, String>
	{

		protected Activity activity;
		
		public GetTokenTask(Activity activity) {
			this.activity = activity;
		}
		
		@Override
		protected String doInBackground(Account... accounts) {
			return this.getToken(accounts[0], true);
		}
		
		private String getToken(Account account, boolean invalidateToken) {
			String authToken = "null";
			try {
				AccountManager am = AccountManager.get(this.activity);
				AccountManagerFuture<Bundle> accountManagerFuture;
				accountManagerFuture = am
						.getAuthToken(
								account,
								"oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile",
								null, this.activity, null, null);
				Bundle authTokenBundle = accountManagerFuture.getResult();
				authToken = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN)
						.toString();
				if (invalidateToken) {
					am.invalidateAuthToken("com.google", authToken);
					authToken = getToken(account, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return authToken;
		}
		
		@Override
		protected void onPostExecute(String auth_token) {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(DoGoogleLoginActivity.this);
			ServerApi api = new ServerApi();
			api.apiUrl = preferences.getString(
					"server.baseurl",
					DoGoogleLoginActivity.this.getResources().getString(
							R.string.default_baseurl));

			LoginResponse response = new LoginResponse();
			PasswordGenerator generator = new PasswordGenerator();
			response.devicePassword = generator.generatePassword();
			
			ClientLoginTask task = new ClientLoginTask(DoGoogleLoginActivity.this, api, "google",
					response);
			task.addListener(DoGoogleLoginActivity.this);
			task.execute(auth_token);
			
			super.onPostExecute(auth_token);
		}
		
	}

	/*private class GetAuthTokenCallback implements
			AccountManagerCallback<Bundle> {
		@Override
		public void run(AccountManagerFuture<Bundle> result) {
			Bundle bundle;
			try {
				bundle = result.getResult();
				String accountType = bundle
						.getString(AccountManager.KEY_ACCOUNT_TYPE);
				String authToken = bundle
						.getString(AccountManager.KEY_AUTHTOKEN);
				AccountManager accountManager = AccountManager
						.get(getApplicationContext());
				accountManager.invalidateAuthToken(accountType, authToken);
				Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
				if (intent != null) {
					// User input required
					startActivity(intent);
				} else {
					onGetAuthToken(bundle);
				}
			} catch (OperationCanceledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AuthenticatorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		protected void onGetAuthToken(Bundle bundle) {
			String auth_token = bundle.getString(AccountManager.KEY_AUTHTOKEN);

			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(DoGoogleLoginActivity.this);
			ServerApi api = new ServerApi();
			api.apiUrl = preferences.getString(
					"server.baseurl",
					DoGoogleLoginActivity.this.getResources().getString(
							R.string.default_baseurl));

			/*new ClientLoginTask(DoGoogleLoginActivity.this, api, "google",
					"password123").execute(auth_token);*-/

		}
	}*/

	private class GetCookieTask extends ServerApiUiTask<String, Void, Boolean> {
		public GetCookieTask(Context context, ServerApi api) {
			super(context, api);

			this.setUseProgressDialog(true);
		}

		@Override
		protected Boolean doInBackgroundWithException(String... tokens) {
			System.out.println(tokens[0]);
			try {
				// Don't follow redirects
				http_client.getParams().setBooleanParameter(
						ClientPNames.HANDLE_REDIRECTS, true);

				String returnUrl = "http://192.168.2.13/app-setup/setup?device_password=lala";

				HttpGet http_get = new HttpGet(
						"http://192.168.2.13/auth/client-login/google?"
								+ "returnurl="
								+ URLEncoder.encode(returnUrl, "UTF-8")
								+ "&accesstoken=" + tokens[0]);
				System.out.println(http_get.getURI());
				HttpResponse response;
				response = http_client.execute(http_get);
				HttpEntity entity = response.getEntity();

				String responseText = IOUtils.toString(entity.getContent(),
						"UTF-8");
				System.out.println("Response: " + responseText);
				if (response.getStatusLine().getStatusCode() != 302)
					// Response should be a redirect
					return false;

				for (Cookie cookie : http_client.getCookieStore().getCookies()) {
					if (cookie.getName().equals("ACSID"))
						return true;
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			try {
				this.doExceptionCheck();

				if (result) {
					// No error
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				Toast.makeText(
						this.context,
						this.context.getResources()
								.getString(R.string.error_general,
										e.getLocalizedMessage()),
						Toast.LENGTH_LONG).show();
			}

			/*
			 * new AuthenticatedRequestTask()
			 * .execute("http://yourapp.appspot.com/admin/");
			 */
			super.onPostExecute(result);
		}
	}

	@Override
	public void onTaskReady(LoginResponse response) {
		Intent returnIntent = new Intent();
		returnIntent.putExtra("device_id",
				response.deviceId.toString());
		returnIntent.putExtra("device_password",
				response.devicePassword);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

}
