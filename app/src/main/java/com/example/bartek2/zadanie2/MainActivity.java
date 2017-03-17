package com.example.bartek2.zadanie2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener{

    private ImageButton button_foto;
    private ImageButton button_download;
    private ImageButton button_delete;
    private ImageView mImageView;
    private TextView mTextView;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;
    private String encoded_low;
    private String encoded_high;
    private String imageFileName;
    private String[] linkiTab;
    private String[] noweLinkiTab;
    private int linkiId = -1;
    private int linkiIdMax;
    private String downloadUrl = "http://www.bkyziol.ayz.pl/technologie_mobilne/przeslane/";
    private GestureDetectorCompat gestureDetector;
    private Timer mtimer;
    private static final String TAG = "MyActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setEnabled(false);
        button_foto = (ImageButton) findViewById(R.id.foto);
        button_download = (ImageButton) findViewById(R.id.download);
        button_delete = (ImageButton) findViewById(R.id.delete);
        mTextView = (TextView) findViewById(R.id.textView);
        Log.i(TAG, "Start programu");
        onButtonClickListener();
        this.gestureDetector = new GestureDetectorCompat(this, this);
        mtimer = new Timer();
        mtimer.schedule(new TimerTask(){

            @Override
            public void run() {
                pobierzListe(false);
            }

        },0, 1000);
    }


    public void onButtonClickListener() {

        button_foto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        button_delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i(TAG,"Naciśnięto przycisk delete");
                skasuj();
            }
        });

    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.i(TAG,"onFling: " + Float.toString(velocityX) + " / " + Float.toString(velocityY));


        if (velocityX > 0 && linkiId > 0) {
            linkiId--;
            new pobierzZdjecie(mImageView).execute(downloadUrl + linkiTab[linkiId]+"_low.jpg");
        }

        if (velocityX < 0 && linkiId < linkiIdMax) {
            linkiId++;
            new pobierzZdjecie(mImageView).execute(downloadUrl + linkiTab[linkiId]+"_low.jpg");
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private class pobierzZdjecie extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public pobierzZdjecie(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(TAG, "Blad przy pobieraniu zdjecia:"+ e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            mTextView.setText(Integer.toString(linkiId+1)+"/"+Integer.toString(linkiIdMax+1));
            bmImage.setImageBitmap(result);
            Log.i(TAG, "Zdjecie pobrane");

        }
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Bląd przy zapisie pliku!");
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,"com.example.android.fileprovider",photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                Log.i(TAG, "Zdjęcie zapisane:" + photoFile);            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap myBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();

            myBitmap.compress(Bitmap.CompressFormat.JPEG,20,stream1);
            mImageView.setImageBitmap(myBitmap);
            byte[] array1 = stream1.toByteArray();
            encoded_low = Base64.encodeToString(array1,0);
            myBitmap.compress(Bitmap.CompressFormat.JPEG,10,stream2);
            byte[] array2 = stream2.toByteArray();
            encoded_high = Base64.encodeToString(array2,0);
            wyslij();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "BK" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void wyslij() {

        Log.i(TAG,"Rozpoczynam wysyłanie zdjęcia.");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, "http://www.bkyziol.ayz.pl/technologie_mobilne/upload.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG,"Zdjęcie wysłane");
                        Toast.makeText(MainActivity.this, "Zdjęcie wysłane", Toast.LENGTH_SHORT).show();
                        pobierzListe(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"Blad w trakcie wysylania zdjecia.");
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("zdjecie_high", encoded_high);
                map.put("zdjecie_low", encoded_low);
                map.put("nazwa",imageFileName);
                Log.i(TAG,"Zdjęcie zakodowane");
                return map;
            }
        };

        requestQueue.add(request);
    }

    private void skasuj() {

        Log.i(TAG,"Rozpoczynam kasowanie zdjęcia.");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, "http://www.bkyziol.ayz.pl/technologie_mobilne/skasuj.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG,"Zdjęcie skasowane");
                        pobierzListe(true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"Blad w trakcie kasowania zdjecia.");
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("skasuj",linkiTab[linkiId]);
                Log.i(TAG,"Zdjęcie zakodowane");
                return map;
            }
        };
        requestQueue.add(request);
        Toast.makeText(MainActivity.this, "Zdjęcie skasowane", Toast.LENGTH_SHORT).show();
    }

    private void pobierzListe(final boolean czy_odswiezyc) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, "http://www.bkyziol.ayz.pl/technologie_mobilne/pobieranie.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            try {
                                JSONArray reader = new JSONArray(response);
                                int dlugosc = reader.length();
                                noweLinkiTab = new String[dlugosc];
                                for (int i = 0; i < dlugosc; i++)
                                {
                                    JSONObject c = reader.getJSONObject(i);
                                    noweLinkiTab[i]= c.getString("nazwa");
                                }
                            } catch (final JSONException e) {
                                Log.e(TAG, "Error przy wyciąganiu z Jsona: " + e.getMessage());
                            }
                            if (!Arrays.equals(noweLinkiTab,linkiTab)){
                                Log.i(TAG, "Sa nowe zdjecia");
                                int dlugosc = noweLinkiTab.length;
                                linkiTab = new String[dlugosc];
                                linkiTab=noweLinkiTab;
//                                if (linkiTab.length < 1){
//                                    mImageView.setEnabled(false);
//                                }else{
//                                    mImageView.setEnabled(true);
//                                }


                                if (czy_odswiezyc && linkiId < linkiIdMax) {
                                    linkiIdMax = noweLinkiTab.length - 1;
                                    new pobierzZdjecie(mImageView).execute(downloadUrl+linkiTab[linkiId]+"_low.jpg");
                                }else {
                                    if (linkiId < 0 || linkiId >= linkiIdMax) {
                                        linkiIdMax = noweLinkiTab.length - 1;
                                        linkiId = linkiIdMax;
                                        new pobierzZdjecie(mImageView).execute(downloadUrl+linkiTab[linkiId]+"_low.jpg");
                                    }else{
                                        linkiIdMax = noweLinkiTab.length - 1;
                                        Toast.makeText(MainActivity.this, "Nowe zdjecie", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                mTextView.setText(Integer.toString(linkiId+1)+"/"+Integer.toString(linkiIdMax+1));
                            }else{
                                Log.i(TAG, "Nie ma nowych zdjęć");
                             }
                            Log.i(TAG, "Pobrałem listę: "+ Integer.toString(linkiIdMax+1)+" zdjec.");

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Serwer zgłasza błąd");
            }
        });
        requestQueue.add(request);
        Log.i(TAG, "Wysłano zapytanie do servera o listę");

    }

}
