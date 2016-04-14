package com.rahul.mymovies;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieContent extends AppCompatActivity {

    Information movieData;
    static CookieManager cookieManager;
    static String csrfTOKEN;
    TextView title, release, duration, genre, story, rating, director, writer, cast;
    ImageView image, arrowImage;
    private String link ,imdbLink;
    private MovieListAdapter adapter;
    LinearLayout movieLayout,arrowConatinerLayout;
    View rootLayout;
    List<Information> list;
    RecyclerView recyclerView;
    RatingBar ratingBar;
    private InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_content);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        movieData = new Information();
        movieData.linkData = new HashMap<>();
        movieData.credits = new HashMap<>();
        list = new ArrayList<>();
        intializeView();
        CustomFont.overrideFonts(this, rootLayout, "fonts/Montserrat-Regular.ttf");
        title.setText((String) getIntent().getExtras().get("title"));
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        link = (String) getIntent().getExtras().get("link");
        recyclerView = (RecyclerView) findViewById(R.id.recycler_download);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Have any trouble ?", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.white))
                        .setAction("OPEN", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                startActivity(intent);
                            }
                        }).show();
            }
        });
        arrowConatinerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerView.getVisibility() == View.VISIBLE) {
                    recyclerView.setVisibility(View.GONE);
                    arrowImage.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    arrowImage.setVisibility(View.GONE);
                }
            }
        });
        Picasso.with(getApplicationContext()).load((String) getIntent().getExtras().get("image")).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                rootLayout.setBackground(new BitmapDrawable(getResources(), BlurBuilder.blur(getApplicationContext(), bitmap)));
            }

            @Override
            public void onBitmapFailed(Drawable drawable) {

            }

            @Override
            public void onPrepareLoad(Drawable drawable) {

            }
        });
        ParserAsyncTask parserAsyncTask = new ParserAsyncTask();
        parserAsyncTask.execute();
        intializeAd();
    }

    private void intializeAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("384F57DE71755443E9FF6CB793E0F105")
                .build();
        adView.loadAd(adRequest);
        mInterstitialAd.loadAd(adRequest);
    }

    public void displayInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void intializeView() {
        title = (TextView) findViewById(R.id.title);
        image = (ImageView) findViewById(R.id.image);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        release = (TextView) findViewById(R.id.release);
        duration = (TextView) findViewById(R.id.duration);
        genre = (TextView) findViewById(R.id.genre);
        story = (TextView) findViewById(R.id.story);
        rating = (TextView) findViewById(R.id.rating);
        director = (TextView) findViewById(R.id.director);
        writer = (TextView) findViewById(R.id.writer);
        cast = (TextView) findViewById(R.id.cast);
        movieLayout = (LinearLayout) findViewById(R.id.movie_layout);
        rootLayout = (View) findViewById(R.id.movie_root_layout);
        arrowConatinerLayout = (LinearLayout) findViewById(R.id.arrow_conatiner_layout);
        arrowImage = (ImageView) findViewById(R.id.arrow_imageview);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.rating), PorterDuff.Mode.SRC_ATOP);
    }

    class ParserAsyncTask extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MovieContent.this);
            dialog.setTitle((String) getIntent().getExtras().get("title"));
                    dialog.setMessage("Please wait while fetching movie data");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            recyclerView.setVisibility(View.GONE);
            movieLayout.setVisibility(View.GONE);
//            Toast.makeText(MovieContent.this, link, Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try{
//                URL url = new URL("http://www.trainman.in/pnr");
//                HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                getTokens("http://www.trainman.in", con);
//                URL reqURL = new URL("http://www.trainman.in/pnr"); //the URL we will send the request to
//                HttpURLConnection request = (HttpURLConnection) (reqURL.openConnection());
//                String post = "pnr=4317362859&csrfmiddlewaretoken="+csrfTOKEN;
//                request.setRequestMethod("POST");
//                request.setRequestProperty("Host", "www.trainman.in");
//                request.addRequestProperty("Content-Length", Integer.toString(post.length())); //add the content length of the post data
//                request.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                request.setRequestProperty("Cache-Control", "no-cache");//add the content type of the request, most post data is of this type
//                request.setRequestProperty("cookie", csrfTOKEN);
//                request.setDoOutput(true);
//                request.connect();
//                OutputStreamWriter writer = new OutputStreamWriter(request.getOutputStream()); //we will write our request data here
//                writer.write(post);
//                writer.flush();
//                writer.close();
////                String csrfToken = "", unq;
////                URL url = new URL("http://www.trainman.in/pnr");
////                HttpURLConnection con = (HttpURLConnection) url.openConnection();
////                con.setRequestMethod("POST");
////                Log.d("cookie", csrfTOKEN);
////                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
////                con.setRequestProperty("Host", "www.trainman.in");
////                con.setRequestProperty("Cache-Control", "max-age=0");
////                con.setRequestProperty("Origin", "http://www.trainman.in");
////                con.setRequestProperty("Referer", "http://www.trainman.in/pnr");
////                con.setRequestProperty("Content-Length", "67");
////                con.setRequestProperty("csrfmiddlewaretoken", csrfTOKEN);
////                con.setRequestProperty("pnr", "4317362859");
////                con.connect();
//                Log.d("header POST", request.getHeaderFields().toString());
//                Log.d("check", request.getResponseCode()+" "+request.getResponseMessage());
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//                HttpURLConnection httpURLConnection;
//                url = new URL("http://www.confirmtkt.com/pnr.php?pnr=4317362859");
////                URL url = new URL("http://search.railyatri.in/mobile/trainsearch?q=124");
//                httpURLConnection = (HttpURLConnection) url.openConnection();
//                httpURLConnection.setRequestMethod("GET");
////                httpURLConnection.setRequestProperty("Content-type", "application/json");
//                httpURLConnection.setRequestProperty("Accept", "application/json");
//                StringBuilder builder = new StringBuilder();
//                builder.append(httpURLConnection.getResponseCode())
//                        .append(" ")
//                        .append(httpURLConnection.getResponseMessage())
//                        .append("\n");
//
//                BufferedReader r = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
//                StringBuilder total = new StringBuilder();
//                String line;
//                while ((line = r.readLine()) != null) {
//                    total.append(line);
//                }
//
//                Log.d("GET", httpURLConnection.getResponseCode() + "");
//                Log.d("GET", total+"");
                Document document = Jsoup.connect(link)
                        .timeout(0)
                        .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0")
                        .get();

                Element x = document.getElementsContainingOwnText("Genre").first();
                while (x.text().length() < "Genre : ".length()) {
                    x = x.parent();
                }

//                .getElementsByAttributeValueContaining("target", "_blank")
                Element element = document.getElementsByClass("entry").first();
                for (Element a : element.getElementsByTag("a")){
                    if (!a.text().contains(".00") && !a.text().trim().isEmpty()) {
                            Information linkInfo = new Information();
                            linkInfo.title = a.text();
                            linkInfo.link = a.attr("href");
                            movieData.linkData.put(a.text(), a.attr("href"));
                            list.add(linkInfo);
                    }
                }

//                Element element = document.select("div[class^=wpz-sc-box]:contains(Single)").first();
//                while (element.siblingElements().size() < 8)
//                    element = element.parent();
//                for (Element e:element.siblingElements()) {
//                    for (Element a : e.select("a")) {
//                        if (!a.text().contains(".00") && !a.text().trim().isEmpty()) {
//                            Information linkInfo = new Information();
//                            linkInfo.title = a.text();
//                            linkInfo.link = a.attr("href");
//                            movieData.linkData.put(a.text(), a.attr("href"));
//                            list.add(linkInfo);
//                        }
////                        Log.d("xx", a.text()+"\n"+a.attr("href"));
//                    }
////                    element = element.nextElementSibling();
////                    Log.d("xx", element.tagName());
//                }
                imdbLink = document.getElementsByAttributeValueContaining("href", "imdb.com").first().attr("href");

            }catch (IOException e){
                e.printStackTrace();
            }finally {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Picasso.with(getApplicationContext()).load((String)getIntent().getExtras().get("image")).into(image);
            movieLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new MovieListAdapter(getApplicationContext(), list, false);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new WrappingLinearLayoutManager(getApplicationContext()));
            if (dialog.isShowing())
                dialog.dismiss();
            mInterstitialAd.setAdListener(new AdListener() {
                public void onAdLoaded() {
                    displayInterstitial();
                }
            });
            new ParserIMDBAsyncTask().execute();
        }
    }

    private void verifyData() {
        movieData.title = (String) getIntent().getExtras().get("title");
        //movieData.title = movieData.title!=null?movieData.title:(String)getIntent().getExtras().get("title");
        movieData.duration = movieData.duration!=null?movieData.duration:"N/A";
        movieData.release = movieData.release!=null?movieData.release:"N/A";
        movieData.rating = movieData.rating!=null?movieData.rating:"N/A";
        movieData.genre = movieData.genre!=null?movieData.genre:"Genre : N/A";
        movieData.story = movieData.story!=null?movieData.story:"N/A";
        if (!movieData.credits.containsKey("Director:"))
            movieData.credits.put("Director:", "N/A");
        if (!movieData.credits.containsKey("Writers:"))
            movieData.credits.put("Writers:", "N/A");
        if (!movieData.credits.containsKey("Stars:"))
            movieData.credits.put("Stars:", "N/A");
//        if (movieData.image != null) {
//            String image = movieData.image;
//            image = image.substring(0, image.substring(0, image.lastIndexOf(".") - 1).lastIndexOf("."));
//            image = image + "._V1_SX300_SY450_.jpg";
//            movieData.image = image;
//        }
//        Log.d("image", (String)getIntent().getExtras().get("image"));
//        if (movieData.image == null || movieData.image.isEmpty())
            movieData.image = (String)getIntent().getExtras().get("image");
    }

    private void setData() {
        title.setText(movieData.title);
        duration.setText("Time : "+movieData.duration);
        release.setText("Release : "+movieData.release);
        rating.setText("Rating : " + movieData.rating);
        try {

            if (movieData.rating != "N/A") {
                ratingBar.setVisibility(View.VISIBLE);
                ratingBar.setRating((float) (Float.parseFloat((movieData.rating.split("/")[0])) / 2.0));
            }
            else ratingBar.setVisibility(View.GONE);
        }catch (Exception e){
            e.printStackTrace();
        }
        genre.setText(movieData.genre);
        story.setText(movieData.story);
        director.setText("Director : "+movieData.credits.get("Director:"));
        writer.setText("Writers : "+movieData.credits.get("Writers:"));
        cast.setText("Stars : "+movieData.credits.get("Stars:"));
        Picasso.with(getApplicationContext()).load(movieData.image).into(image);
        Picasso.with(getApplicationContext()).load(movieData.image).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                rootLayout.setBackground(new BitmapDrawable(getResources(), BlurBuilder.blur(getApplicationContext(), bitmap)));
            }

            @Override
            public void onBitmapFailed(Drawable drawable) {

            }

            @Override
            public void onPrepareLoad(Drawable drawable) {

            }
        });
    }


    public static void getTokens(String urlBase, HttpURLConnection con) throws IOException{
//        URL url = new URL(urlBase);
//        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Connection", "keep-alive");
        con.setRequestProperty("Host", "www.trainman.in");
        con.setRequestProperty("Cache-Control", "max-age=0");
        con.connect();

        List<String> cookieHeader = con.getHeaderFields().get("Set-Cookie");

        if (cookieHeader != null) {
            for (String cookie : cookieHeader) {
                String[] tokens = TextUtils.split(cookie, "=");
                if (tokens[0].equals("csrftoken")) {
                    String[] tokenValue = TextUtils.split(tokens[1],";");
                    csrfTOKEN = tokenValue[0];
                }
            }
        }
        Log.d("header GET",con.getResponseMessage()+"\n"+ con.getHeaderFields().toString());
        con.disconnect();
    }

    class ParserIMDBAsyncTask extends AsyncTask<String, Void, Boolean>{
        @Override
        protected Boolean doInBackground(String... params) {
            Document documentIMDB = null;
            try {
                documentIMDB = Jsoup.connect(imdbLink)
                        .timeout(0)
                        .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0")
                        .get();
            Elements e = documentIMDB.getElementsByClass("credit_summary_item");
            for (Element ele:e){
                movieData.credits.put(ele.getElementsByClass("inline").text(), ele.select("span[itemprop=name]").text());
            }
            movieData.imdbLink = imdbLink;
            movieData.title = documentIMDB.getElementsByAttributeValueContaining("itemprop", "name").first().text();
            movieData.release = documentIMDB.select("a[title*=release dates]").first().text();
            movieData.rating = documentIMDB.getElementsByClass("ratingValue").text();
            movieData.duration = documentIMDB.select("time[itemprop=duration]").text();
            movieData.genre = documentIMDB.select("div[class^=see-more][itemprop=genre]").first().text();
            movieData.story = documentIMDB.getElementById("titleStoryLine").select("div[itemprop=description]").first().text();
            movieData.image = documentIMDB.getElementsByClass("poster").select("img").attr("src");
            }
            catch (Exception e) {
                e.printStackTrace();
            }finally {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            verifyData();
            setData();
            if (imdbLink != null && movieData.title == null)
                new ParserIMDBAsyncTask().execute();
        }
    }
}
