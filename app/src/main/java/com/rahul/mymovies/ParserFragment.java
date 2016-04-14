package com.rahul.mymovies;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParserFragment extends Fragment{

    private org.jsoup.nodes.Document document;
    private Connection.Response response;
    private View layout;
    private ObservableRecyclerView recyclerView;
    private MovieListAdapter adapter;
    private String link, title,image;
    private SwipeRefreshLayout swipeContainer;
    List<Information> list = new ArrayList<>();
    boolean fromSearch=false, isInitialRefresh = true;
    private int previousListCount = 0, pageCount=1, numberOfColumns = 2;
    GridLayoutManager mGridLayoutManager;
    LinearLayout progressBar,swipeMessage ;
    private boolean loading = true;

    public ParserFragment(){

    }

    public ParserFragment(boolean fromSearch){
        this.fromSearch = fromSearch;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout = inflater.inflate(R.layout.activity_parser_fragment, container, false);
        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        link = getArguments().getString("pageLink");
        title = getArguments().getString("pageTitle");
        getActivity().setTitle(title);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        progressBar = (LinearLayout) view.findViewById(R.id.recycler_progress);
        swipeMessage = (LinearLayout) view.findViewById(R.id.swipe_message);
        swipeMessage.setVisibility(View.GONE);
        progressBar.setAnimation(CustomAnimation.fadeIn(getContext()));
        progressBar.setVisibility(View.VISIBLE);
        progressBar.bringToFront();
        recyclerView = (ObservableRecyclerView) view.findViewById(R.id.recyclerList);
        recyclerView.setHasFixedSize(true);
        initializeRecyclerView();
        isNetworkAvailable();
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                list.clear();
                pageCount = 1;
                previousListCount = 1;
                swipeMessage.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                recyclerView.removeAllViews();
                isInitialRefresh = true;
                isNetworkAvailable();
            }
        });
    }

    class ParserAsyncTask extends AsyncTask<String, Void, Boolean>{

        FloatingActionButton fab = ((MainActivity) getActivity()).getFloatingActionButton();
        @Override
        protected void onPreExecute() {
            if (!swipeContainer.isRefreshing()) {
                progressBar.setAnimation(CustomAnimation.fadeIn(getContext()));
                progressBar.setVisibility(View.VISIBLE);
                progressBar.bringToFront();
            }
            previousListCount = list.size();
//            layout.findViewById(R.id.recyclerList).setVisibility(View.GONE);
            if (fab != null)
                fab.setVisibility(View.GONE);
//            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try{
                Log.d("search", params[0]);
                document = Jsoup.connect(params[0])
                        .timeout(0)
                        .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0")
                        .followRedirects(true)
                        .get();
                Elements elements = document.getElementsByClass("cover");
                for (Element e : elements){
                    Information information = new Information();
                    information.title = e.getElementsByTag("a").attr("title");
                    information.image = e.getElementsByTag("img").attr("src");
                    information.link = e.getElementsByTag("a").attr("href");
                    list.add(information);
                }




//                document = Jsoup.connect(params[0]).get();
//                Elements elements = document.getElementsByClass("item");
//                for (Element e : elements){
//                    Information information = new Information();
//                    information.title = e.select("a").text();
//                    information.image = "http:"+e.select("img").attr("src");
//                    information.link = "http://www.watchfree.to"+e.select("a").attr("href");
//                    list.add(information);
//                    Log.v (information.link, e.select("a").attr("href"));
//                    Log.v ("title", e.select("a").text());
//                    Log.v ("image", e.select("img").attr("src"));
//                }


//                document = Jsoup.connect("http://www.watchfree.to"+document.getElementsByClass("item").first().select("a").attr("href"))
////                        .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0")
//                        .timeout(5000)
//                        .followRedirects(true)
//                        .get();
//
////                Log.d("script", document.getElementsByTag("script").html());
//                elements = document.select("div.list_links").select("table");
//                for (Element e : elements){
//                    Log.v ("link", e.select("a").attr("href"));
//                    Log.v("title", e.getElementsByAttributeValue("align", "left").first().text());
//                    Log.v("quality", e.getElementsByClass("quality").text());
//                }
//                Log.d("imdb", document.getElementsByClass("movie_info_header").first().select("strong").eq(0).text());
//                elements = document.getElementsByClass("movie_data");
//
//                Log.d("Story", elements.first().getElementsByClass("synopsis").first().text());
//
//                for (Element e:elements.first().select("tr")){
//                    Log.d("genre", e.select("th").text()+" : "+e.select("td").text());
//                }

            }catch (IOException e){
                e.printStackTrace();
            }finally {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            // THIS IS FOR SHOWING TWO PAGES AT START
            if (ParserFragment.this.isVisible()) {
                swipeContainer.setRefreshing(false);
//                initializeRecyclerView();
                adapter.notifyDataSetChanged();
                if (list.isEmpty()) {
                    swipeMessage.setVisibility(View.VISIBLE);
                    swipeMessage.bringToFront();
                }
                if (list.isEmpty() || list.size() == previousListCount) {
                    Toast.makeText(getContext(), "Content not found please try again", Toast.LENGTH_SHORT).show();
                    if (pageCount > 1)
                        pageCount--;
                }
                progressBar.setAnimation(CustomAnimation.fadeOut(getContext()));
                progressBar.setVisibility(View.GONE);

                if (previousListCount != 0 && pageCount > 2)
                    recyclerView.smoothScrollToPosition(previousListCount);

                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (loading && dy > 0 && !fromSearch) {
                            if (mGridLayoutManager.findLastCompletelyVisibleItemPosition() == list.size() - 1) {
                                loading = false;
                                pageCount++;
                                isNetworkAvailable();
//                                new ParserAsyncTask().execute("http://www.watchfree.to/" + "?page=" + pageCount);
                            }
                        }
                    }
                });
                loading = true;
                if (isInitialRefresh && !fromSearch) {
                    pageCount++;
                    isNetworkAvailable();
                    isInitialRefresh = false;
                }
            }
        }
    }

    private void initializeRecyclerView() {
        adapter = new MovieListAdapter(getContext(), list, true);
        recyclerView.setAdapter(adapter);
        mGridLayoutManager = new VarColumnGridLayoutManager(getContext(), 300);
//        mGridLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mGridLayoutManager);
    }

    public void isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            Log.d("network", "async  called");
            if (fromSearch) {
                new ParserAsyncTask().execute(link);
            }
            else {new ParserAsyncTask().execute(link + "page/" + pageCount);}
        }
        else showDialogBox();
    }

    public boolean showDialogBox(){
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(getContext());
        dialog.setTitle("Network Connectivity");
        dialog.setMessage("No internet connection detected please try again");
        dialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isNetworkAvailable();
            }
        });
        dialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                System.exit(0);
            }
        });
//        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setCancelable(false);
        dialog.show();
        return true;
    }

}
