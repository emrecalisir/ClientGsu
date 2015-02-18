package com.clientgsu.activity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	private static final int SELECT_PICTURE = 1;
	private ImageView img1;
	private ImageView img2;
	private Bitmap bitmap;
	private EditText textIp;
	private TextView textTimestamp;
	private List<RectangleFace> rectangleFaceList = null;
	InputStream inputStream = null;
	BufferedInputStream bufferedInputStream = null;
	Long startTime = 0L;
	Long endTime = 0L;
	InputStream byteInputStream = null;
	ProgressDialog progress;

	@Override
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

	public void post(String url, List<NameValuePair> nameValuePairs) {
		HttpClient httpClient = new DefaultHttpClient();

		HttpPost httpPost = new HttpPost(textIp.getText()
				+ ":8080/Calculate_Server/rest/imageChanger/post");

		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			progress = ProgressDialog.show(this, "dialog title",
				    "dialog message", true);
			HttpResponse response = httpClient.execute(httpPost);
			List<RectangleFace> result = null;
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
	}

	private class SendImageTask extends AsyncTask<String, Void, Boolean> {

		protected Boolean doInBackground(String... string) {
			startTime = System.currentTimeMillis() / 1000;

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

				HttpResponse response = httpClient.execute(httpPost);

				BufferedReader br = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));

				String line = "";
				StringBuilder content = new StringBuilder();
				List<String> list = new ArrayList<String>();
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
					} catch (Exception ex) {
						System.out.println(ex);
					}
				}
			});
			return true;
		}

		protected void onPostExecute(Boolean doInBackground) {

			endTime = System.currentTimeMillis() / 1000;

			System.out.println("UpdateImageTask completed");
			System.out.println("All tasks are completed within "
					+ (endTime - startTime) + " seconds");
			Long diff = endTime - startTime;
			textTimestamp.setText(diff.toString());

		}

	}
}
