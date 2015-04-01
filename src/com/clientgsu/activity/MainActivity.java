package com.clientgsu.activity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONObject;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.media.FaceDetector;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.BatteryManager;
import android.os.PowerManager;

import com.clientgsu.data.RectangleFace;
import com.example.clientgsu.R;
import com.geag.rmi.GeagRmiInterface;

public class MainActivity extends ActionBarActivity {

	private static final int SELECT_PICTURE = 1;
	private ImageView img1;
	private Bitmap bitmap;
	private EditText textIp;
	private TextView textA1, textB1, textC1, textD1, textTotal,
			textStartTimestamp, textEndTimestamp;
	private List<RectangleFace> rectangleFaceList = null;
	private Long timeDiff = 0L;
	InputStream inputStream = null;
	BufferedInputStream bufferedInputStream = null;
	private boolean isLocalProcessing = false;
	Long aStartTime, aEndTime, cStartTime, cEndTime, dStartTime, dEndTime,
			eStartTime, eEndTime = 0L;
	InputStream byteInputStream = null;
	ProgressDialog progress;
	final String TAG = "Hello World";
	private String serverTimeLasted = null;
	private FaceDetector myFaceDetect;
	private FaceDetector.Face[] faces;
	Long energyInitial = 0L;
	String imageRmiResponse = "";
	String exceptionText = "";

	// private BatteryManager mBatteryManager = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getBatteryInfo();

		/*
		 * BatteryManager mBatteryManager = (BatteryManager)
		 * this.getSystemService(BATTERY_SERVICE);
		 * 
		 * if(mBatteryManager==null) System.out.println("Battery Manager null");
		 * energyInitial = mBatteryManager.getLongProperty(BatteryManager.
		 * BATTERY_PROPERTY_ENERGY_COUNTER);
		 * System.out.println("Remaining energy = " + energyInitial + "nWh");
		 */
		setContentView(R.layout.activity_main);
		// System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		img1 = (ImageView) findViewById(R.id.ImageView01);

		textIp = (EditText) findViewById(R.id.editText1);
		textA1 = (TextView) findViewById(R.id.TextViewA1);
		textB1 = (TextView) findViewById(R.id.TextViewB1);
		textC1 = (TextView) findViewById(R.id.TextViewC1);
		textD1 = (TextView) findViewById(R.id.TextViewD1);
		textTotal = (TextView) findViewById(R.id.TextViewTotal);
		textStartTimestamp = (TextView) findViewById(R.id.TextViewStartTimestamp);
		textEndTimestamp = (TextView) findViewById(R.id.TextViewTotalFinalTimestamp);
		findViewById(R.id.buttonBrowse).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = new Intent();
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(
								Intent.createChooser(intent, "Select Picture"),
								SELECT_PICTURE);
					}
				});
		findViewById(R.id.buttonSend).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						new Thread() {
							public void run() {
								// TODO Run network requests here.
								new SendImageTask().execute("");
							}
						}.start();

					}
				});

		findViewById(R.id.buttonSendRmi).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						new Thread() {
							public void run() {
								// TODO Run network requests here.
								new RmiTask().execute("");
							}
						}.start();

					}
				});

		findViewById(R.id.buttonDoLocally).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						new Thread() {
							public void run() {
								// TODO Run network requests here.
								new LocalProcessingTask().execute("");
							}
						}.start();

					}
				});
	}

	private void getBatteryInfo() {
		BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
			int scale = -1;
			int level = -1;
			int voltage = -1;
			int temp = -1;

			@Override
			public void onReceive(Context context, Intent intent) {
				level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
				voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
				Log.e("BatteryManager", "level is " + level + "/" + scale
						+ ", temp is " + temp + ", voltage is " + voltage);
				System.out.println("BatteryManager level is " + level + "/"
						+ scale + ", temp is " + temp + ", voltage is "
						+ voltage);
			}
		};
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryReceiver, filter);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				if (bitmap != null) {
					bitmap.recycle();
				}
				try {
					inputStream = getContentResolver().openInputStream(
							data.getData());
					bufferedInputStream = new BufferedInputStream(inputStream);
					Bitmap bMap = BitmapFactory
							.decodeStream(bufferedInputStream);
					img1.setImageBitmap(bMap);
					if (inputStream != null) {
						inputStream.close();
					}
					if (bufferedInputStream != null) {
						bufferedInputStream.close();
					}
				} catch (FileNotFoundException e) {
					exceptionText = e.toString();
					displayAlert();
				} catch (IOException e) {
					exceptionText = e.toString();
					displayAlert();
				}
				try {
					inputStream.close();
				} catch (IOException e) {
					exceptionText = e.toString();
					displayAlert();
				}

			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class SendImageTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {

			// clearTextBoxValues();
			/*
			 * System.out.println("Remaining energy = " +
			 * BatteryManager.BATTERY_PROPERTY_CURRENT_NOW + "nWh");
			 */
			isLocalProcessing = false;

			aStartTime = System.currentTimeMillis();
			String imageAsString = "", line = "";
			try {
				imageAsString = prepareRawDataOfImage();

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						1);

				nameValuePairs.add(new BasicNameValuePair("imageContentData",
						imageAsString));

				HttpClient httpClient = new DefaultHttpClient();
				HttpConnectionParams.setConnectionTimeout(
						httpClient.getParams(), 100000);

				HttpPost httpPost = new HttpPost("http://" + textIp.getText()
						+ ":8080/Calculate_Server/rest/imageChanger/post");

				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
						nameValuePairs);
				entity.setContentType("application/json");
				httpPost.setEntity(entity);

				aEndTime = System.currentTimeMillis();
				// WifiManager mainWifiObj;
				// mainWifiObj = (WifiManager)
				// getSystemService(Context.WIFI_SERVICE);
				HttpResponse response = httpClient.execute(httpPost);
				cStartTime = System.currentTimeMillis();

				BufferedReader br = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));

				rectangleFaceList = new ArrayList<RectangleFace>();

				RectangleFace rectangleFace = null;
				JSONObject jsonObject = null;
				JSONArray array = null;
				while (null != (line = br.readLine())) {
					array = new JSONArray(line);
					for (int i = 0; i < array.length() - 1; i++) {
						jsonObject = array.getJSONObject(i);
						rectangleFace = new RectangleFace(
								jsonObject.getInt("x1"),
								jsonObject.getInt("x2"),
								jsonObject.getInt("y1"),
								jsonObject.getInt("y2"));
						rectangleFaceList.add(rectangleFace);

					}
					// last element in the jsonArray is "server lasted time"
					// information
					jsonObject = array.getJSONObject(array.length() - 1);
					serverTimeLasted = jsonObject.getString("serverTimeLasted");

				}
			} catch (UnsupportedEncodingException e) {
				exceptionText = e.toString();
				displayAlert();
			} catch (ClientProtocolException e) {
				exceptionText = e.toString();
			} catch (IOException e) {
				System.out.println("Server unaccessible");
				exceptionText = e.toString();
				displayAlert();
			} catch (Exception e) {
				System.out.println(line);
				exceptionText = e.toString();
				displayAlert();
			}

			return true;

		}

		protected void onPostExecute(Boolean doInBackground) {

			System.out.println("a Task completed");

			new Thread() {
				public void run() {
					// TODO Run network requests here.

					new UpdateImageTask().execute("");
				}
			}.start();

		}

	}

	private class UpdateImageTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					try {

						Bitmap createBitmap = Bitmap.createBitmap(
								img1.getWidth(), img1.getHeight(),
								Config.ARGB_8888);
						Canvas canvas = new Canvas(createBitmap);
						img1.draw(canvas);

						Paint p = new Paint();
						p.setColor(Color.GREEN);
						p.setStyle(Paint.Style.STROKE);
						int i = 0;

						for (RectangleFace rectangleFace : rectangleFaceList) {
							i++;
							canvas.drawRect(rectangleFace.getX1(),
									rectangleFace.getY1(),
									rectangleFace.getX2(),
									rectangleFace.getY2(), p);
						}

						img1.setImageBitmap(createBitmap);

					} catch (Exception e) {
						exceptionText = e.toString();
						displayAlert();
					}
				}
			});
			return true;
		}

		protected void onPostExecute(Boolean doInBackground) {

			if (!isLocalProcessing) {
				cEndTime = System.currentTimeMillis();
				textA1.setText("a1: " + (aEndTime - aStartTime) + " ms");
				textB1.setText("b1: " + serverTimeLasted + " ms");
				textC1.setText("c1: " + (cEndTime - cStartTime) + " ms");
				textTotal.setText("Total: " + (cEndTime - aStartTime) + " ms");
				textStartTimestamp.setText(String.valueOf(aStartTime));
				textEndTimestamp.setText(String.valueOf(cEndTime));

			} else {
				dEndTime = System.currentTimeMillis();
				// Log.d("d1:", dEndTime - dStartTime + " ms");
				textD1.setText("d1: " + (dEndTime - dStartTime) + " ms");
				textStartTimestamp.setText(String.valueOf(dStartTime));
				textEndTimestamp.setText(String.valueOf(dEndTime));

			}
		}

	}

	private class LocalProcessingTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {

			try {
				// clearTextBoxValues();
				/*
				 * System.out.println("Remaining energy = " +
				 * BatteryManager.BATTERY_PROPERTY_CURRENT_NOW + "nWh");
				 */
				isLocalProcessing = true;

				dStartTime = System.currentTimeMillis();
				img1 = (ImageView) findViewById(R.id.ImageView01);

				Bitmap bitmap = ((BitmapDrawable) img1.getDrawable())
						.getBitmap();

				// if bitmap width is even, then resize to enable face detection
				if ((1 == (bitmap.getWidth() % 2))) {
					bitmap = Bitmap.createScaledBitmap(bitmap,
							bitmap.getWidth() + 1, bitmap.getHeight(), false);
				}

				// getting new heights
				int height = bitmap.getHeight();
				int width = bitmap.getWidth();
				Log.d("initial size: ", "height: " + height + " width: "
						+ width);

				// config arrangements
				Bitmap bitmap565 = Bitmap.createBitmap(width, height,
						Config.RGB_565);

				Paint ditherPaint = new Paint();
				Paint drawPaint = new Paint();

				ditherPaint.setDither(true);
				drawPaint.setColor(Color.RED);
				drawPaint.setStyle(Paint.Style.STROKE);
				drawPaint.setStrokeWidth(2);

				Canvas canvas = new Canvas();
				canvas.setBitmap(bitmap565);
				canvas.drawBitmap(bitmap, 0, 0, ditherPaint);

				faces = new FaceDetector.Face[5];
				myFaceDetect = new FaceDetector(width, height, 5);
				int facesFound = myFaceDetect.findFaces(bitmap565, faces);
				height = bitmap565.getHeight();
				width = bitmap565.getWidth();
				Log.d("new size: ", "height: " + height + " width: " + width);

				String file_path = Environment.getExternalStorageDirectory()
						.getAbsolutePath() + "/PhysicsSketchpad";
				File dir = new File(file_path);
				if (!dir.exists())
					dir.mkdirs();
				File file = new File(dir, "sketch.png");
				FileOutputStream fOut = new FileOutputStream(file);

				bitmap565.compress(Bitmap.CompressFormat.PNG, 85, fOut);
				fOut.flush();
				fOut.close();

				Log.d("Face_Detection",
						"Face Count: " + String.valueOf(facesFound));

				Bitmap createBitmap = Bitmap.createBitmap(img1.getWidth(),
						img1.getHeight(), Config.ARGB_8888);
				Canvas canvas1 = new Canvas(createBitmap);
				img1.draw(canvas1);

				Paint p = new Paint();
				p.setColor(Color.GREEN);
				p.setStyle(Paint.Style.STROKE);
				float myEyesDistance;
				RectangleFace rectangleFace = null;
				rectangleFaceList = new ArrayList<RectangleFace>();
				PointF myMidPoint = new PointF();

				for (FaceDetector.Face detectedFace : faces) {

					detectedFace.getMidPoint(myMidPoint);

					myEyesDistance = detectedFace.eyesDistance();

					rectangleFace = new RectangleFace(
							(int) (myMidPoint.x - myEyesDistance),
							(int) (myMidPoint.x + myEyesDistance),
							(int) (myMidPoint.y - myEyesDistance),
							(int) (myMidPoint.y + myEyesDistance));
					rectangleFaceList.add(rectangleFace);
				}

			} catch (Exception e) {
				exceptionText = e.toString();
				displayAlert();
			}
			return true;

		}

		protected void onPostExecute(Boolean doInBackground) {

			System.out.println("SendImageTask completed");

			new Thread() {
				public void run() {
					// TODO Run network requests here.
					new UpdateImageTask().execute("");
				}
			}.start();

		}
	}

	private class RmiTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {
			// clearTextBoxValues();
			aStartTime = System.currentTimeMillis();

			Looper.prepare();

			try {
				String serverIp = textIp.getText().toString();
				CallHandler callHandler = new CallHandler();
				Client client = new Client(serverIp, 7777, callHandler);
				GeagRmiInterface geagRmiService = (GeagRmiInterface) client
						.getGlobal(GeagRmiInterface.class);

				String imageAsString = prepareRawDataOfImage();

				aEndTime = System.currentTimeMillis();

				String res = geagRmiService.getResponse(imageAsString);

				cStartTime = System.currentTimeMillis();
				rectangleFaceList = new ArrayList<RectangleFace>();

				String[] split = res.split(";");
				RectangleFace rectFace = null;

				String[] split2 = null;

				for (int i = 0; i < split.length-1; i++) {
					
						split2 = split[i].split(",");
						rectFace = new RectangleFace(
								Integer.parseInt(split2[0]),
								Integer.parseInt(split2[1]),
								Integer.parseInt(split2[2]),
								Integer.parseInt(split2[3]));
						rectangleFaceList.add(rectFace);	
				}
				serverTimeLasted = split[split.length-1];				

				client.close();
			} catch (IOException e) {
				exceptionText = e.toString();
				displayAlert();
			} catch (Exception e) {
				exceptionText = e.toString();
				displayAlert();
			}
			return true;

		}

		protected void onPostExecute(Boolean doInBackground) {

			System.out.println("RmiTask completed");

			new Thread() {
				public void run() {
					// TODO Run network requests here.
					new UpdateImageTask().execute("");
				}
			}.start();

		}

	}

	private String prepareRawDataOfImage() {
		img1 = (ImageView) findViewById(R.id.ImageView01);

		Bitmap bitmap = ((BitmapDrawable) img1.getDrawable()).getBitmap();

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

		byte[] byte_arr = Arrays.copyOf(stream.toByteArray(), stream.size());

		byte[] imageByte = Base64.encodeBase64(byte_arr);

		String imageAsString = new String(Hex.encodeHex(imageByte));
		return imageAsString;
	}

	public void displayAlert() {
		MainActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MainActivity.this, exceptionText, 10000).show();
			}
		});
	}

	/*
	 * private class DisplayPopupTask extends AsyncTask<String, Void, Boolean> {
	 * 
	 * protected Boolean doInBackground(String... string) { runOnUiThread(new
	 * Runnable() {
	 * 
	 * @Override public void run() {
	 * 
	 * // stuff that updates ui
	 * 
	 * try {
	 * 
	 * }
	 * 
	 * private void clearTextBoxValues() { textA1.setText("");
	 * textB1.setText(""); textC1.setText(""); textD1.setText("");
	 * textTotal.setText(""); textStartTimestamp.setText("");
	 * textEndTimestamp.setText("");
	 * 
	 * }
	 */
}
