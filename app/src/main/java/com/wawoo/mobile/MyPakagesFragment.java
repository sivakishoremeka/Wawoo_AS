package com.wawoo.mobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wawoo.adapter.PackageAdapter;
import com.wawoo.adapter.PaytermAdapter;
import com.wawoo.data.OrderDatum;
import com.wawoo.data.Paytermdatum;
import com.wawoo.data.PlanDatum;
import com.wawoo.data.ResponseObj;
import com.wawoo.mobile.MyApplication.DoBGTasks;
import com.wawoo.parser.JSONPaytermConverter;
import com.wawoo.retrofit.CustomUrlConnectionClient;
import com.wawoo.retrofit.OBSClient;
import com.wawoo.service.DoBGTasksService;
import com.wawoo.utils.Utilities;

public class MyPakagesFragment extends Fragment {
	public static String TAG = MyPakagesFragment.class.getName();
	private final static String NETWORK_ERROR = "Network error.";
	private static String NEW_PLANS_DATA;
	private static String MY_PLANS_DATA;
	public static int selGroupId = -1;
	public static int selPaytermId = -1;
	public static int orderId = -1;
	private final static String PREPAID_PLANS = "Prepaid plans";
	private final static String MY_PLANS = "My plans";
	private ProgressDialog mProgressDialog;
	MyApplication mApplication = null;
	boolean mIsReqCanceled = false;
	Activity mActivity;
	View mRootView;
	List<PlanDatum> mPrepaidPlans;
	List<Paytermdatum> mPayterms;
	List<PlanDatum> mMyPlans;
	List<PlanDatum> mNewPlans;
	List<OrderDatum> mMyOrders;
	PackageAdapter listAdapter;
	ExpandableListView mExpListView;
	PaytermAdapter  mListAdapter;
	ListView mPaytermLv;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		mActivity = getActivity();
		mApplication = ((MyApplication) mActivity.getApplicationContext());
		// mOBSClient = mApplication.getOBSClient(mActivity);
		setHasOptionsMenu(true);
		selGroupId = -1;
		orderId = -1;
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mRootView = inflater.inflate(R.layout.fragment_my_packages, container,
				false);

		TextView tv_title = (TextView) mRootView
				.findViewById(R.id.a_plan_tv_selpkg);
		tv_title.setText(R.string.choose_plan_change);

		Button btnNext = (Button) mRootView
				.findViewById(R.id.a_plan_btn_submit);
		btnNext.setText(R.string.next);

		NEW_PLANS_DATA = mApplication.getResources().getString(
				R.string.new_plans_data);
		MY_PLANS_DATA = mApplication.getResources().getString(
				R.string.my_plans_data);
		String newPlansJson = mApplication.getPrefs().getString(NEW_PLANS_DATA,
				"");
		String myPlansJson = mApplication.getPrefs().getString(MY_PLANS_DATA,
				"");
		if (newPlansJson != null && newPlansJson.length() != 0) {
			mNewPlans = getPlanListFromJSON(newPlansJson);
		}
		if (myPlansJson != null && myPlansJson.length() != 0) {
			mMyPlans = getPlanListFromJSON(myPlansJson);
			buildPlansList();
		} else {
			getPlansFromServer();
		}
		return mRootView;
	}

	private void buildPlansList() {
		
		(mRootView.findViewById(R.id.f_my_pkg_exlv_my_plans)).setVisibility(View.VISIBLE);
		(mRootView.findViewById(R.id.f_my_pkg_payterm_lv)).setVisibility(View.GONE);
		
		TextView tv_title = (TextView) mRootView.findViewById(R.id.a_plan_tv_selpkg);
		tv_title.setText(R.string.sel_pkg);
		
		Button btnNext = (Button) mRootView.findViewById(R.id.a_plan_btn_submit);
		btnNext.setText(R.string.next);
		
		if (mMyPlans != null && mMyPlans.size() > 0) {
			mExpListView = (ExpandableListView) mRootView
					.findViewById(R.id.f_my_pkg_exlv_my_plans);
			listAdapter = new PackageAdapter(mActivity, mMyPlans);
			mExpListView.setAdapter(listAdapter);
			mExpListView.setOnGroupClickListener(new OnGroupClickListener() {
				@Override
				public boolean onGroupClick(ExpandableListView parent, View v,
						int groupPosition, long id) {

					RadioButton rb1 = (RadioButton) v
							.findViewById(R.id.plan_list_plan_rb);
					if (null != rb1 && (!rb1.isChecked())) {
						selGroupId = groupPosition;
					} else {
						selGroupId = -1;
					}
					return false;
				}
			});
		}
	}

	private void getPlansFromServer() {
		getPlans(PREPAID_PLANS);
	}

	private void getMyPlansFromServer() {
		getPlans(MY_PLANS);
	}

	public void getPlans(String planType) {
		boolean showProgressDialog = false;
		if (mProgressDialog != null)
			if (mProgressDialog.isShowing()) {
				// do nothing
			} else {
				showProgressDialog = true;
			}
		else {
			showProgressDialog = true;
		}
		if (showProgressDialog) {
			mProgressDialog = null;
			mProgressDialog = new ProgressDialog(mActivity,
					ProgressDialog.THEME_HOLO_DARK);
			mProgressDialog.setMessage("Connecting Server");
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {

				public void onCancel(DialogInterface arg0) {
					if (mProgressDialog.isShowing())
						mProgressDialog.dismiss();
					mIsReqCanceled = true;
				}
			});
			mProgressDialog.show();
		}

		if (PREPAID_PLANS.equalsIgnoreCase(planType)) {
			OBSClient mOBSClient = mApplication.getOBSClient();
			mOBSClient.getPrepaidPlans(getPrepaidPlansCallBack);
		} else if (MY_PLANS.equalsIgnoreCase(planType)) {
			OBSClient mOBSClient = null;
			RestAdapter restAdapter = new RestAdapter.Builder()
					.setEndpoint(MyApplication.API_URL)
					.setLogLevel(RestAdapter.LogLevel.NONE)
					/** Need to change Log level to NONe */
					.setConverter(new JSONConverter())
					.setClient(
							new com.wawoo.retrofit.CustomUrlConnectionClient(
									MyApplication.tenentId,
									MyApplication.basicAuth,
									MyApplication.contentType)).build();
			mOBSClient = restAdapter.create(OBSClient.class);
			mOBSClient.getClinetPackageDetails(mApplication.getClientId(),
					getMyPlansCallBack);
		}
	}

	final Callback<List<PlanDatum>> getPrepaidPlansCallBack = new Callback<List<PlanDatum>>() {
		@Override
		public void failure(RetrofitError retrofitError) {
			if (!mIsReqCanceled) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (retrofitError.isNetworkError()) {
					Toast.makeText(mActivity,
							mApplication.getString(R.string.error_network),
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(
							mActivity,
							"Server Error : "
									+ retrofitError.getResponse().getStatus(),
							Toast.LENGTH_LONG).show();
				}
			} else
				mIsReqCanceled = true;
		}

		@Override
		public void success(List<PlanDatum> planList, Response response) {
			if (!mIsReqCanceled) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (planList != null) {
					mPrepaidPlans = planList;
					getMyPlansFromServer();
				}
			} else
				mIsReqCanceled = true;
		}
	};

	Callback<List<OrderDatum>> getMyPlansCallBack = new Callback<List<OrderDatum>>() {

		@Override
		public void success(List<OrderDatum> orderList, Response response) {
			if (!mIsReqCanceled) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (orderList == null || orderList.size() == 0) {
					
					BufferedReader reader;
					String line;
					StringBuilder builder = new StringBuilder();
					try {
						reader = new BufferedReader(
								new InputStreamReader(response.getBody().in()));
						
						while ((line = reader.readLine()) != null) {
							builder.append(line);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					(mRootView.findViewById(R.id.f_my_pkg_exlv_my_plans)).setVisibility(View.GONE);
					(mRootView.findViewById(R.id.f_my_pkg_payterm_lv)).setVisibility(View.GONE);
					
					Toast.makeText(mActivity, "No Active Plans.",
							Toast.LENGTH_LONG).show();
					
					
				} else {
					mMyOrders = orderList;
					CheckPlansnUpdate();
				}

			} else
				mIsReqCanceled = false;

		}

		@Override
		public void failure(RetrofitError retrofitError) {
			if (!mIsReqCanceled) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (retrofitError.isNetworkError()) {
					Toast.makeText(mActivity,
							mApplication.getString(R.string.error_network),
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(
							mActivity,
							"Server Error : "
									+ retrofitError.getResponse().getStatus(),
							Toast.LENGTH_LONG).show();
				}
			} else
				mIsReqCanceled = false;

		}
	};

	private void CheckPlansnUpdate() {
		mNewPlans = new ArrayList<PlanDatum>();
		mMyPlans = new ArrayList<PlanDatum>();
		if (null != mPrepaidPlans && null != mMyOrders
				&& mPrepaidPlans.size() > 0 && mMyOrders.size() > 0) {
			for (PlanDatum plan : mPrepaidPlans) {
				int planId = plan.getId();
				boolean isNew = true;
				String sOrderId = null;
				for (int i = 0; i < mMyOrders.size(); i++) {
					if (mMyOrders.get(i).getPdid() == planId
							&& mMyOrders.get(i).status
									.equalsIgnoreCase("ACTIVE")) {
						isNew = false;
						sOrderId = mMyOrders.get(i).orderId;
					}
				}
				if (isNew) {
					mNewPlans.add(plan);
				} else {
					plan.orderId = sOrderId;
					mMyPlans.add(plan);
				}
			}
		}

		boolean savePlans = false;
		if (null != mNewPlans && mNewPlans.size() != 0) {
			mApplication.getEditor().putString(NEW_PLANS_DATA,
					new Gson().toJson(mNewPlans));
			savePlans = true;
		}
		if (null != mMyPlans && mMyPlans.size() != 0) {
			mApplication.getEditor().putString(MY_PLANS_DATA,
					new Gson().toJson(mMyPlans));
			savePlans = true;
		}
		if (savePlans)
			mApplication.getEditor().commit();
		buildPlansList();

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.nav_menu, menu);
		MenuItem refreshItem = menu.findItem(R.id.action_refresh);
		refreshItem.setVisible(true);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_home:
			startActivity(new Intent(getActivity(), MainActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			break;
		case R.id.action_refresh:
			Button btn = (Button) mRootView.findViewById(R.id.a_plan_btn_submit);
			if (btn.getText().toString()
					.equalsIgnoreCase(getString(R.string.subscribe))) {
				selGroupId = -1;
				TextView tv_title = (TextView) mRootView
						.findViewById(R.id.a_plan_tv_selpkg);
				tv_title.setText(R.string.choose_plan_change);

				btn.setText(R.string.next);
			}
			getPlansFromServer();
			break;
		case R.id.action_logout:
			logout();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	static class JSONConverter implements Converter {

		@Override
		public List<OrderDatum> fromBody(TypedInput typedInput, Type type)
				throws ConversionException {
			List<OrderDatum> ordersList = null;

			try {
				String json = MyApplication.getJSONfromInputStream(typedInput
						.in());

				JSONObject jsonObj;
				jsonObj = new JSONObject(json);
				JSONArray arrOrders = jsonObj.getJSONArray("clientOrders");
				ordersList = getOrdersFromJson(arrOrders.toString());

			} catch (IOException e) {
				Log.i(TAG, e.getMessage());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ordersList;
		}

		@Override
		public TypedOutput toBody(Object o) {
			return null;
		}

	}

	public static List<OrderDatum> getOrdersFromJson(String json) {
		List<OrderDatum> ordersList = new ArrayList<OrderDatum>();
		try {

			JSONArray arrOrders = new JSONArray(json);
			for (int i = 0; i < arrOrders.length(); i++) {

				JSONObject obj = arrOrders.getJSONObject(i);
				if ("ACTIVE".equalsIgnoreCase(obj.getString("status"))) {
					OrderDatum order = new OrderDatum();
					order.setOrderId(obj.getString("id"));
					order.setPlanCode(obj.getString("planCode"));
					order.setPdid(obj.getInt("pdid"));
					order.setPrice(obj.getString("price"));
					order.setStatus(obj.getString("status"));
					try {
						JSONArray arrDate = obj.getJSONArray("activeDate");
						Date date = MyApplication.df.parse(arrDate.getString(0)
								+ "-" + arrDate.getString(1) + "-"
								+ arrDate.getString(2));
						order.setActiveDate(MyApplication.df.format(date));
					} catch (JSONException e) {
						order.setActiveDate(obj.getString("activeDate"));
					}
					try {
						JSONArray arrDate = obj.getJSONArray("invoiceTilldate");
						Date date = MyApplication.df.parse(arrDate.getString(0)
								+ "-" + arrDate.getString(1) + "-"
								+ arrDate.getString(2));
						order.setInvoiceTilldate(MyApplication.df.format(date));
					} catch (JSONException e) {
						try {
							order.setInvoiceTilldate(obj
									.getString("invoiceTilldate"));
						} catch (JSONException ex) {
							// no invoice till date.
						}
					}
					ordersList.add(order);
				}
			}
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
		}
		return ordersList;
	}

	public void btnSubmit_onClick(View v) {
		if (((Button) v).getText().toString()
				.equalsIgnoreCase(getString(R.string.next))) {
			if (selGroupId == -1) {
				Toast.makeText(getActivity(), "Select a Plan to Change",
						Toast.LENGTH_LONG).show();
			} else {
				((Button) v).setEnabled(false);
				orderId = Integer
						.parseInt(mMyPlans.get(selGroupId).orderId);
				selGroupId = -1;
				TextView tv_title = (TextView) mRootView
						.findViewById(R.id.a_plan_tv_selpkg);
				tv_title.setText(R.string.choose_plan_sub);

				((Button) v).setText(R.string.next2);
				mExpListView = (ExpandableListView) mRootView
						.findViewById(R.id.f_my_pkg_exlv_my_plans);
				listAdapter = null;
				if (mNewPlans != null && mNewPlans.size() > 0) {
					listAdapter = new PackageAdapter(mActivity, mNewPlans);
					mExpListView.setAdapter(listAdapter);
					mExpListView
							.setOnGroupClickListener(new OnGroupClickListener() {
								@Override
								public boolean onGroupClick(
										ExpandableListView parent, View v,
										int groupPosition, long id) {

									RadioButton rb1 = (RadioButton) v
											.findViewById(R.id.plan_list_plan_rb);
									if (null != rb1 && (!rb1.isChecked())) {
										selGroupId = groupPosition;
									} else {
										selGroupId = -1;
									}
									return false;
								}
							});
					((Button) v).setEnabled(true);
				} else {
					mExpListView.setAdapter(listAdapter);
					Toast.makeText(getActivity(), "No new Plans",
							Toast.LENGTH_LONG).show();
				}
			}
		}else if (((Button) v).getText().toString()
				.equalsIgnoreCase(getString(R.string.next2))) {
			if(selGroupId!=-1){
				((Button) v).setEnabled(false);
				getPaytermsforSelPlan();
			}
			else{
				Toast.makeText(getActivity(), "Choose a Plan to Subscribe", Toast.LENGTH_LONG).show();
			}
		} 
		else if (((Button) v).getText().toString()
				.equalsIgnoreCase(getString(R.string.subscribe))) {
			if (selGroupId !=-1) {
				((Button) v).setEnabled(false);
				changePlan(mNewPlans.get(selGroupId).toString());
			} else {
				Toast.makeText(getActivity().getApplicationContext(),
						"Select a Plan", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void getPaytermsforSelPlan(){
		RestAdapter restAdapter = new RestAdapter.Builder()
					.setEndpoint(MyApplication.API_URL)
					.setLogLevel(RestAdapter.LogLevel.NONE)
					.setConverter(new JSONPaytermConverter())
					.setClient(
							new CustomUrlConnectionClient(MyApplication.tenentId,
									MyApplication.basicAuth,
									MyApplication.contentType)).build();
			 OBSClient client = restAdapter.create(OBSClient.class);
			//CLIENT_DATA = mApplication.getResources().getString(
			//		R.string.client_data);
			//mPrefs = mActivity.getSharedPreferences(mApplication.PREFS_FILE, 0);
			//mClinetData = mPrefs.getString(CLIENT_DATA, "");		 
			 if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				mProgressDialog = new ProgressDialog(getActivity(),
						ProgressDialog.THEME_HOLO_DARK);
				mProgressDialog.setMessage("Connecting Server");
				mProgressDialog.setCanceledOnTouchOutside(false);
				mProgressDialog.setOnCancelListener(new OnCancelListener() {

					public void onCancel(DialogInterface arg0) {
						if (mProgressDialog.isShowing())
							mProgressDialog.dismiss();
						mIsReqCanceled = true;
					}
				});
				mProgressDialog.show();
				client.getPlanPayterms((mNewPlans.get(selGroupId).getId())+"",getPlanPayterms);	
		}
	
	final Callback<List<Paytermdatum>> getPlanPayterms = new Callback<List<Paytermdatum>>() {
		@Override
		public void failure(RetrofitError retrofitError) {
			if (!mIsReqCanceled) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (retrofitError.isNetworkError()) {
					Toast.makeText(
							getActivity(),
							getActivity().getApplicationContext().getString(
									R.string.error_network), Toast.LENGTH_LONG)
							.show();
				} else {
					Toast.makeText(
							getActivity(),
							"No Payterms for this plan.", Toast.LENGTH_LONG)
							.show();
					//Toast.makeText(
					//		getActivity(),
					//		"Server Error : "
					//				+ retrofitError.getResponse().getStatus(),
					//		Toast.LENGTH_LONG).show();
				}
			} else
				mIsReqCanceled = false;
			Button b = (Button) mRootView.findViewById(R.id.a_plan_btn_submit);
			b.setEnabled(true);
			selGroupId = -1;
		}

		@Override
		public void success(List<Paytermdatum> payterms, Response response) {
			if (!mIsReqCanceled) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (payterms != null) {
					mPayterms = payterms;
					buildPaytermList();
				}
			} else
				mIsReqCanceled = false;
		}
	};


	private void buildPaytermList(){
		
		(mRootView.findViewById(R.id.f_my_pkg_exlv_my_plans)).setVisibility(View.GONE);
		(mRootView.findViewById(R.id.f_my_pkg_payterm_lv)).setVisibility(View.VISIBLE);
		
		TextView tv_title = (TextView) mRootView.findViewById(R.id.a_plan_tv_selpkg);
		tv_title.setText(R.string.choose_payterm);
		
		mPaytermLv = (ListView) mRootView.findViewById(R.id.f_my_pkg_payterm_lv);
		mListAdapter = null;
		if (mPayterms != null && mPayterms.size() > 0) {
			boolean isNewPlan=false;
			mListAdapter = new PaytermAdapter(getActivity(), mPayterms,isNewPlan);
			mPaytermLv.setAdapter(mListAdapter);			
			mPaytermLv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					RadioButton rb1 = (RadioButton) view
							.findViewById(R.id.a_plan_payterm_row_rb);
					if (null != rb1 && (!rb1.isChecked())) {
						selPaytermId = position;
					} else {
						selPaytermId = -1;
					}
				}
			});
			Button b = (Button) mRootView.findViewById(R.id.a_plan_btn_submit);
			b.setText(R.string.subscribe);
			b.setEnabled(true);
		} else {
			mPaytermLv.setAdapter(null);
			Toast.makeText(getActivity(), "No Payterms for this Plan",
					Toast.LENGTH_LONG).show();
		}		
	}
	
	public void changePlan(String planid) {
		new ChangePlansAsyncTask().execute();
	}

	private class ChangePlansAsyncTask extends
			AsyncTask<Void, Void, ResponseObj> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			boolean showProgressDialog = false;
			if (mProgressDialog != null)
				if (mProgressDialog.isShowing()) {
					// do nothing
				} else {
					showProgressDialog = true;
				}
			else {
				showProgressDialog = true;
			}
			if (showProgressDialog) {
				mProgressDialog = new ProgressDialog(getActivity(),
						ProgressDialog.THEME_HOLO_DARK);
				mProgressDialog.setMessage("Processing Order...");
				mProgressDialog.setCanceledOnTouchOutside(false);
				mProgressDialog.setOnCancelListener(new OnCancelListener() {

					public void onCancel(DialogInterface arg0) {
						if (mProgressDialog.isShowing())
							mProgressDialog.dismiss();
						cancel(true);
					}
				});
				mProgressDialog.show();
			}
		}

		@Override
		protected ResponseObj doInBackground(Void... params) {
			PlanDatum plan = mNewPlans.get(selGroupId);
			ResponseObj resObj = new ResponseObj();
			if (Utilities.isNetworkAvailable(getActivity()
					.getApplicationContext())) {
				HashMap<String, String> map = new HashMap<String, String>();
				//Object[] arrStartDate = plan.getStartDate().toArray();
				Date discDate = new Date();
				SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy",
						new Locale("en"));
				/*Date startDate = new Date((Integer) arrStartDate[0],
						(Integer) arrStartDate[1], (Integer) arrStartDate[2]);*/
				String strStartDate = df.format(discDate);
				String strDiscDate = df.format(discDate);
				map.put("TagURL", "/orders/changePlan/" + orderId);
				map.put("planCode", plan.getId().toString());
				map.put("dateFormat", "dd MMMM yyyy");
				map.put("locale", "en");
				map.put("contractPeriod", mPayterms.get(selPaytermId).getId()+"");
				map.put("isNewplan", "false");
				map.put("start_date", strStartDate);
				map.put("disconnectionDate", strDiscDate);
				map.put("disconnectReason", "Not Interested");
				map.put("billAlign", "false");
				map.put("paytermCode", mPayterms.get(selPaytermId).getPaytermtype());
				resObj = Utilities.callExternalApiPutMethod(getActivity()
						.getApplicationContext(), map);
			} else {
				resObj.setFailResponse(100, NETWORK_ERROR);
			}
			return resObj;
		}

		@Override
		protected void onPostExecute(ResponseObj resObj) {
			super.onPostExecute(resObj);
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			if (resObj.getStatusCode() == 200) {
				// update balance config n Values
				Toast.makeText(mActivity, "Plan Change Success",
						Toast.LENGTH_LONG).show();
				Intent intent = new Intent(mActivity, DoBGTasksService.class);
				intent.putExtra(DoBGTasksService.TASK_ID,
						DoBGTasks.UPDATESERVICES_CONFIGS.ordinal());
				mActivity.startService(intent);
				UpdateUI();
				//CheckBalancenGetData();
			} else {
				Toast.makeText(getActivity(), resObj.getsErrorMessage(),
						Toast.LENGTH_LONG).show();
			}
			
			Button b = (Button) mRootView.findViewById(R.id.a_plan_btn_submit);
			b.setEnabled(true);
		}
	}
	public void onBackPressed() {
		Button btn = (Button) mRootView.findViewById(R.id.a_plan_btn_submit);
		if (btn.getText().toString()
				.equalsIgnoreCase(getString(R.string.subscribe))) {
			//selGroupId = -1;
			selPaytermId = -1;
			(mRootView.findViewById(R.id.f_my_pkg_exlv_my_plans)).setVisibility(View.VISIBLE);
			(mRootView.findViewById(R.id.f_my_pkg_payterm_lv)).setVisibility(View.GONE);
			
			TextView tv_title = (TextView) mRootView
					.findViewById(R.id.a_plan_tv_selpkg);
			tv_title.setText(R.string.choose_plan_change);

			btn.setText(R.string.next2);
			mExpListView = (ExpandableListView) mRootView
					.findViewById(R.id.f_my_pkg_exlv_my_plans);

			if (mNewPlans != null && mNewPlans.size() > 0) {
				mExpListView = (ExpandableListView) mRootView
						.findViewById(R.id.f_my_pkg_exlv_my_plans);
				listAdapter = new PackageAdapter(mActivity, mNewPlans);
				mExpListView.setAdapter(listAdapter);
				mExpListView
						.setOnGroupClickListener(new OnGroupClickListener() {
							@Override
							public boolean onGroupClick(
									ExpandableListView parent, View v,
									int groupPosition, long id) {

								RadioButton rb1 = (RadioButton) v
										.findViewById(R.id.plan_list_plan_rb);
								if (null != rb1 && (!rb1.isChecked())) {
									selGroupId = groupPosition;
								} else {
									selGroupId = -1;
								}
								return false;
							}
						});
			}
		}else if (btn.getText().toString()
				.equalsIgnoreCase(getString(R.string.next2))) {
			selGroupId = -1;
			TextView tv_title = (TextView) mRootView
					.findViewById(R.id.a_plan_tv_selpkg);
			tv_title.setText(R.string.choose_plan_change);

			btn.setText(R.string.next);
			
			mExpListView = (ExpandableListView) mRootView
					.findViewById(R.id.f_my_pkg_exlv_my_plans);

			if (mMyPlans != null && mMyPlans.size() > 0) {
				mExpListView = (ExpandableListView) mRootView
						.findViewById(R.id.f_my_pkg_exlv_my_plans);
				listAdapter = new PackageAdapter(mActivity, mMyPlans);
				mExpListView.setAdapter(listAdapter);
				mExpListView
						.setOnGroupClickListener(new OnGroupClickListener() {
							@Override
							public boolean onGroupClick(
									ExpandableListView parent, View v,
									int groupPosition, long id) {

								RadioButton rb1 = (RadioButton) v
										.findViewById(R.id.plan_list_plan_rb);
								if (null != rb1 && (!rb1.isChecked())) {
									selGroupId = groupPosition;
								} else {
									selGroupId = -1;
								}
								return false;
							}
						});
			}
		}
		else
			getActivity().finish();
	}

	protected void UpdateUI() {
		selGroupId = -1;
		TextView tv_title = (TextView) mRootView
				.findViewById(R.id.a_plan_tv_selpkg);
		tv_title.setText(R.string.choose_plan_change);
		Button btn = (Button) mRootView.findViewById(R.id.a_plan_btn_submit);
		btn.setText(R.string.next);
		getPlansFromServer();
	}

	private List<PlanDatum> getPlanListFromJSON(String json) {
		java.lang.reflect.Type t = new TypeToken<List<PlanDatum>>() {
		}.getType();
		return new Gson().fromJson(json, t);
	}
	public void logout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
				AlertDialog.THEME_HOLO_LIGHT);
		builder.setIcon(R.drawable.ic_logo_confirm_dialog);
		builder.setTitle("Confirmation");
		builder.setMessage("Are you sure to Logout?");
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int buttonId) {
					}
				});
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						// Clear shared preferences..
						((MyApplication) getActivity().getApplicationContext()).clearAll();
						// close all activities..
						Intent Closeintent = new Intent(getActivity(),
								MainActivity.class);
						// set the new task and clear flags
						Closeintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
								| Intent.FLAG_ACTIVITY_CLEAR_TOP);
						Closeintent.putExtra("LOGOUT", true);
						startActivity(Closeintent);
						getActivity().finish();
					}
				});
		dialog.show();

	}

}
