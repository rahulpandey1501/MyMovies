package com.rahul.mymovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by rahul on 24/2/16.
 */
public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.CustomViewHolder> {
    private LayoutInflater inflater;
    private List<Information> list;
    private Context context;
    private boolean flag;
    static int pos = 0;

    public MovieListAdapter(Context context, List<Information> list, boolean flag) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.list = list;
        this.flag = flag;
//        Log.d("size", list.size()+"  "+flag);
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recycler_list, parent, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        pos = position;
        final Information information = list.get(position);
        if(flag) {
            holder.itemView.findViewById(R.id.recycler_gridlist).setVisibility(View.VISIBLE);
            holder.itemView.findViewById(R.id.recycler_dlist).setVisibility(View.GONE);
            holder.title.setText(information.title);
            Picasso.with(context).load(information.image).placeholder(R.drawable.placeholder).into(holder.image);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MovieContent.class);
                    intent.putExtra("link", information.link);
                    intent.putExtra("image", information.image);
                    intent.putExtra("title", information.title);
                    context.startActivity(intent);
                }
            });
        }
        else{
            holder.itemView.findViewById(R.id.recycler_gridlist).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.recycler_dlist).setVisibility(View.VISIBLE);
            holder.dTitle.setText(information.title);
            holder.dLink.setText(information.link);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SHORTEST_API_TOKEN_LINK+information.link));
                    browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(browserIntent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder{
        TextView title, dTitle, dLink;
        ImageView image;
        public CustomViewHolder(View itemView) {
            super(itemView);
            if (flag) {
                image = (ImageView) itemView.findViewById(R.id.movie_IV);
                image.setScaleType(ImageView.ScaleType.FIT_XY);
                title = (TextView) itemView.findViewById(R.id.movie_title_TV);
            }
            else {
                dTitle = (TextView) itemView.findViewById(R.id.dTitle);
                dLink = (TextView) itemView.findViewById(R.id.dLink);
            }
        }
    }

    public int getPos(){
        return pos;
    }
}
