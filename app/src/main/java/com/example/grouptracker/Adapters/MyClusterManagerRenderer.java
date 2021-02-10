package com.example.grouptracker.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.grouptracker.Model.ClusterMarker;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.CircleBubbleTransformation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker> {

    private IconGenerator iconGenerator;
    private IconGenerator clusterIconGenerator;
    private ImageView imageView;
    private CircleImageView clusterImageView;
    private final int dimensions;
    private Bitmap icon;
    private float maxZoomLevel;
    private final float zoomOffset = 0f;

    public MyClusterManagerRenderer(Context context, GoogleMap googleMap,
                                    ClusterManager<ClusterMarker> clusterManager) {

        super(context, googleMap, clusterManager);

        // initialize cluster item icon generator
        iconGenerator = new IconGenerator(context.getApplicationContext());
        clusterIconGenerator = new IconGenerator(context.getApplicationContext());
        iconGenerator.setBackground(context.getResources().getDrawable(R.color.transparent));
        imageView = new ImageView(context.getApplicationContext());
        clusterImageView = new CircleImageView(context.getApplicationContext());
        dimensions = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        clusterImageView.setLayoutParams(new ViewGroup.LayoutParams(dimensions, dimensions));
        imageView.setLayoutParams(new ViewGroup.LayoutParams(dimensions, dimensions));
        int padding = (int) context.getResources().getDimension(R.dimen.custom_marker_padding);
        imageView.setPadding(padding, padding, padding, padding);
        iconGenerator.setContentView(imageView);
        clusterIconGenerator.setContentView(clusterImageView);
    }

    /**
     * Rendering of the individual ClusterItems
     * @param item
     * @param markerOptions
     */
    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {
        markerOptions
                .icon(getItemIcon(item))
                .title(item.getTitle())
                .snippet(item.getSnippet());
    }

    protected void onBeforeClusterRendered(ClusterMarker marker, MarkerOptions markerOptions) {
        markerOptions
                .icon(getClusterIcon(marker));
    }

    @Override
    protected void onClusterRendered(@NonNull final Cluster<ClusterMarker> cluster, @NonNull final Marker marker) {
            /*
            final List<Drawable> profilePhotos = new ArrayList<>(Math.min(4, cluster.getSize()));
            final Bitmap dummyBitmap = null;
            Drawable drawable;
            final int clusterSize = cluster.getSize();
            final int[] count = {0};

            for (ClusterMarker item : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                try {
                    Picasso.get().load(item.getIconPicture())
                            .into(clusterImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Drawable photo = clusterImageView.getDrawable();
                                    photo.setBounds(0, 0, dimensions, dimensions);
                                    profilePhotos.add(photo);
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, dimensions, dimensions);

            clusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = clusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
             */

    }

    private BitmapDescriptor getClusterIcon(ClusterMarker marker) {
        Picasso.get()
                .load(Uri.parse(marker.getIconPicture()))
                .into(imageView);

        iconGenerator.setContentView(imageView);
        Bitmap icon = iconGenerator.makeIcon();
        return BitmapDescriptorFactory.fromBitmap(icon);
            /*
            final List<Drawable> profilePhotos = new ArrayList<>(Math.min(4, cluster.getSize()));

            for (ClusterMarker p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                try {
                    Picasso.get().load(p.getIconPicture())
                            .into(clusterImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Drawable photo = clusterImageView.getDrawable();
                                    photo.setBounds(0, 0, dimensions, dimensions);
                                    profilePhotos.add(photo);
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, dimensions, dimensions);

            clusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = clusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            return BitmapDescriptorFactory.fromBitmap(icon);
             */
    }

    private BitmapDescriptor getItemIcon(final ClusterMarker item) {
        if(item.getIconPicture().equals("")) {
            if(item.getUser().getGender().equals("Male")) {
                Picasso.get()
                        .load(R.mipmap.default_profile_male)
                        .resize(200, 200)
                        .centerCrop()
                        .transform(new CircleBubbleTransformation())
                        .into(imageView);
            } else {
                Picasso.get()
                        .load(R.mipmap.default_female_photo)
                        .resize(200, 200)
                        .centerCrop()
                        .transform(new CircleBubbleTransformation())
                        .into(imageView);
            }
            //bmp = new CircleBubbleTransformation().transform(bmp);
            //imageView.setImageBitmap(bmp);
        } else {
            Picasso.get()
                    .load(Uri.parse(item.getUser().getImageUri()))
                    .resize(200, 200)
                    .centerCrop()
                    .transform(new CircleBubbleTransformation())
                    .into(imageView);
        }

        iconGenerator.setContentView(imageView);
        icon = iconGenerator.makeIcon();
        return BitmapDescriptorFactory.fromBitmap(icon);
    }

    @Override
    public void setOnClusterInfoWindowClickListener(ClusterManager.OnClusterInfoWindowClickListener<ClusterMarker> listener) {
        super.setOnClusterInfoWindowClickListener(listener);
    }

    @Override
    protected void onClusterItemUpdated(@NonNull ClusterMarker item, @NonNull Marker marker) {
        marker.setIcon(getItemIcon(item));
        marker.setTitle(item.getTitle());
        marker.setSnippet(item.getSnippet());
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        //boolean wouldCluster = super.shouldRenderAsCluster(cluster);

        // determine if when we want to cluster based on zoomLevel
        //if(wouldCluster) {
        //    wouldCluster = currentZoom < maxZoomLevel;
        //}
        //if(!shouldShowCluster && cluster.getSize() > 1) {
        //    return false;
        //}
        return cluster.getSize() > 1;
    }

    /**
     * Update the GPS coordinate of a ClusterItem
     * @param clusterMarker
     */
    public void setUpdateMarker(ClusterMarker clusterMarker) {
        Marker marker = getMarker(clusterMarker);
        if (marker != null) {
            marker.setPosition(clusterMarker.getPosition());
        }
    }
/*
        private BitmapDescriptor getClusterIcon(Cluster<ClusterMarker> cluster) {
            List<Drawable> profilePhotos = new ArrayList<>(Math.min(4, cluster.getSize()));
            int width = 50;
            int height = 50;
            URL url = null;
            Bitmap bmp;

            for(ClusterMarker item : cluster.getItems()) {
                if(profilePhotos.size() == 4) break;
                try{
                    url = new URL(item.getUser().getImageUri());
                    bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            clusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = clusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            return BitmapDescriptorFactory.fromBitmap(icon);
        }
 */
}
