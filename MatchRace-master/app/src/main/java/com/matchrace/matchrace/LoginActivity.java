package com.matchrace.matchrace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.matchrace.matchrace.classes.C;
import com.matchrace.matchrace.classes.GetBuoysTask;
import com.matchrace.matchrace.classes.SendDataHThread;
import com.matchrace.matchrace.modules.JsonReader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Login activity. Allows the user to log in or register to DB.
 *
 */
public class LoginActivity extends Activity {

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Indicates requested login.
	private boolean adminRequest = false;
	private boolean registerRequest = false;

	// SharedPreferences used for loading the latest user.
	private SharedPreferences sp;

	// Values for user, password and event at the time of the login attempt.
	private String mUser;
	private String mPassword;
	private String mEvent;

	// UI references.
	private EditText etUser;
	private EditText etPass;
	private EditText etEvent;
	private View svLoginForm;
	private View llLoginStatus;
	private TextView tvLoginStatusMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Disables lock-screen and keeps screen on.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		loadLastUser();
		initialize();
	}

	/**
	 * Loads the latest user that was connected.
	 */
	public void loadLastUser() {
		sp = getSharedPreferences(C.PREFS_USER, MODE_PRIVATE); 
		String fullUserName = sp.getString(C.PREFS_FULL_USER_NAME, "Anonymous");
		if (fullUserName != null && !fullUserName.equals("Anonymous")) {
			Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
			String user = fullUserName.split("_")[0];
			String pass = fullUserName.split("_")[1];
			String event = fullUserName.split("_")[2];
			Toast.makeText(this, "Hello " + user.substring(6) + "!", Toast.LENGTH_SHORT).show();
			intent.putExtra(C.USER_NAME, user);
			intent.putExtra(C.USER_PASS, pass);
			intent.putExtra(C.EVENT_NUM, event);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
	}

	/**
	 * Initialize components.
	 */
	private void initialize() {
		// Initialize Views.
		etUser = (EditText) findViewById(R.id.etUser);
		etPass = (EditText) findViewById(R.id.etPassword);
		etPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});
		etEvent = (EditText) findViewById(R.id.etEvent);

		svLoginForm = findViewById(R.id.svLoginForm);
		llLoginStatus = findViewById(R.id.llLoginStatus);
		tvLoginStatusMessage = (TextView) findViewById(R.id.tvLoginStatusMessage);

		findViewById(R.id.bSignIn).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});

		// Register button.
		findViewById(R.id.bReg).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				registerRequest = true;
				attemptLogin();
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		etUser.setError(null);
		etPass.setError(null);
		etEvent.setError(null);

		// Store values at the time of the login attempt.
		mUser = C.SAILOR_PREFIX + etUser.getText().toString();
		mPassword = etPass.getText().toString();
		mEvent = etEvent.getText().toString();

		String lowerName = mUser.toLowerCase();
		String lowerPass = mPassword.toLowerCase();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			etPass.setError(getString(R.string.error_field_required));
			focusView = etPass;
			cancel = true;
		} else if (mPassword.length() < 4) {
			etPass.setError(getString(R.string.error_invalid_password));
			focusView = etPass;
			cancel = true;
		}

		// Check for a valid user.
		if (TextUtils.isEmpty(mUser)) {
			etUser.setError(getString(R.string.error_field_required));
			focusView = etUser;
			cancel = true;
		} else if (mUser.contains(" ")) {
			etUser.setError(getString(R.string.error_invalid_user));
			focusView = etUser;
			cancel = true;
		} else if (registerRequest && (mUser.equals(C.SAILOR_PREFIX +"admin") || mUser.equals(C.SAILOR_PREFIX + "Admin")))
		{
			etUser.setError(getString(R.string.error_admin_registration));
			focusView = etUser;
			registerRequest = false;
				cancel = true;
		} else if (lowerName.contains("select") || lowerName.contains("from") || lowerName.contains("where") || lowerName.contains("drop")) {
			etUser.setError(getString(R.string.error_invalid_strings));
			registerRequest = false;
			focusView = etUser;
			cancel = true;
		} else if (lowerPass.contains("select") || lowerPass.contains("from") || lowerPass.contains("where") || lowerPass.contains("drop")) {
			etPass.setError(getString(R.string.error_invalid_strings));
			registerRequest = false;
			focusView = etPass;
			cancel = true;
		}

		// Check for a valid event.
		if (TextUtils.isEmpty(mEvent)) {
			etEvent.setError(getString(R.string.error_field_required));
			focusView = etEvent;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		}
		else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			tvLoginStatusMessage.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			llLoginStatus.setVisibility(View.VISIBLE);
			llLoginStatus.animate().setDuration(shortAnimTime)
			.alpha(show ? 1 : 0)
			.setListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationEnd(Animator animation) {
					llLoginStatus.setVisibility(show ? View.VISIBLE
							: View.GONE);
				}
			});

			svLoginForm.setVisibility(View.VISIBLE);
			svLoginForm.animate().setDuration(shortAnimTime)
			.alpha(show ? 0 : 1)
			.setListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationEnd(Animator animation) {
					svLoginForm.setVisibility(show ? View.GONE
							: View.VISIBLE);
				}
			});
		}
		else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			llLoginStatus.setVisibility(show ? View.VISIBLE : View.GONE);
			svLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			if (mUser.equals("Sailoradmin") || mUser.equals("SailorAdmin")) {
				adminRequest = true;
				boolean ans = mPassword.equals("admin") || mPassword.equals("Admin");
				if(ans) return 0; 	// OK
				else return 1;		// Wrong pass
			}
			// registration handling
			if (registerRequest) {
				String url = C.URL_CLIENTS_TABLE + "UserCheck" + "&Information=" + mUser + "_" + mPassword + "_" + mEvent;
				try {
					InputStream is = new URL(url).openStream();
					BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
					String in = rd.readLine();
					is.close();
					if(in.startsWith("NotOK"))
					{ return 2;	}	// user already registered
					else if (in.startsWith("NoEvent"))
					{ return 3; }	// no such Event
				}
				catch (Exception e)
				{e.printStackTrace(); }


				// HandlerThread for creating a new user in the DB through thread.
				SendDataHThread thread = new SendDataHThread("CreateNewUser");
				thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);

				thread.setFullUserName("new" + mUser + "_" + mPassword + "_" + mEvent);
				thread.setEvent(mEvent);
				thread.setLat("0");
				thread.setLng("0");
				thread.setSpeed("0");
				thread.setBearing("0");

				thread.start();
				return 0; // ok
			}

			String name = "UserLoginTask";

				// Gets the user data from DB and checks if the user's data match.
				String url = C.URL_CLIENTS_TABLE + "&Information=" + mUser + "_" + mPassword + "_" + mEvent;

			try {
				InputStream is = new URL(url).openStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				String in = rd.readLine();
				is.close();
				boolean ans = in.startsWith("OK");
				if(ans) return 0; 	// OK
				else return 1;		// Wrong pass

			}
			catch (Exception e)
			{e.printStackTrace(); }
			return 1; // wring pass
		}

		@Override
		protected void onPostExecute(final Integer success) {
			mAuthTask = null;
			showProgress(false);

			if (success.equals((Integer)0)) {
				Intent intent;
				if (adminRequest) {
					adminRequest = false;
					intent = new Intent(LoginActivity.this, AdminActivity.class);
				}
				else {
					intent = new Intent(LoginActivity.this, MenuActivity.class);
				}

				if (!mUser.equals("Sailoradmin") && !mUser.equals("SailorAdmin")) {
					// Updates the SharedPreferences.
					String fullUserName = mUser + "_" + mPassword + "_" + mEvent;
					SharedPreferences.Editor spEdit = sp.edit();
					spEdit.putString(C.PREFS_FULL_USER_NAME, fullUserName);

					spEdit.commit();
				}

				intent.putExtra(C.USER_NAME, mUser);
				intent.putExtra(C.USER_PASS, mPassword);
				intent.putExtra(C.EVENT_NUM, mEvent);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
			else if (success.equals((Integer)1)){
				etPass.setError(getString(R.string.error_incorrect_pass_event));
				etEvent.setError(getString(R.string.error_incorrect_pass_event));
				etEvent.requestFocus();
			}
			else if (success.equals((Integer)2)) {
				etUser.setError(getString(R.string.error_double_registration));
				registerRequest = false;
				etUser.requestFocus();
			}
			else if (success.equals((Integer)3)) {
				etEvent.setError(getString(R.string.error_no_such_event));
				registerRequest = false;
				etEvent.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

}
