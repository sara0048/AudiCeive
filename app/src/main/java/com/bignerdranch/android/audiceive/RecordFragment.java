package com.bignerdranch.android.audiceive;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import java.io.*;
import android.os.Environment;
import java.nio.ByteBuffer;

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
    private FloatingActionButton mRecordButton = null;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private int recordInterval;
    //private String[] files = {"20000hz.wav", "20150hz.wav", "20300hz.wav", "20450hz.wav", "20600hz.wav", "20750hz.wav", "21000hz.wav", "21150hz.wav", "21300hz.wav", "21450hz.wav", "21600hz.wav", "21750hz.wav", "22000hz.wav"};
    private String[] files = {"20kHz.wav", "20.15kHz.wav", "20.3kHz.wav", "20.45kHz.wav", "20.6kHz.wav", "20.75kHz.wav", "21kHz.wav", "21.15kHz.wav", "21.3kHz.wav", "21.45kHz.wav", "21.6kHz.wav", "21.75kHz.wav", "22kHz.wav"};
    private int[] match;
    private TimerTask recordTask;
    private RecordRunnable runnable;
    private DatabaseHelper dbHelper;
    private int[] previous;

    public static RecordFragment newInstance() {
        return new RecordFragment();
    }

    private void onRecord(boolean isRecording) {
        if (!isRecording) {
            match = new int[files.length];
            previous = new int[files.length];
            for (int i = 0; i < match.length; i++) {
                previous[i] = 0;
            }
            startRecording();
            mRecordButton.setImageResource(R.drawable.ic_stop_white_36px);
            mRecordButton.setSoundEffectsEnabled(true);
            text.setText("Click on the button to stop recording");
        } else {
            stopRecording();
            mRecordButton.setImageResource(R.drawable.ic_hearing_white_36px);
            mRecordButton.setSoundEffectsEnabled(false);
            text.setText("Click on the button to start recording");
        }
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        text = (TextView) view.findViewById(R.id.text);
        mRecordButton = (FloatingActionButton) view.findViewById(R.id.start_record);
        mRecordButton.setImageResource(R.drawable.ic_hearing_white_36px);
        mRecordButton.setSoundEffectsEnabled(false);

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            0);
                else
                    onRecord(isRecording);
            }
        });

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
        // Renew database upon AudioAnalysis parameters change
        //start comment
        /*
        dbHelper = new DatabaseHelper(getContext());
        dbHelper.refreshDatabase();
        for (int i = 0; i < files.length; i++) {
            String fileNames = files[i];
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileNames);
            byte[] imgDataBa = new byte[(int) file.length()];

            DataInputStream dataIs;
            try {
                dataIs = new DataInputStream(new FileInputStream(file));
                dataIs.readFully(imgDataBa);
            } catch (IOException e) {
                e.printStackTrace();
            }

            short[] shorts = new short[imgDataBa.length / 2];
            // to turn bytes to shorts as either big endian or little endian.
            ByteBuffer.wrap(imgDataBa).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

            ArrayList<Fingerprint> fingerprints = AudioAnalysis.fingerprint(shorts);

            for (Fingerprint f : fingerprints)
                dbHelper.insertFingerprint(f.getAnchorFrequency(), f.getPointFrequency(), f.getDelta(), f.getAbsoluteTime(), i);

        }
        */
        //end comment
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isRecording) {
            onRecord(true);
        }
    }

    private class RecordTask extends TimerTask {
        public void run() {
            if (recorder != null) {
                stopRecording();
                ArrayList<Fingerprint> fingerprints = AudioAnalysis.fingerprint(audio);
                audio = new short[RECORDER_SAMPLERATE * recordInterval];
                startRecording();
                dbHelper = new DatabaseHelper(getContext());
                HashMap<ArrayList<Integer>, ArrayList<Integer>> targetZoneMap = new HashMap<>();
                SparseIntArray[] timeCoherencyMap = new SparseIntArray[files.length];
                for (int i = 0; i < files.length; i++)
                    timeCoherencyMap[i] = new SparseIntArray();
                for (Fingerprint f : fingerprints) {
                    Cursor couples = dbHelper.getData(f.getAnchorFrequency(), f.getPointFrequency(), f.getDelta());
                    if (couples.moveToFirst()) {
                        do {
                            Integer id = couples.getInt(couples.getColumnIndex("scene_id"));
                            Integer absoluteTime = couples.getInt(couples.getColumnIndex("absolute_time"));
                            Integer delta = f.getAbsoluteTime() - absoluteTime;
                            ArrayList<Integer> couple = new ArrayList<>();
                            couple.add(id);
                            couple.add(absoluteTime);
                            ArrayList<Integer> a;
                            if ((a = targetZoneMap.get(couple)) != null) {
                                a.add(delta);
                                targetZoneMap.put(couple, a);
                            } else {
                                a = new ArrayList<>();
                                a.add(delta);
                                targetZoneMap.put(couple, a);
                            }
                        } while (couples.moveToNext());
                    }
                    couples.close();
                    dbHelper.close();
                }
                for (ArrayList<Integer> i : targetZoneMap.keySet()) {
                    ArrayList<Integer> a = targetZoneMap.get(i);
                    if (a.size() >= 3)
                        for (Integer delta : a) {
                            Integer count = timeCoherencyMap[i.get(0)].get(delta);
                            timeCoherencyMap[i.get(0)].put(delta, count == 0 ? 1 : count + 1);
                        }
                }
                // Currently assume most common delta is the correct delta
                for (int i = 0; i < match.length; i++) {
                    SparseIntArray s = timeCoherencyMap[i];
                    int currentMaxDeltaCount = 0;
                    for (int j = 0; j < s.size(); j++) {
                        Integer delta = s.keyAt(j);
                        if (s.get(delta) >= currentMaxDeltaCount) {
                            currentMaxDeltaCount = s.get(delta);
                        }
                    }
                    match[i] += currentMaxDeltaCount;
                }

                int maximum = -1;
                for (int i = 0; i < match.length; i++) {
                    if ((match[i] - previous[i]) > 10) {
                        maximum = i;
                        break;
                    }
                }

                for (int i = 0; i < match.length; i++) {
                    previous[i] = match[i];
                }

                if (maximum > -1 && isRecording) {
                    final String MATCHING_FREQUENCY = files[maximum];
                    text.post(new Runnable() {
                        public void run() {
                            text.setText("Match found: " + MATCHING_FREQUENCY);
                        }
                    });
                }
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
