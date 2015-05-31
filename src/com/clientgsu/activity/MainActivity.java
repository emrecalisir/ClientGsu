package com.clientgsu.activity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Client;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.media.FaceDetector;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.textservice.TextInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.clientgsu.data.RectangleFace;
import com.clientgsu.jscience.JScienceCalculation;
import com.clientgsu.util.Util;
import com.example.clientgsu.R;
import com.geag.engine.decide.GeagDecisionEngine;
import com.geag.linpack.Linpack;
import com.geag.ocr.Tess;
import com.geag.rmi.GeagRmiInterface;

public class MainActivity extends ActionBarActivity {

	private static final int SELECT_PICTURE = 1;
	private ImageView img1;
	private EditText textIp;
	private TextView textA, textB, textC, textD, textTotal, textResult,
			textLinpack;
	private List<RectangleFace> rectangleFaceList = null;

	InputStream inputStream = null;
	BufferedInputStream bufferedInputStream = null;

	private static Long aStartTime, aEndTime, cStartTime, cEndTime, dStartTime,
			dEndTime, eStartTime, eEndTime = 0L;
	InputStream byteInputStream = null;
	ProgressDialog progress;
	final String TAG = "Hello World";
	private String serverTimeLasted = null;
	private FaceDetector myFaceDetect;
	private FaceDetector.Face[] faces;
	Long energyInitial = 0L;
	String imageRmiResponse = "";
	String exceptionText = "";
	String ocrText = "";
	double[][] a;
	String resultOfCalculation = "";
	List<NameValuePair> nameValuePairs = null;
	Bitmap bitmap;
	ByteArrayOutputStream stream;
	byte[] byte_arr;
	byte[] imageByte;
	String imageAsString;
	String res = "";
	String urlString = "http://380dental.com/wp-content/themes/campbell/images/family_img.png";
	int scale = -1;
	int level = -1;
	int voltage = -1;
	int temp = -1;
	Integer linkSpeed = -1;
	String cpuSpeed = "";
	String memory = "";
	String linkpackResult = "";
	public static String pingError = null;
	public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Main/"; 
	//public static final String DATA_PATH ="/mnt/sdcard/ClientGsu/";
	public static final String lang = "eng";
	
	// private BatteryManager mBatteryManager = null;

	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			// getBatteryInfo();

			/*
			 * BatteryManager mBatteryManager = (BatteryManager)
			 * this.getSystemService(BATTERY_SERVICE);
			 * 
			 * if(mBatteryManager==null)
			 * System.out.println("Battery Manager null"); energyInitial =
			 * mBatteryManager.getLongProperty(BatteryManager.
			 * BATTERY_PROPERTY_ENERGY_COUNTER);
			 * System.out.println("Remaining energy = " + energyInitial +
			 * "nWh");
			 */

			// System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			img1 = (ImageView) findViewById(R.id.ImageView01);

			textIp = (EditText) findViewById(R.id.editText1);

			textA = (TextView) findViewById(R.id.TextViewA1);
			textB = (TextView) findViewById(R.id.TextViewB1);
			textC = (TextView) findViewById(R.id.TextViewC1);
			textD = (TextView) findViewById(R.id.TextViewD1);
			textTotal = (TextView) findViewById(R.id.TextViewTotal);
			textResult = (TextView) findViewById(R.id.TextViewResult);
			textLinpack = (TextView) findViewById(R.id.TextViewLinpack);

			final Button buttonSend = (Button) findViewById(R.id.buttonSend);

			buttonSend.findViewById(R.id.buttonSend).setOnClickListener(
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

			findViewById(R.id.buttonBrowse).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							Intent intent = new Intent();
							intent.setType("image/*");
							intent.setAction(Intent.ACTION_GET_CONTENT);
							startActivityForResult(Intent.createChooser(intent,
									"Select Picture"), SELECT_PICTURE);
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
			findViewById(R.id.buttonJSciLocal).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							new Thread() {
								public void run() {
									// TODO Run network requests here.
									new JScienceTaskLocal().execute("");
								}
							}.start();

						}
					});

			findViewById(R.id.buttonJSciServer).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							new Thread() {
								public void run() {
									// TODO Run network requests here.
									new JScienceTaskServer().execute("");
								}
							}.start();

						}
					});

			findViewById(R.id.buttonSendSocket).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							new Thread() {
								public void run() {
									// TODO Run network requests here.
									new JScienceTaskSocket().execute("");
								}
							}.start();

						}
					});

			findViewById(R.id.buttonFaceWithDecide).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							new Thread() {
								public void run() {
									// TODO Run network requests here.
									new FaceDecideTask().execute("");
								}
							}.start();

						}
					});

			findViewById(R.id.buttonJSciWithDecide).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							new Thread() {
								public void run() {
									// TODO Run network requests here.
									new JSciDecideTask().execute("");
								}
							}.start();

						}
					});

			findViewById(R.id.buttonLinpack).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							new Thread() {
								public void run() {
									// TODO Run network requests here.
									new LinpackTask().execute("");
								}
							}.start();

						}
					});
			
			findViewById(R.id.buttonOcrLocal).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							new Thread() {
								public void run() {
									// TODO Run network requests here.
									new LocalOcrTask().execute("");
								}
							}.start();

						}
					});

			findViewById(R.id.buttonOcrServer).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							new Thread() {
								public void run() {
									// TODO Run network requests here.
									new ServerOcrTask().execute("");
								}
							}.start();

						}
					});

			initializeMatrices();
			
			File file1 = new File(DATA_PATH + "tessdata/" + lang + ".traineddata");
			if (!file1.exists()) {
				try {

					AssetManager assetManager = getAssets();
					//InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
					
					InputStream in = getResources().getAssets().open("tessdata/" + lang + ".traineddata");
					//GZIPInputStream gin = new GZIPInputStream(in);
					
				    File file = new File(DATA_PATH
							+ "tessdata/" + lang + ".traineddata");

					OutputStream out = new FileOutputStream(DATA_PATH
							+ "tessdata/" + lang + ".traineddata");

					// Transfer bytes from in to out
					byte[] buf = new byte[1024];
					int len;
					//while ((lenf = gin.read(buff)) > 0) {
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					in.close();
					//gin.close();
					out.close();
					
					Log.v(TAG, "Copied " + lang + " traineddata");
				} catch (IOException e) {
					Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
				}
			}
		} catch (Exception e) {
			exceptionText = e.toString();
			displayAlert();

		}
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

			aStartTime = System.currentTimeMillis();
			String imageAsString = "", line = "";
			try {
				imageAsString = prepareRawDataOfImage();

				nameValuePairs = new ArrayList<NameValuePair>(1);

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

	private class LocalProcessingTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {

			try {
				// clearTextBoxValues();
				/*
				 * System.out.println("Remaining energy = " +
				 * BatteryManager.BATTERY_PROPERTY_CURRENT_NOW + "nWh");
				 */

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
				dEndTime = System.currentTimeMillis();

			} catch (Exception e) {
				exceptionText = e.toString();
				displayAlert();
			}
			return true;

		}

		protected void onPostExecute(Boolean doInBackground) {

			System.out.println("SendImageTask completed");

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					textD.setText("D: " + (dEndTime - dStartTime) + " ms");

				}
			});

		}
	}

	private class RmiTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {

			try {
				aStartTime = System.currentTimeMillis();

				String serverIp = textIp.getText().toString();
				CallHandler callHandler = new CallHandler();
				Client client = new Client(serverIp, 55661, callHandler);
				GeagRmiInterface geagRmiService = (GeagRmiInterface) client
						.getGlobal(GeagRmiInterface.class);

				String imageAsString = prepareRawDataOfImage();

				aEndTime = System.currentTimeMillis();

				res = geagRmiService.getResponseOfFaceDetection(imageAsString);

				cStartTime = System.currentTimeMillis();
				rectangleFaceList = new ArrayList<RectangleFace>();

				String[] split = res.split(";");
				RectangleFace rectFace = null;

				String[] split2 = null;

				for (int i = 0; i < split.length - 1; i++) {

					split2 = split[i].split(",");
					rectFace = new RectangleFace(Integer.parseInt(split2[0]),
							Integer.parseInt(split2[1]),
							Integer.parseInt(split2[2]),
							Integer.parseInt(split2[3]));
					rectangleFaceList.add(rectFace);
				}
				serverTimeLasted = split[split.length - 1];
				client.close();
				cEndTime = System.currentTimeMillis();

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

	private class JScienceTaskLocal extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {
			try {
				clearTextBoxValues();
				dStartTime = System.currentTimeMillis();

				JScienceCalculation jScienceCalculation = new JScienceCalculation();
				resultOfCalculation = jScienceCalculation.multiplyMatrices(a);

				dEndTime = System.currentTimeMillis();
			} catch (Exception e) {
				exceptionText = e.toString();
				displayAlert();

			}
			return true;
		}

		protected void onPostExecute(Boolean doInBackground) {
			System.out.println("JScienceTaskLocal completed");
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					textD.setText("D: " + (dEndTime - dStartTime) + " ms");

					if (resultOfCalculation != "") {
						textResult.setText("Sol: " + resultOfCalculation);
						resultOfCalculation = "";
					}

				}
			});

		}
	}

	private class JScienceTaskServer extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {
			try {

				aStartTime = System.currentTimeMillis();
				String serverIp = textIp.getText().toString();
				CallHandler callHandler = new CallHandler();
				Client client = new Client(serverIp, 55661, callHandler);
				GeagRmiInterface geagRmiService = (GeagRmiInterface) client
						.getGlobal(GeagRmiInterface.class);
				aEndTime = System.currentTimeMillis();
				String res = geagRmiService
						.getResponseOfMatriceMultiplicationWithJScience(a);
				cStartTime = System.currentTimeMillis();
				String[] split = res.split(";");

				if (split.length != 2)
					return null;

				resultOfCalculation = split[0];
				serverTimeLasted = split[1];
				client.close();
				cEndTime = System.currentTimeMillis();

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

			System.out.println("JScienceServer completed");

			new Thread() {
				public void run() {
					// TODO Run network requests here.

					new UpdateFormTask().execute("");
				}
			}.start();

		}

	}

	private class JScienceTaskSocket extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {
			try {
				aStartTime = System.currentTimeMillis();

				String serverIp = textIp.getText().toString();

				System.out.println("Connecting to " + serverIp + " on port "
						+ 15001);
				Socket client = new Socket(serverIp, 15002);
				System.out.println("Just connected to "
						+ client.getRemoteSocketAddress());
				OutputStream outToServer = client.getOutputStream();
				DataOutputStream out = new DataOutputStream(outToServer);

				// out.writeUTF("Hello from " + client.getLocalSocketAddress());
				ObjectOutputStream objectOutput = new ObjectOutputStream(
						outToServer);
				objectOutput.writeObject(a);
				aEndTime = System.currentTimeMillis();

				InputStream inFromServer = client.getInputStream();
				DataInputStream in = new DataInputStream(inFromServer);
				String res1 = in.readUTF();
				cStartTime = System.currentTimeMillis();
				System.out.println("res1" + res1);
				String[] split = res1.split(";");

				if (split.length != 2)
					return null;

				resultOfCalculation = split[0];
				serverTimeLasted = split[1];

				client.close();
				cEndTime = System.currentTimeMillis();

			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}

		protected void onPostExecute(Boolean doInBackground) {

			System.out.println("JScienceSocket completed");

			new Thread() {
				public void run() {
					// TODO Run network requests here.

					new UpdateFormTask().execute("");
				}
			}.start();

		}

	}

	private class FaceDecideTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {

			GeagDecisionEngine geagDecisionEngine = new GeagDecisionEngine();

			double w = 0, di = 0, bw = 0, ss = 0, sm = 0;

			// bw = calculateBandwidth();
			getBatteryInfo();
			getCpuInfo();

			try {
				
				ping(textIp.getText().toString());
				
				//ping("google.com");
				//pingHost("192.168.1.3");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// executeCmd(execString, false);

			// execCmd();

			bw = linkSpeed;
			sm = Integer.parseInt(cpuSpeed);

			if (geagDecisionEngine.offloadingImprovesPerformance(w, di, bw, ss,
					sm)) {
				new Thread() {
					public void run() {
						// TODO Run network requests here.
						new SendImageTask().execute("");
					}
				}.start();

			}

			else {
				new Thread() {
					public void run() {
						// TODO Run network requests here.
						new LocalProcessingTask().execute("");
					}
				}.start();
			}

			return true;

		}

		protected void onPostExecute(Boolean doInBackground) {

			System.out.println("FaceDecideTask completed");

		}

	}

	private class JSciDecideTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {

			return true;

		}

		protected void onPostExecute(Boolean doInBackground) {

			System.out.println("JSciDecideTask completed");

		}

	}

	private class LinpackTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {

			for (int i = 0; i < 30; i++) {
				Linpack linpack = new Linpack();
				linpack.run_benchmark();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return true;

		}

		protected void onPostExecute(Boolean doInBackground) {

			System.out.println("LinpackTask completed");
			MainActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (linkpackResult != null)
						textLinpack.setText(linkpackResult);
				}
			});

		}

	}

	private class LocalOcrTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {

			try {
				// clearTextBoxValues();
				/*
				 * System.out.println("Remaining energy = " +
				 * BatteryManager.BATTERY_PROPERTY_CURRENT_NOW + "nWh");
				 */

				dStartTime = System.currentTimeMillis();

				img1 = (ImageView) findViewById(R.id.ImageView01);

				Bitmap bitmap = ((BitmapDrawable) img1.getDrawable())
						.getBitmap();

				
				Tess tess = new Tess();
				//ocrText = tess.performOcrOperation("http://i.stack.imgur.com/1hVxt.png");
				ocrText = tess.tess2Operation(DATA_PATH, lang, bitmap);
				dEndTime = System.currentTimeMillis();

			} catch (Exception e) {
				exceptionText = e.toString();
				displayAlert();
			}
			return true;

		}

		protected void onPostExecute(Boolean doInBackground) {

			System.out.println("LocalOcrTask completed");

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					textD.setText("D: " + (dEndTime - dStartTime) + " ms");
					displayOcrText();
				}
			});

		}
	}
	
	private class ServerOcrTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {

			try {

				aStartTime = System.currentTimeMillis();

				String serverIp = textIp.getText().toString();
				CallHandler callHandler = new CallHandler();
				Client client = new Client(serverIp, 55661, callHandler);
				GeagRmiInterface geagRmiService = (GeagRmiInterface) client
						.getGlobal(GeagRmiInterface.class);

				String imageAsString = prepareRawDataOfImage();

				aEndTime = System.currentTimeMillis();
			
				res = geagRmiService.getResponseOfOcr(imageAsString);
				
				cStartTime = System.currentTimeMillis();
				
				if(res==null)
					return false;
				
				String[] split = res.split(";");

				ocrText = split[0];
				serverTimeLasted = split[split.length - 1];

				client.close();
											
				cEndTime = System.currentTimeMillis();

			} catch (Exception e) {
				exceptionText = e.toString();
				displayAlert();
			}
			return true;

		}

		protected void onPostExecute(Boolean doInBackground) {

			System.out.println("ServerOcrTask completed");

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					new UpdateImageTask().execute("");
					displayOcrText();
				}
			});

		}
	}

	
	private String prepareRawDataOfImage() {

		img1 = (ImageView) findViewById(R.id.ImageView01);

		bitmap = ((BitmapDrawable) img1.getDrawable()).getBitmap();

		stream = new ByteArrayOutputStream();

		bitmap.compress(Bitmap.CompressFormat.JPEG, 10, stream);

		byte_arr = Arrays.copyOf(stream.toByteArray(), stream.size());

		imageByte = Base64.encodeBase64(byte_arr);

		imageAsString = new String(Hex.encodeHex(imageByte));

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
	
	public void displayOcrText() {
		MainActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MainActivity.this, ocrText, 10000).show();
			}
		});
	}

	public static int[][] multiply(int[][] a, int[][] b) {
		int rowsInA = a.length;
		int columnsInA = a[0].length; // same as rows in B
		int columnsInB = b[0].length;
		int[][] c = new int[rowsInA][columnsInB];
		for (int i = 0; i < rowsInA; i++) {
			for (int j = 0; j < columnsInB; j++) {
				for (int k = 0; k < columnsInA; k++) {
					c[i][j] = c[i][j] + a[i][k] * b[k][j];
				}
			}
		}
		return c;
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
	 */
	private void clearTextBoxValues() {
		MainActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textA.setText("A:");
				textB.setText("B: ");
				textC.setText("C: ");
				textD.setText("D: ");
				textTotal.setText("TOTAL: ");
			}
		});

	}

	private class UpdateFormTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					try {
						if (aEndTime != null && aStartTime != null)
							textA.setText("A: " + (aEndTime - aStartTime)
									+ " ms");

						if (serverTimeLasted != null)
							textB.setText("B: " + serverTimeLasted + " ms");

						if (cEndTime != null && cStartTime != null)
							textC.setText("C: " + (cEndTime - cStartTime)
									+ " ms");

						if (cEndTime != null && aStartTime != null)
							textTotal.setText("TOTAL: "
									+ (cEndTime - aStartTime) + " ms");

						if (resultOfCalculation != "") {
							textResult.setText("Sol: " + resultOfCalculation);
							resultOfCalculation = "";
						}

					} catch (Exception e) {
						exceptionText = e.toString();
						displayAlert();
					}
				}
			});
			return true;
		}

		protected void onPostExecute(Boolean doInBackground) {
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

			cEndTime = System.currentTimeMillis();
			textA.setText("A: " + (aEndTime - aStartTime) + " ms");
			textB.setText("B: " + serverTimeLasted + " ms");
			textC.setText("C: " + (cEndTime - cStartTime) + " ms");
			textTotal.setText("TOTAL: " + (cEndTime - aStartTime) + " ms");

		}

	}

	private void initializeMatrices() {
		a = new double[70][70];
		Util util = new Util();

		for (int i = 0; i < 70; i++) {
			for (int j = 0; j < 70; j++) {

				a[i][j] = util.randInt(2, 5);
			}
		}
	}

	public void getCpuInfo() {
		try {
			Process proc = Runtime.getRuntime().exec("cat /proc/cpuinfo");
			InputStream is = proc.getInputStream();
			cpuSpeed = getStringFromInputStream(is);
		} catch (IOException e) {
			Log.e(TAG, "------ getCpuInfo " + e.getMessage());
		}
	}

	public void getMemoryInfo() {
		try {
			Process proc = Runtime.getRuntime().exec("cat /proc/meminfo");
			InputStream is = proc.getInputStream();
			memory = getStringFromInputStream(is);
		} catch (IOException e) {
			Log.e(TAG, "------ getMemoryInfo " + e.getMessage());
		}
	}

	private static String getStringFromInputStream(InputStream is) {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;

		try {
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (IOException e) {
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}

		return sb.toString();
	}

	private void getBatteryInfo() {
		BroadcastReceiver batteryReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
				voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
				WifiManager wifiManager = (WifiManager) context
						.getSystemService(WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				if (wifiInfo != null) {
					linkSpeed = wifiInfo.getLinkSpeed(); // measured using
															// WifiInfo.LINK_SPEED_UNITS
				}
			}
		};
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryReceiver, filter);
	}

	public static int pingHost(String host) throws IOException,
			InterruptedException {
		Runtime runtime = Runtime.getRuntime();
		Process proc = runtime.exec("ping -c 1 " + host);
		proc.waitFor();
		int exit = proc.exitValue();
		return exit;
	}

	public String ping(String host) throws IOException, InterruptedException {
		StringBuffer echo = new StringBuffer();
		Runtime runtime = Runtime.getRuntime();
		long pingStartTime = System.currentTimeMillis();
		Process proc = runtime.exec("ping -c 10 -s 6000 google.com");
		proc.waitFor();
		long pingEndTime = System.currentTimeMillis();
		long diff = pingEndTime - pingStartTime;
		System.out.println(diff);
		int exit = proc.exitValue();
		if (exit == 0) {
			InputStreamReader reader = new InputStreamReader(
					proc.getInputStream());
			BufferedReader buffer = new BufferedReader(reader);
			String line = "";
			while ((line = buffer.readLine()) != null) {
				echo.append(line + "\n");
			}
			return getPingStats(echo.toString());
		} else if (exit == 1) {
			pingError = "failed, exit = 1";
			return null;
		} else {
			pingError = "error, exit = 2";
			return null;
		}
	}

	public static String getPingStats(String s) {
		if (s.contains("0% packet loss")) {
			int start = s.indexOf("/mdev = ");
			int end = s.indexOf(" ms\n", start);
			s = s.substring(start + 8, end);
			String stats[] = s.split("/");
			return stats[2];
		} else if (s.contains("100% packet loss")) {
			pingError = "100% packet loss";
			return null;
		} else if (s.contains("% packet loss")) {
			pingError = "partial packet loss";
			return null;
		} else if (s.contains("unknown host")) {
			pingError = "unknown host";
			return null;
		} else {
			pingError = "unknown error in getPingStats";
			return null;
		}
	}

	public static String executeCmd(String cmd, boolean sudo) {
		try {

			Process p;
			if (!sudo)
				p = Runtime.getRuntime().exec(cmd);
			else {
				p = Runtime.getRuntime().exec(new String[] { "su", "-c", cmd });
			}
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String s;
			String res = "";
			while ((s = stdInput.readLine()) != null) {
				res += s + "\n";
			}
			p.destroy();
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";

	}

	public void execCmd() {

		InetAddress in;
		in = null;
		InetAddress[] allByName = null;
		// Definimos la ip de la cual haremos el ping
		try {
			allByName = InetAddress.getAllByName(textIp.getText().toString());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Definimos un tiempo en el cual ha de responder
		try {
			if (in.isReachable(50000)) {
				System.out.println("Responde OK");
			} else {
				System.out.println("No responde: Time out");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
		}
	}
}
