package com.wawoo.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class MXPlayerActivity extends Activity {

	// MX-Player
	public static final String TAG = "mxvp.intent.test";

	public static final String MXVP = "com.mxtech.videoplayer.ad";
	public static final String MXVP_PRO = "com.mxtech.videoplayer.pro";

	public static final String MXVP_PLAYBACK_CLASS = "com.mxtech.videoplayer.ad.ActivityScreen";
	public static final String MXVP_PRO_PLAYBACK_CLASS = "com.mxtech.videoplayer.ActivityScreen";

	//public static final String RESULT_VIEW = "com.mxtech.intent.result.VIEW";
	public static final String EXTRA_DECODE_MODE = "decode_mode"; // (byte)
	public static final String EXTRA_VIDEO_LIST = "video_list";
	public static final String EXTRA_SUBTITLES = "subs";
	public static final String EXTRA_SUBTITLES_ENABLE = "subs.enable";
	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_POSITION = "position";
	public static final String EXTRA_RETURN_RESULT = "return_result";
	public static final String EXTRA_HEADERS = "headers";
	public static int INSTALL_MXPLAYER_MARKET = 9999;

	// MX-Player

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initiallizePlayer();
	}

	private void initiallizePlayer() {
		// TODO Auto-generated method stub
		Uri mUri = Uri.parse(getIntent().getStringExtra("URL"));
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(mUri, "application/*");
		// s/w decoder
		i.putExtra(EXTRA_DECODE_MODE, (byte) 2);
		// request result
		i.putExtra(EXTRA_RETURN_RESULT, true);
		String[] headers = new String[] { "User-Agent",
				"MX Player Caller App/1.0", "Extra-Header", "911" };
		i.putExtra(EXTRA_HEADERS, headers);
		try {
			i.setPackage(MXVP_PRO);
			PackageManager pm = getPackageManager();
			ResolveInfo info = pm.resolveActivity(i,
					PackageManager.MATCH_DEFAULT_ONLY);
			if (info == null) {
			//startActivityForResult(i, 0);
			//return;
		//} catch (ActivityNotFoundException e) {
		//}
		//try {
			i.setPackage(MXVP);
			i.setClassName(MXVP, MXVP_PLAYBACK_CLASS);
			//PackageManager
			//pm = getPackageManager();
			//ResolveInfo 
			info = pm.resolveActivity(i,
					PackageManager.MATCH_DEFAULT_ONLY);
			if (info == null) {
				AlertDialog mConfirmDialog = ((MyApplication) getApplicationContext())
						.getConfirmDialog((MyApplication) getApplicationContext());
				mConfirmDialog.setMessage("MXPlayer is not found in Device. Are you sure to download from Play Store.??");
				mConfirmDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent goToMarket = new Intent(Intent.ACTION_VIEW)
							    .setData(Uri.parse("market://details?id=com.mxtech.videoplayer.ad&hl=en"));
								startActivityForResult(goToMarket,1);
							}
						});
				mConfirmDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});
				mConfirmDialog.show();
			} else{
			startActivityForResult(i, 1);
			return;
			}
			}
			else
			{
				startActivityForResult(i, 1);
				return;
			}
		} catch (ActivityNotFoundException e2) {
			Log.e("MxException", e2.getMessage().toString());
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==1){
			initiallizePlayer();
		}
		else{
		finish();
		if (resultCode != RESULT_OK) {
			return;
		}
		// handle result.
		Uri lastVideoUri = data.getData();
		byte lastDecodingMode = data.getByteExtra(EXTRA_DECODE_MODE, (byte) 0);
		int lastPosition = data.getIntExtra(EXTRA_POSITION, 0);
		Log.i(TAG, "OK: " + lastVideoUri + " last-decoding-mode="
				+ lastDecodingMode + " last-position=" + lastPosition);
	}
	}
}
