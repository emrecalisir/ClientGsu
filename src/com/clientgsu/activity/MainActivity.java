package com.clientgsu.activity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.DecoderException;
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
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import com.clientgsu.data.RectangleFace;
import com.example.clientgsu.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	private static final int SELECT_PICTURE = 1;
	private ImageView img1;
	private Bitmap bitmap;
	private EditText textIp;
	private TextView textTimestamp;
	private List<RectangleFace> rectangleFaceList = null;
	private Long timeDiff = 0L;
	InputStream inputStream = null;
	BufferedInputStream bufferedInputStream = null;
	Long startSendImageTaskTime = 0L;
	Long startDrawRectsTaskTime = 0L;
	Long firstStartTime = 0L;

	Long endTime = 0L;
	InputStream byteInputStream = null;
	ProgressDialog progress;
	private CascadeClassifier faceDetector;

	final String TAG = "Hello World";

	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				// Create and set View
				setContentView(R.layout.activity_main);
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		img1 = (ImageView) findViewById(R.id.ImageView01);

		textIp = (EditText) findViewById(R.id.editText1);
		textTimestamp = (TextView) findViewById(R.id.textView1);
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

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				if (bitmap != null) {
					bitmap.recycle();
				}
				try {
					// stream =
					// getContentResolver().openInputStream(data.getData());
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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

			startSendImageTaskTime = System.currentTimeMillis() / 1000;

			img1 = (ImageView) findViewById(R.id.ImageView01);

			Bitmap bitmap = ((BitmapDrawable) img1.getDrawable()).getBitmap();

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			// bitmap.recycle();

			System.out.println("stream size=" + stream.size());

			byte[] byte_arr = Arrays
					.copyOf(stream.toByteArray(), stream.size());

			byte[] imageByte = Base64.encodeBase64(byte_arr);

			try {
				String imageAsString = new String(Hex.encodeHex(imageByte));

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

				endTime = System.currentTimeMillis() / 1000;
				timeDiff = endTime - startSendImageTaskTime;
				System.out
						.println("timeDiff: client image to binary code lasted: "
								+ timeDiff
								+ " seconds");

				HttpResponse response = httpClient.execute(httpPost);

				startDrawRectsTaskTime = System.currentTimeMillis() / 1000;
				timeDiff = startDrawRectsTaskTime - endTime;
				System.out.println("timeDiff: server to client response time lasted: "
						+ (timeDiff) + " seconds");

				startDrawRectsTaskTime = System.currentTimeMillis() / 1000;

				BufferedReader br = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));

				String line = "";
				rectangleFaceList = new ArrayList<RectangleFace>();

				RectangleFace rectangleFace = null;

				while (null != (line = br.readLine())) {
					JSONArray array = new JSONArray(line);
					for (int i = 0; i < array.length(); i++) {
						JSONObject jsonObject = array.getJSONObject(i);
						rectangleFace = new RectangleFace(
								jsonObject.getInt("x1"),
								jsonObject.getInt("x2"),
								jsonObject.getInt("y1"),
								jsonObject.getInt("y2"));
						rectangleFaceList.add(rectangleFace);

					}
				}

				/*
				 * if (response.getStatusLine().getStatusCode() != 201) { throw
				 * new RuntimeException("Failed : HTTP error code : " +
				 * response.getStatusLine().getStatusCode()); }
				 */

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			catch (Exception e) {
				e.printStackTrace();
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

	private class UpdateImageTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					// stuff that updates ui

					try {

						Bitmap createBitmap = Bitmap.createBitmap(
								img1.getWidth(), img1.getHeight(),
								Config.ARGB_8888);
						Canvas canvas = new Canvas(createBitmap);
						img1.draw(canvas);

						Paint p = new Paint();
						p.setColor(Color.GREEN);
						p.setStyle(Paint.Style.STROKE);
						for (RectangleFace rectangleFace : rectangleFaceList) {
							canvas.drawRect(rectangleFace.getX1(),
									rectangleFace.getY1(),
									rectangleFace.getX2(),
									rectangleFace.getY2(), p);
						}

						img1.setImageBitmap(createBitmap);

						endTime = System.currentTimeMillis() / 1000;

						timeDiff = endTime - startDrawRectsTaskTime;

						System.out.println("timeDiff: client image drawing time lasted: "
								+ (timeDiff)
								+ " seconds");

						
					} catch (Exception ex) {
						System.out.println(ex);
					}
				}
			});
			return true;
		}

		protected void onPostExecute(Boolean doInBackground) {

			endTime = System.currentTimeMillis() / 1000;

			timeDiff = endTime - startSendImageTaskTime;
			System.out.println("TimeDiff: total time for client to server face detection lasted: "
					+ (timeDiff)
					+ " seconds");
			

		}

	}

	private class LocalProcessingTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {

			File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);

			File mCascadeFile = new File(cascadeDir,
					"haarcascade_frontalface_alt.xml");

			faceDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());

			img1 = (ImageView) findViewById(R.id.ImageView01);

			Bitmap bitmap = ((BitmapDrawable) img1.getDrawable()).getBitmap();

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			// bitmap.recycle();

			System.out.println("stream size=" + stream.size());

			byte[] byte_arr = Arrays
					.copyOf(stream.toByteArray(), stream.size());

			Mat mat = new Mat(img1.getHeight(), img1.getWidth(), CvType.CV_8UC3);
			mat.put(0, 0, byte_arr);

			MatOfRect faceDetections = new MatOfRect();
			System.out.println(String.format("Detected %s faces",
					faceDetections.toArray().length));
			faceDetector.detectMultiScale(mat, faceDetections);
			RectangleFace rectangleFace = null;
			for (Rect rect : faceDetections.toArray()) {
				rectangleFace = new RectangleFace(rect.x, rect.x + rect.width,
						rect.y, rect.y + rect.height);
				rectangleFaceList.add(rectangleFace);

			}
			return true;

		}

		protected void onPostExecute(Boolean doInBackground) {

			endTime = System.currentTimeMillis() / 1000;

			System.out.println("LocalProcessingTask completed");
			System.out.println("All tasks are completed within "
					+ (endTime - startSendImageTaskTime) + " seconds");
			Long diff = endTime - startSendImageTaskTime;
			textTimestamp.setText(String.valueOf(diff));

		}
	}
}
