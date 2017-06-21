package com.bignerdranch.android.audiceive;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.test.espresso.core.deps.guava.reflect.TypeToken;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

// In this case, the fragment displays simple text based on the page
public class RecentsFragment extends Fragment {

    SharedPreferences sharedpreferences;
    FloatingActionButton removeScene;
    SharedPreferences.Editor prefsEditor;
    Gson gson = new Gson();
    SceneRecycleViewAdapter adapter;
    private List<Scene> scenes = new ArrayList<>();
    private String jsonScenes;
    private RecyclerView rv;

    public static RecentsFragment newInstance() {
        RecentsFragment fragment = new RecentsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        jsonScenes = sharedpreferences.getString("sceneList", "");
        if (!jsonScenes.isEmpty()) {
            Type type = new TypeToken<List<Scene>>() {}.getType();
            ArrayList<Scene> newlist = gson.fromJson(jsonScenes, type);
            scenes.addAll(newlist);
        }
        adapter = new SceneRecycleViewAdapter(scenes, getActivity().getApplication());
        adapter.setHasStableIds(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_fragment, container, false);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity()) {
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        };
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        removeScene = (FloatingActionButton) view.findViewById(R.id.remove_scene);
        //removeScene.setVisibility(scenes.isEmpty()?View.GONE:View.VISIBLE);
        removeScene.setImageResource(R.drawable.ic_clear_white_36px);
        removeScene.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                // set title
                alertDialogBuilder.setTitle("Confirmation");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Are you sure you want to delete all saved scenes?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                // current activity
                                adapter.removeAll();
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

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        rv.setAdapter(adapter);

        /*Call your Fragment functions that uses getActivity()
        if (isVisible && isStarted) {
            updateView();
        }
        */
    }

    @Override
    public void onStop() {
        super.onStop();
        prefsEditor = sharedpreferences.edit();
        jsonScenes = gson.toJson(adapter.getScenes());
        prefsEditor.putString("sceneList", jsonScenes);
        prefsEditor.apply();
        //Log.d("TAG","jsonCars = " + jsonScenes);
    }

    public void addScene(Scene scene) {
        adapter.insert(0, scene);
    }

}
