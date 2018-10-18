package car.com.cartique.adapter;

/**
 * Created by napster on 1/18/18.
 */

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import car.com.cartique.R;
import car.com.cartique.ServiceBookDetails;
import car.com.cartique.app.AppController;
import car.com.cartique.app.CircularNetworkImageView;
import car.com.cartique.app.FeedImageView;
import car.com.cartique.feed.ConnectionDetector;
import car.com.cartique.model.Client;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FeedListAdapter extends RecyclerView.Adapter<FeedListAdapter.ViewHolder> {
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private Activity activity;
    private List<Client> feedItems;
    private ViewHolder holder;
    private ConnectionDetector connection;
    private View v;
    private FirebaseStorage storage;

    public FeedListAdapter(Activity activity, List<Client> feedItems) {
        this.activity = activity;
        this.feedItems = feedItems;
        connection = new ConnectionDetector(this.activity);
        storage = FirebaseStorage.getInstance();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        final Client item = feedItems.get(position);
        holder.feedname.setText(item.getName());
        holder.location.setText(item.getAddress());
        holder.bio.setText(item.getBio());

        try {

            //Timestamp stamp = Timestamp.valueOf(System.currentTimeMillis());
            //final long now = System.currentTimeMillis();
            // final long timeGap = (now - stamp.getTime())/1000;

            // CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
            //       stamp.getTime(),now, DateUtils.SECOND_IN_MILLIS,DateUtils.FORMAT_ABBREV_RELATIVE);
            //holder.timestamp.setText(timeAgo);
        } catch (Exception ex) {
            System.out.print(ex.getMessage());
            ex.printStackTrace();
        } finally {

        }

        // user profile pic
       // holder.profilePic.setImageUrl(item.getImageUrl(), imageLoader);
        // Feed image
        if (item.getImageUrl() != null) {
            //holder.feedImageView.setImageUrl(item.getImageUrl(), imageLoader);
            //StorageReference httpsReference = storage.getReferenceFromUrl(item.getImageUrl());
            Picasso.with(activity.getApplicationContext())
                    .load(item.getImageUrl())
                    .fit()
                    .into(holder.feedImageView);
            Picasso.with(activity.getApplicationContext())
                    .load(item.getImageUrl())
                    .fit()
                    .transform(new CircularNetworkImageView(activity.getApplicationContext()))
                    .into(holder.profilePic);
            holder.feedImageView.setVisibility(View.VISIBLE);
        } else {
            holder.feedImageView.setVisibility(View.GONE);
        }

        holder.feedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity.getApplicationContext(), ServiceBookDetails.class);
                intent.putExtra("name", item.getName());
                intent.putExtra("image", item.getImageUrl());
                intent.putExtra("clientID", item.getUserID());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                view.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void insert(int position, Client data) {
        feedItems.add(position, data);
        notifyItemInserted(position);
    }

    public void navigateToDetails() {
        //Intent intent = new Intent(activity.getApplicationContext(),FeedDetails.class);
        //this.activity.startActivity(intent);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircularNetworkImageView profilePic;
        TextView feedname;
        ImageView feedImageView;
        TextView location;
        TextView bio;
        LinearLayout layout;

        ViewHolder(View itemView) {
            super(itemView);

            feedname = itemView.findViewById(R.id.name);
            profilePic = itemView
                    .findViewById(R.id.profilePic);
            feedImageView = itemView
                    .findViewById(R.id.feedImage1);
            location = itemView.findViewById(R.id.location);
            bio = itemView.findViewById(R.id.bio);
            layout = itemView.findViewById(R.id.hometab_linearlayout);

        }

    }
}

