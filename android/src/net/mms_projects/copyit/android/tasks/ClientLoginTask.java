package net.mms_projects.copyit.android.tasks;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.mms_projects.copyit.LoginResponse;
import net.mms_projects.copyit.api.ServerApi;
import net.mms_projects.copyit.api.responses.ApiResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

public class ClientLoginTask extends
		ServerApiUiTask<String, Void, LoginResponse> {

	private String service;
	protected LoginResponse response = new LoginResponse();
	protected List<TaskListener<LoginResponse>> listeners = new ArrayList<TaskListener<LoginResponse>>();

	public ClientLoginTask(Context context, ServerApi api, String service,
			LoginResponse response) {
		super(context, api);

		this.service = service;
		this.response = response;
	}
	
	public void addListener(TaskListener<LoginResponse> listener) {
		this.listeners.add(listener);
	}

	@Override
	protected LoginResponse doInBackgroundWithException(String... tokens)
			throws Exception {
		DefaultHttpClient client = new DefaultHttpClient();
		client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS,
				true);
		client.addResponseInterceptor(new HttpResponseInterceptor() {
			@Override
			public void process(HttpResponse httpResponse, HttpContext context)
					throws HttpException, IOException {
				if (httpResponse.containsHeader("Location")) {
					Header[] locations = httpResponse.getHeaders("Location");
					if (locations.length > 0) {
						String location = locations[0].getValue();
						if (location.startsWith("/app-setup/done/")) {
							response.deviceId = UUID.fromString(location
									.substring(16));
							Intent returnIntent = new Intent();
							returnIntent.putExtra("device_id",
									response.deviceId.toString());
							returnIntent.putExtra("device_password",
									response.devicePassword);
						} 
					}
						System.out.println(locations[0].getValue());
				}
			}
		});

		String returnUrl = this.api.apiUrl
				+ "/app-setup/setup?device_password=" + this.response.devicePassword;

		HttpGet http_get = new HttpGet(this.api.apiUrl + "/auth/client-login/"
				+ this.service + "?" + "returnurl="
				+ URLEncoder.encode(returnUrl, "UTF-8") + "&accesstoken="
				+ tokens[0]);
		System.out.println(http_get.getURI());
		HttpResponse httpResponse = client.execute(http_get);
		HttpEntity entity = httpResponse.getEntity();

		String responseText = IOUtils.toString(entity.getContent(), "UTF-8");
		System.out.println("Response: " + responseText);

		System.out.println(this.response.deviceId);

		ApiResponse data = null;
		try {
			data = new Gson().fromJson(responseText, ApiResponse.class);
		} catch (com.google.gson.JsonSyntaxException exception) {
			throw new Exception(exception);
		}
		
		if ("ERR".equals(data.status)) {
			System.out.println(data.messages.get(0));
		}

		return this.response;
	}

	@Override
	protected void onPostExecute(LoginResponse result) {
		for (TaskListener<LoginResponse> listener : this.listeners) {
			listener.onTaskReady(result);
		}
		
		super.onPostExecute(result);
	}
	
}
