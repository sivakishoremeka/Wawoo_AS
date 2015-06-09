package com.wawoo.mobile;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.wawoo.data.ActivePlanDatum;
import com.wawoo.data.DeviceDatum;
import com.wawoo.mobile.R;
import com.wawoo.retrofit.OBSClient;

public class AuthenticationAcitivity extends Activity {
	public static String TAG = AuthenticationAcitivity.class.getName();
	private ProgressBar mProgressBar;
	private Button mBtnRefresh;
	MyApplication mApplication = null;
	OBSClient mOBSClient;
	boolean mIsReqCanceled = false;
	boolean mIsFailed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication);
		setTitle("");
		mApplication = ((MyApplication) getApplicationContext());
		mOBSClient = mApplication.getOBSClient();
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mBtnRefresh = (Button) findViewById(R.id.btn_refresh);
		validateDevice();
	}

	private void validateDevice() {
		if (!mProgressBar.isShown()) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
		String androidId = Settings.Secure.getString(getApplicationContext()
				.getContentResolver(), Settings.Secure.ANDROID_ID);
		mOBSClient.getMediaDevice(androidId, deviceCallBack);
	}

	final Callback<List<ActivePlanDatum>> activePlansCallBack = new Callback<List<ActivePlanDatum>>() {

		@Override
		public void success(List<ActivePlanDatum> list, Response arg1) {
			if (!mIsReqCanceled) {
				/** on success if client has active plans redirect to home page */
				if (list != null && list.size() > 0) {
					Intent intent = new Intent(AuthenticationAcitivity.this,
							MainActivity.class);
					AuthenticationAcitivity.this.finish();
					startActivity(intent);
				} else {
					Intent intent = new Intent(AuthenticationAcitivity.this,
							PlanActivity.class);
					AuthenticationAcitivity.this.finish();
					startActivity(intent);
				}
			} else
				mIsReqCanceled = false;
		}

		@Override
		public void failure(RetrofitError retrofitError) {
			if (!mIsReqCanceled) {
				mIsFailed = true;
				mBtnRefresh.setVisibility(View.VISIBLE);
				Toast.makeText(
						AuthenticationAcitivity.this,
						"Server Error : "
								+ retrofitError.getResponse().getStatus(),
						Toast.LENGTH_LONG).show();
			} else
				mIsReqCanceled = false;
		}
	};

	final Callback<DeviceDatum> deviceCallBack = new Callback<DeviceDatum>() {

		@Override
		public void success(DeviceDatum device, Response arg1) {
			if (!mIsReqCanceled) {
				if (device != null) {
					try {
						/** on success save client id and check for active plans */
						mApplication.setClientId(Long.toString(device
								.getClientId()));
						mApplication.setBalance(device.getBalanceAmount());
						mApplication.setBalanceCheck(device.isBalanceCheck());
						mApplication.setCurrency(device.getCurrency());
						boolean isPayPalReq = false;
						if (device.getPaypalConfigData() != null)
							isPayPalReq = device.getPaypalConfigData()
									.getEnabled();
						mApplication.setPayPalCheck(isPayPalReq);
						if (isPayPalReq) {
							String value = device.getPaypalConfigData()
									.getValue();
							if (value != null && value.length() > 0) {
								JSONObject json = new JSONObject(value);
								try {
									if (json != null) {
										mApplication.setPayPalClientID(json
												.get("clientId").toString());
									}
								} catch (NullPointerException npe) {
									Log.e("AuthenticationAcitivity",
											(npe.getMessage() == null) ? "NPE Exception"
													: npe.getMessage());
									Toast.makeText(
											AuthenticationAcitivity.this,
											"Invalid Data for PayPal details",
											Toast.LENGTH_LONG).show();
								}
							} else
								Toast.makeText(AuthenticationAcitivity.this,
										"Invalid Data for PayPal details",
										Toast.LENGTH_LONG).show();
						}
						mOBSClient.getActivePlans(mApplication.getClientId(),
								activePlansCallBack);
					} catch (NullPointerException npe) {
						Log.e("AuthenticationAcitivity",
								(npe.getMessage() == null) ? "NPE Exception"
										: npe.getMessage());
						Toast.makeText(AuthenticationAcitivity.this,
								"Invalid Data-NPE Exception", Toast.LENGTH_LONG)
								.show();
					} catch (JSONException e) {
						Log.e("AuthenticationAcitivity",
								(e.getMessage() == null) ? "Json Exception" : e
										.getMessage());
						Toast.makeText(AuthenticationAcitivity.this,
								"Invalid Data-Json Exception",
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(AuthenticationAcitivity.this,
							"Server Error  :Device details not exists",
							Toast.LENGTH_LONG).show();
				}
			} else
				mIsReqCanceled = false;
		}

		@Override
		public void failure(RetrofitError retrofitError) {
			if (!mIsReqCanceled) {
				mIsFailed = true;
				if (mProgressBar.isShown()) {
					mProgressBar.setVisibility(View.INVISIBLE);
				}
				mBtnRefresh.setVisibility(View.VISIBLE);
				if (retrofitError.isNetworkError()) {
					Toast.makeText(
							AuthenticationAcitivity.this,
							getApplicationContext().getString(
									R.string.error_network), Toast.LENGTH_LONG)
							.show();
				} else if (retrofitError.getResponse().getStatus() == 403) {
					Intent intent = new Intent(AuthenticationAcitivity.this,
							RegisterActivity.class);
					AuthenticationAcitivity.this.finish();
					startActivity(intent);
				} else if (retrofitError.getResponse().getStatus() == 401) {
					Toast.makeText(AuthenticationAcitivity.this,
							"Authorization Failed", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(
							AuthenticationAcitivity.this,
							"Server Error : "
									+ retrofitError.getResponse().getStatus(),
							Toast.LENGTH_LONG).show();
				}
			} else
				mIsReqCanceled = false;
		}
	};

	public void Refresh_OnClick(View v) {
		mBtnRefresh.setVisibility(View.INVISIBLE);
		validateDevice();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mIsFailed) {
				AuthenticationAcitivity.this.finish();
			} else {
				AlertDialog mConfirmDialog = mApplication
						.getConfirmDialog(this);
				mConfirmDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (mProgressBar.isShown()) {
									mProgressBar.setVisibility(View.INVISIBLE);
								}
								mIsReqCanceled = true;
								AuthenticationAcitivity.this.finish();
							}
						});
				mConfirmDialog.show();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
