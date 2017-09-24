package com.bignerdranch.android.audiceive;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.android.volley.VolleyError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// In this case, the fragment displays simple text based on the page
public class RecordFragment extends Fragment {

    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static int BUFFER_SIZE = AudioRecord.getMinBufferSize(
            RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    SharedPreferences sharedpreferences;
    private short[] audio;
    private TextView text;
    private Timer timer = null;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private int recordInterval;
    private int currentMaximum;
    private SparseIntArray match;
    private TimerTask recordTask;
    private RecordRunnable runnable;
    private SparseIntArray previous;
    private MyInterface listener;
    private LinearLayout placeholder;
    private static String previoustitle="";

    public static RecordFragment newInstance() {
        return new RecordFragment();
    }

    private void startRecording() {
        recorder.startRecording();
        isRecording = true;
        runnable = new RecordRunnable();
        recordingThread = new Thread(runnable, "AudioRecorder Thread");
        recordingThread.start();
        recordTask = new RecordTask();
        timer = new Timer();
        timer.schedule(recordTask, recordInterval * 1000);
    }

    private void stopRecording() {
        if (recorder != null && runnable != null) {
            recordTask.cancel();
            runnable.stop();
            runnable = null;
            recordingThread = null;
            recorder.stop();
            isRecording = false;
        }
    }

    public static void setPrevious(String prevtitle) {
        previoustitle = prevtitle;
    }

    public static String getPrevious(){
        return previoustitle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.record_fragment, container, false);
        placeholder = (LinearLayout) view.findViewById(R.id.placeholder);
        placeholder.setOrientation(LinearLayout.VERTICAL);
        text = new TextView(getContext());
        text.setGravity(Gravity.CENTER);
        text.setText("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nPlace the phone close to the audio");
        placeholder.addView(text);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        recordInterval = Integer.parseInt(sharedpreferences.getString("recInterval", "2"));
        audio = new short[RECORDER_SAMPLERATE * recordInterval];
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BUFFER_SIZE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            match = new SparseIntArray();
            previous = new SparseIntArray();
            for (int i = 0; i < match.size(); i++) {
                previous.put(match.keyAt(i), 0);
            }
            currentMaximum = -1;
            startRecording();
        }
        else
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isRecording) {
            stopRecording();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (!isRecording && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            startRecording();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MyInterface) {
            listener = (MyInterface) context;
        }
    }

    @Override
    public void onDetach(){
        listener = null;
        super.onDetach();
    }

    private class RecordTask extends TimerTask {
        public void run() {
            if (recorder != null) {
                stopRecording();
                ArrayList<Fingerprint> fingerprints = AudioAnalysis.fingerprint(audio);
                for (Fingerprint f : fingerprints)
                    Log.d("Fingerprint", f.getAnchorFrequency() + " " + f.getPointFrequency() + " " + f.getDelta());

                audio = new short[RECORDER_SAMPLERATE * recordInterval];
                startRecording();

                final RequestQueue queue = Volley.newRequestQueue(getContext());
                final Gson gson = new Gson();

                JSONArray fingerprintJsonArray = null;
                try {
                    fingerprintJsonArray = new JSONArray(gson.toJson(fingerprints));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final JsonArrayRequest scoreRequest = new JsonArrayRequest(Request.Method.POST, "http://192.168.1.11/fingerprint_score.php", fingerprintJsonArray, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("I got", "response");
                        Score[] update = gson.fromJson(response.toString(), new TypeToken<Score[]>() {
                        }.getType());
                        for (Score s : update) {
                            Log.d(Integer.toString(s.getSceneID()), Integer.toString(s.getScore()));
                            if (s.getScore() > 10 && currentMaximum != s.getSceneID()) {
                                //display
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject().put("sceneID", s.getSceneID());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                JsonObjectRequest sceneRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.1.11/scene_search.php", jsonObject, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        final Scene scene = gson.fromJson(response.toString(), new TypeToken<Scene>() {
                                        }.getType());
                                        if ( !(RecordFragment.getPrevious().equals(scene.getName())) || (RecentsFragment.getRemoved()==1) ) {
                                            listener.saveScene(scene);
                                            RecordFragment.setPrevious(scene.getName());
                                            RecentsFragment.setRemoved(0);
                                        }
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                placeholder.removeAllViews();
                                                List<Scene> matchingScene = new ArrayList<>();
                                                matchingScene.add(scene);
                                                RecyclerView showMatchingScene = new RecyclerView(getContext());
                                                showMatchingScene.setLayoutManager(new LinearLayoutManager(getContext()));
                                                showMatchingScene.setHasFixedSize(true);
                                                SceneRecycleViewAdapter showMatchingSceneAdapter = new SceneRecycleViewAdapter(matchingScene, getContext());
                                                showMatchingScene.setAdapter(showMatchingSceneAdapter);
                                                placeholder.addView(showMatchingScene);
                                                TextView footer = new TextView(getContext());
                                                footer.setGravity(Gravity.CENTER);
                                                footer.setText("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nPlace the phone close to the audio");
                                                placeholder.addView(footer);
                                            }
                                        });
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        error.printStackTrace();
                                    }
                                });
                                queue.add(sceneRequest);
                                currentMaximum = s.getSceneID();
                                break;
                            }
                        }

                        }}, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });

                queue.add(scoreRequest);
            }
        }
    }

    private class RecordRunnable implements Runnable {
        private volatile boolean isStopped = false;

        public void run() {
            if (!isStopped) {
                short sData[] = new short[BUFFER_SIZE / 2];
                int index = 0;
                while (isRecording) {
                    recorder.read(sData, 0, BUFFER_SIZE / 2);
                    if (index + sData.length <= audio.length) {
                        System.arraycopy(sData, 0, audio, index, sData.length);
                        index += sData.length;
                    }
                }
            }
        }

        void stop() {
            isStopped = true;
        }
    }

}
