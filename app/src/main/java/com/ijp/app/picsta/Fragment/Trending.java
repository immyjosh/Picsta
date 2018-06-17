package com.ijp.app.picsta.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ijp.app.picsta.Common.Common;
import com.ijp.app.picsta.Interface.ItemClickListener;
import com.ijp.app.picsta.ListWallpaper;
import com.ijp.app.picsta.Model.WallpaperItem;
import com.ijp.app.picsta.R;
import com.ijp.app.picsta.ViewHolder.ListWallpaperViewHolder;
import com.ijp.app.picsta.ViewWallpaper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class Trending extends Fragment {

    RecyclerView recyclerView;

    FirebaseDatabase database;
    DatabaseReference categoryBackground;

    FirebaseRecyclerOptions<WallpaperItem> options;
    FirebaseRecyclerAdapter<WallpaperItem,ListWallpaperViewHolder> adapter;

    private static Trending INSTANCE=null;
    public Trending() {
        // Init Firebase
        database=FirebaseDatabase.getInstance();
        categoryBackground=database.getReference(Common.STR_WALLPAPER);

        Query query=categoryBackground.orderByChild("viewCount").limitToLast(10); // get 10 item which has biggest view count

        options=new FirebaseRecyclerOptions.Builder<WallpaperItem>()
                .setQuery(query,WallpaperItem.class)
                .build();

        adapter=new FirebaseRecyclerAdapter<WallpaperItem, ListWallpaperViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ListWallpaperViewHolder holder, int position, @NonNull final WallpaperItem model) {
                Picasso.with(getActivity())
                        .load(model.getImagelink())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.wallpaper, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(getActivity())
                                        .load(model.getImagelink())
                                        .error(R.drawable.ic_terrain_black_24dp)
                                        .into(holder.wallpaper, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Log.e("ERROR","COULD NOT FETCH IMAGE");
                                            }
                                        });
                            }
                        });

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        Intent intent=new Intent(getActivity(),ViewWallpaper.class);
                        Common.selectBackground=model;
                        Common.selectBackgroundKey=adapter.getRef(position).getKey();
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public ListWallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_wallpaper_item,parent,false);
                int height=parent.getMeasuredHeight()/2;
                itemView.setMinimumHeight(height);
                return new ListWallpaperViewHolder(itemView);
            }
        };
    }

    public static Trending getInstance()
    {
        if(INSTANCE==null)
            INSTANCE=new Trending();
        return INSTANCE;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_daily_popular, container, false);
        recyclerView=view.findViewById(R.id.recycler_trending);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());

        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        loadTrendingList();

        return view;
    }

    private void loadTrendingList() {
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        if(adapter!=null)
        adapter.stopListening();
        super.onStop();
    }

    @Override
    public void onStart() {
        if(adapter!=null)
            adapter.startListening();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.startListening();
    }
}
