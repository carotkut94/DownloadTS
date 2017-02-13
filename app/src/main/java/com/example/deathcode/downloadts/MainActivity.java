package com.example.deathcode.downloadts;

import android.Manifest;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.deathcode.downloadts.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import hybridmediaplayer.HybridMediaPLayer;

public class MainActivity extends AppCompatActivity {

    CardView cardView;
    ImageButton playPause, download;
    SeekBar seekBar;
    TextView textViewTitle;
    ProgressBar doingThings;
    Long downloadReference;
    List<String> res;
    List<String> links;
    DownloadManager downloadManager;
    List<String> extensions;
    android.widget.SearchView searchView;
    private boolean isPrepared;
    private int time;
    static int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final HybridMediaPLayer mediaPlayer = HybridMediaPLayer.getInstance(MainActivity.this);
        res = new ArrayList<>();
        links = new ArrayList<>();
        extensions = new ArrayList<>();
        haveStoragePermission();
        cardView = (CardView) findViewById(R.id.viewContainer);
        playPause = (ImageButton) findViewById(R.id.pausePlay);
        download = (ImageButton) findViewById(R.id.downloadStartStop);
        textViewTitle = (TextView) findViewById(R.id.title);
        doingThings = (ProgressBar) findViewById(R.id.doingThings);
        searchView = (android.widget.SearchView) findViewById(R.id.textURL);
        //
        showDialog();
        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                links.clear();
                res.clear();
                extensions.clear();
                makeJsonObjectRequest(Utility.getUrl(s));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });


        download.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                count++;
                if (count % 2 == 0) {
                    if (haveStoragePermission()) {
                        download.setImageResource(R.drawable.ic_stop);
                        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        Uri Download_Uri = Uri.parse(links.get(0));
                        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                        //Set whether this download may proceed over a roaming connection.
                        request.setAllowedOverRoaming(true);
                        //Set the title of this download, to be displayed in notifications.
                        request.setTitle(textViewTitle.getText().toString());
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        //Set the local destination for the downloaded file to a path within the application's external files directory
                        request.setDestinationInExternalFilesDir(MainActivity.this, Environment.getExternalStorageDirectory() + "/Download", textViewTitle.getText().toString() +"."+ extensions.get(0));
                        //Enqueue a new download and same the referenceId
                        downloadReference = downloadManager.enqueue(request);
                    }
                }else
                {
                    download.setImageResource(R.drawable.ic_download);
                    downloadManager.remove(downloadReference);
                }
            }

        });


        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.setDataSource(links.get(0));
                mediaPlayer.setOnCompletionListener(new HybridMediaPLayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(HybridMediaPLayer hybridMediaPLayer) {
                        hybridMediaPLayer.release();
                    }
                });
                mediaPlayer.setOnErrorListener(new HybridMediaPLayer.OnErrorListener() {
                    @Override
                    public void onError(HybridMediaPLayer hybridMediaPLayer) {
                        Toast.makeText(MainActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
                    }
                });
                mediaPlayer.setOnPreparedListener(new HybridMediaPLayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(HybridMediaPLayer hybridMediaPLayer) {
                    }
                });
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playPause.setImageResource(R.drawable.ic_play);
                } else {
                    mediaPlayer.prepare();
                    mediaPlayer.play();
                    playPause.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean haveStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error", "You have permission");
                return true;
            } else {
                Log.e("Permission error", "You have asked for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //you dont need to worry about these stuff below api level 23
            Log.e("Permission error", "You already have the permission");
            return true;
        }
    }

    private void makeJsonObjectRequest(String urlJsonObj) {
        doingThings.setVisibility(View.VISIBLE);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlJsonObj, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                if (response.has("error")) {
                    try {
                        Toast.makeText(MainActivity.this, response.getString("error"), Toast.LENGTH_LONG).show();
                        doingThings.setVisibility(View.INVISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else

                {
                    cardView.setVisibility(View.VISIBLE);
                    //Log.e("TAG", response.toString());
                    try {
                        JSONObject info = response.getJSONObject("info");
                        String titles = info.getString("title");
                        textViewTitle.setText(titles);
                        JSONArray formats = info.getJSONArray("formats");
                        for (int i = 0; i < formats.length(); i++) {
                            JSONObject tempObject = formats.getJSONObject(i);
                            String extension = tempObject.getString("ext");
                            if (extension.equals("m4a")) {
                                Log.e("ARRAY", tempObject.toString());
                                res.add(tempObject.getString("format").substring(tempObject.getString("format").indexOf("-") + 1, tempObject.getString("format").length()));
                                links.add(tempObject.getString("url"));
                                extensions.add(extension);
                            }
                        }
                        Log.e("URL For Audio", links.get(0));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                    doingThings.setVisibility(View.INVISIBLE);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("TAG", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                doingThings.setVisibility(View.INVISIBLE);
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    public void showDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_MinWidth);
        alertDialog.setMessage("This app is only for educational purpose, and this application might be illegal in you area of residence. As a law abiding citizen you must obey all the laws. If you press accept then you hereby accept the warning stated above, and you are aware of the risks involved and the developer of the app will no be held for any outcome of using this app like 'Nuclear War' etc.");
        alertDialog.setTitle("Warning!");
        alertDialog.setNegativeButton("Shut up!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                finishActivity(0);
            }
        });
        alertDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences preferences = getSharedPreferences("APPCOUNTER", MODE_APPEND);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("ISFIRSTRUN", false);
                editor.apply();
                editor.commit();
            }
        });
        alertDialog.setCancelable(false);

        SharedPreferences preferences = getSharedPreferences("APPCOUNTER", MODE_APPEND);
        if (preferences.getBoolean("ISFIRSTRUN", true)) {
            alertDialog.show();
        }
    }
}
