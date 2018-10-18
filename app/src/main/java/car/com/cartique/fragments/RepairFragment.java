package car.com.cartique.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import car.com.cartique.adapter.FeedListAdapter;
import car.com.cartique.feed.ConnectionDetector;
import car.com.cartique.model.FeedItem;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by napster on 1/18/18.
 */

public class RepairFragment extends Fragment {
    LinearLayoutManager layoutManager;
    Activity activity;
    com.bhargavms.dotloader.DotLoader bar;
    JSONArray cache_array;
    ConnectionDetector connection;
    CoordinatorLayout coordinatorLayout;
    Boolean isUpdate = false;
    private RecyclerView recyclerView;
    private FeedListAdapter listAdapter;
    private List<FeedItem> feedItems;
    //NotificationUpdateTask updateTask;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = getActivity();
    }


}
