package com.bignerdranch.android.audiceive;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class SceneRecycleViewAdapter extends RecyclerView.Adapter<SceneRecycleViewAdapter.SceneViewHolder> {

    public static class SceneViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView sceneName;
        TextView sceneAddress;
        TextView sceneDetails;
        Button sceneLink;
        ImageView sceneImage;
        Button sceneRemove;

        SceneViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            sceneName = (TextView) itemView.findViewById(R.id.scene_name);
            sceneAddress = (TextView)itemView.findViewById(R.id.scene_address);
            sceneDetails = (TextView) itemView.findViewById(R.id.scene_details);
            sceneLink = (Button) itemView.findViewById(R.id.scene_link);
            sceneImage = (ImageView) itemView.findViewById(R.id.scene_image);
            sceneRemove = (Button) itemView.findViewById(R.id.scene_remove);

        }
    }

    List<Scene> scenes;
    Context context;

    SceneRecycleViewAdapter(List<Scene> scenes, Context context) {
        this.scenes = scenes;
        this.context = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public SceneViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.scene_card, viewGroup, false);
        SceneViewHolder svh = new SceneViewHolder(v);
        return svh;
    }

    @Override
    public void onBindViewHolder(final SceneViewHolder sceneViewHolder, int i) {
        sceneViewHolder.sceneName.setText(scenes.get(i).name);
        sceneViewHolder.sceneAddress.setText(scenes.get(i).address);
        sceneViewHolder.sceneDetails.setText(scenes.get(i).details);
        final int j = i;
        sceneViewHolder.sceneLink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse(scenes.get(j).link));
                v.getContext().startActivity(myWebLink);
            }
        });
        sceneViewHolder.sceneImage.setImageResource(scenes.get(i).imageID);

        sceneViewHolder.sceneRemove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());

                // set title
                alertDialogBuilder.setTitle("Confirmation");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Are you sure you want to delete this advertisement?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                // current activity
                                remove(j);
                                //removeScene.setVisibility(adapter.getScenes().isEmpty()?View.GONE:View.VISIBLE);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });
    }

    public void insert(int position, Scene scene) {
        scenes.add(position, scene);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        scenes.remove(position);
        notifyDataSetChanged();
    }

    public void removeAll() {
        int range = scenes.size();
        for (int i = 0; i < range; i++) {
            remove(0);
        }
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    @Override
    public long getItemId(int position) {
        return scenes.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return scenes.size();
    }
}
