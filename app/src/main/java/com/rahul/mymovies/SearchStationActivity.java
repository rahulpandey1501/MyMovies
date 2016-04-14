package com.rahul.mymovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class SearchStationActivity extends AppCompatActivity {

    HashMap<String, String> header;
    AutoCompleteTextView editText;
    TextView responseTV;
    MyTextWatcher myTextWatcher;
    MyAutoCompleteWatcher myAutoCompleteWatcher;
    String response = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editText = (AutoCompleteTextView) findViewById(R.id.edittext);
        responseTV = (TextView) findViewById(R.id.response);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, Utility2.trainNameAndNumber);
        editText.setAdapter(arrayAdapter);
        editText.setTextColor(getResources().getColor(R.color.colorAccent));
//        myTextWatcher = new MyTextWatcher();
//        myAutoCompleteWatcher = new MyAutoCompleteWatcher();
//        editText.addTextChangedListener(myTextWatcher);
//        editText.setOnItemClickListener(myAutoCompleteWatcher);
    }

    public class MyAsyncTask extends AsyncTask<String, Void, String >{

        @Override
        protected void onPreExecute() {
            header = new HashMap<>();
//            header.put("Cache-Control", "max-age=0");
//            header.put("Connection", "keep-alive");
//            header.put("Content-Type", "application/x-www-form-urlencoded");
//            header.put("Host", "www.trainman.in");
//            header.put("Origin", "http://www.trainman.in");
//            header.put("Referer", "http://www.trainman.in/");
//            header.put("Upgrade-Insecure-Requests", "1");
//            header.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            try {
                url = new URL("http://search.railyatri.in/station/search?q="+params[0]);
                Log.d("url", url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
//                if (cookiesHeader != null) {
//                    for (String cookie : cookiesHeader) {
//                        String[] tokens = TextUtils.split(cookie, "=");
//                        if (tokens[0].equals("csrftoken")) {
//                            String[] tokenValue = TextUtils.split(tokens[1],";");
//                            csrfTOKEN = tokenValue[0];
//                            header.put("csrfmiddlewaretoken", csrfTOKEN);
//                            header.put("pnr", "4317362859");
//                        }
//                    }
//                }
//
                final int responseCode=conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }
                else {
                    response="";
                }

                Log.d("response", response);
                return response;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            responseTV.setText("");
            Gson gson = new Gson();
            List<TrainFinder> trainList = Arrays.asList(gson.fromJson(result,
                    TrainFinder[].class));
            List<String> s = new ArrayList<>();
            for (TrainFinder list: trainList) {
                s.add(list.getName() + " | " + list.getCode());
            }
            Log.d("list", String.valueOf(s.size()));
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, s);
            Log.d("list", String.valueOf(arrayAdapter.getCount()));
            if (s.size() != 0) {
                editText.setAdapter(arrayAdapter);
                editText.showDropDown();
            }
            response = "";
        }
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }
    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public class MyTextWatcher implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length()>1)
                new MyAsyncTask().execute(s.toString().trim().replace(" ", "+"));
        }

        @Override
        public void afterTextChanged(Editable s) {
//            editText.removeTextChangedListener(myTextWatcher);
//            if (editText.isSelected())
//                Log.d("selected ", "select ");
        }

    }

    public class MyAutoCompleteWatcher implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(getApplicationContext(), "Selected", Toast.LENGTH_SHORT).show();
            responseTV.requestFocus();
        }
    }
}
