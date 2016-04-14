package com.rahul.mymovies;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class PNRActivity extends AppCompatActivity {

    EditText editText;
    TextView textView, trainET, fromET, toET, boardET, travelClassET, chartET, passET, currStatusET, bookStatusET;
    Button button;
    String train, from, to, board, travelClass, chart, pass, currStatus, bookStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pnr);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editText = (EditText) findViewById(R.id.pnrET);
        textView = (TextView) findViewById(R.id.response);
        trainET = (TextView) findViewById(R.id.train);
        fromET = (TextView) findViewById(R.id.from);
        toET = (TextView) findViewById(R.id.to);
        boardET = (TextView) findViewById(R.id.board);
        travelClassET = (TextView) findViewById(R.id.travel_class);
        chartET = (TextView) findViewById(R.id.chart);
        passET = (TextView) findViewById(R.id.passenger);
        currStatusET = (TextView) findViewById(R.id.curr_status);
        bookStatusET = (TextView) findViewById(R.id.book_status);
        button = (Button) findViewById(R.id.submitButton);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MyAsyncTask().execute(editText.getText().toString());
            }
        });
    }

    public class MyAsyncTask extends AsyncTask<String, Void, String > {
        String response = "";
        @Override
        protected String doInBackground(String... params) {
//            byIndianRailGov(params);
            byRailYatri(params);
            return null;
        }

        private void byRailYatri(String... params) {
            try {

                Document document1 = Jsoup.connect("http://convert2mp3.net/en/index.php?p=search")
                        .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0")
                        .data("q", "passenger let her go")
                        .timeout(0)
                        .post();

                for (Element link : document1.select("div.searchtitle")){
                    Log.d("link", link.getElementsByAttribute("href").text()+"   "+link.text());
                }

                generateNoteOnSD(getApplicationContext(), "response.html", document1.html());

                Document document = Jsoup.connect("http://www.railyatri.in/pnr-status/" + params[0]).timeout(0).get();
                Elements elements = document.getElementById("result").getElementsByClass("train-schedule").first().getElementsByClass("schedule");
                if (elements.size() != 0) {
                    train = elements.get(0).select("a").text();
                    Elements e = elements.get(1).getElementsByClass("border_div");
                    from = e.get(0).child(1).text();
                    to = e.get(1).child(1).text();
                    board = e.get(2).child(1).text();
                    travelClass = e.get(3).child(1).text();
                    chart = e.get(4).child(1).text();
                    e = elements.get(2).child(1).children();
                    pass = e.get(0).text();
                    currStatus = e.get(1).text();
                    bookStatus = e.get(2).text();
                }else {
                    response = document.getElementById("result").getElementsByClass("train-schedule").first().text();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private void byIndianRailGov(String... params){
            try {
                Document document = Jsoup.connect("http://www.indianrail.gov.in/cgi_bin/inet_pnstat_cgi_2484.cgi")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.8,hi;q=0.6")
//                        .header("Accept-Encoding", "gzip, deflate")
                        .header("Referer", "http://www.indianrail.gov.in/between_Imp_Stations.html")
                        .header("Host", "www.indianrail.gov.in")
                        .header("Origin", "http://www.indianrail.gov.in")
                        .header("Connection", "keep-alive")
                        .header("Upgrade-Insecure-Requests", "1")
                        .data("lccp_cap_value", "11111")
                        .data("lccp_capinp_value", "11111")
                        .data("lccp_pnrno1", params[0])
                        .data("submit", "Get+Status")
//                        .data("lccp_stnname", "b")
//                        .data("lccp_SearchType", "START_STR")
//                        .data("lccp_choice", "STN_NAME")
//                        .data("submit", "Please+Wait...")
                        .timeout(0)
                        .post();
                generateNoteOnSD(getApplicationContext(), "response.html", document.html());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            textView.setText(response);
            trainET.setText(train);
            fromET.setText(from);
            toET.setText(to);
            boardET.setText(board);
            travelClassET.setText(travelClass);
            chartET.setText(chart);
            passET.setText(pass);
            currStatusET.setText(currStatus);
            bookStatusET.setText(bookStatus);
            super.onPostExecute(s);
        }
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
}
