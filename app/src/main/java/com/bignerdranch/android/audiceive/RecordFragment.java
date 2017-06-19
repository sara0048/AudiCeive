package com.bignerdranch.android.audiceive;

import android.Manifest;
import android.content.Context;
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
    private ShowAndSaveSceneRunnable showAndSaveSceneRunnable;
    private MyInterface listener;

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

        dbHelper.addScene("Leadenhall Market", "Gracechurch St, London EC3V 1LT",
                "Opens from 10AM-6PM, Closed on Sat/Sun\n"+
                "Leadenhall Market is a covered market located in the historic centre of the City of London financial district. Built in 1881, it is London’s most beautiful Victorian market.  The double height entrance is flanked by tall, narrow gabled red brick and Portland stone blocks in a C17 Dutch style. Under the elegant glass roof, there are stalls selling flowers, cheese, meat and other fresh food. A number of commercial retailers are also located in the market, including restaurants, clothes shops and a pen shop.",
                "https://www.cityoflondon.gov.uk/things-to-do/leadenhall-market/Pages/default.aspx",R.drawable.pic0,0);
        dbHelper.addScene("The Glass House", "2-4 Bull’s Head Passage, Leadenhall Market, London EC3V 1LU",
                "Opens from 9AM-5PM, Closed on Sat/Sun\n"+
                "An opticians store.",
                "http://glasshouseopticians.co.uk/",R.drawable.pic1,1);
        dbHelper.addScene("Warner Bro. Studios", "Warner Drive, Leavesden WD25 7LP, UK",
                "Warner Bros. Studios, Leavesden is an 80-hectare studio complex in Leavesden in Hertfordshire, in southeast England. Formerly known as Leavesden Film Studios, it is a film and media complex owned by Warner Bros. Warner Bros. Studios, Leavesden is one of only a few places in the United Kingdom where large scale film productions can be made. The studios contain approximately 50,000 m2 (538,196 sq ft) of flexible space and is one of the largest and most state-of-the-art secure filmmaking facilities in the world. From 2000, every one of the Harry Potter films was based out of Leavesden Studios over the following ten years.",
                "https://www.wbstudiotour.co.uk/the-tour-experience/explore#t-2",R.drawable.pic2,2);
        dbHelper.addScene("Exhibition Hall of Australia House", "Strand, London WC2B 4LA, UK",
                "The marble-floored and chandeliered Exhibition Hall of Australia House is the home of the Australian Embassy. It was officially opened by King George V on 3 August 1918. Australia House is usually the single largest polling station in Australian federal elections, with more votes being cast at the London polling station than at any polling station in any of the Australian States or Territories.",
                "https://sourceable.net/australia-house-london-heritage-list/",R.drawable.pic3,3);
        dbHelper.addScene("Pedestrian Sky Bridge at York Station ", "Station Rd, York YO24 1AB, UK",
                "York railway station is on the East Coast Main Line in the United Kingdom, serving the city of York, North Yorkshire. It is 188 miles 40 chains (303.4 km) down-line from London King's Cross and on the main line it is situated between Doncaster to the south and Thirsk to the north. Despite the small size of the city, York's station is one of the most important on the British railway network because of its role as a key junction approximately halfway between London and Edinburgh. The junction was historically a major site for rolling stock manufacture, maintenance and repair.",
                "http://www.nationalrail.co.uk/stations_destinations/yrk.aspx",R.drawable.pic4,4);
        dbHelper.addScene("Arched wall between platforms 4 and 5 at King’s Cross Station", "Euston Road, London, Greater London N1 9AL, UK",
                "King’s Cross railway station is a Central London railway terminus on the northern edge of the city. It is one of the busiest railway stations in the United Kingdom, being the southern terminus of the East Coast Main Line to North East England and Scotland. The station was opened in 1852 by the Great Northern Railway in the Kings Cross area to accommodate the East Coast Main Line. It quickly grew to cater for suburban lines and was expanded several times in the 19th century. In the late 20th century, the area around the station became known for its seedy and downmarket character, and was used as a backdrop for several films as a result.",
                "https://www.kingscross.co.uk/harry-potters-platform-9-34",R.drawable.pic5,5);
        dbHelper.addScene("Glenfinnan Viaduct Bridge", "A830 Rd, Glenfinnan PH37 4, UK",
                "The Glenfinnan Viaduct is a railway viaduct with a curving 21-arch span, located on the West Highland Line in Glenfinnan, Inverness-shire, Scotland and overlooks the Glenfinnan Monument and the waters of Loch Shiel. It is the longest concrete railway bridge in Scotland at 380m and over a height of 30m. To experience this scenery, one can board the Jacobite, which is a steam locomotive hauled tourist train service that operates over part of the West Highland Railway Line. The Jacobite runs a distance of 41 miles between Fort William and Mallaig, passing through an area of great scenic beauty including alongside Loch Eil, Glenfinnan Viaduct and Arisaig. Trains cross with regular service trains at Glenfinnan station.",
                "http://www.westcoastrailways.co.uk/jacobite/jacobite-steam-train-details.cfm",R.drawable.pic6,6);
        dbHelper.addScene("Goathland Railway Station ", "North York Moors National Park, Cow Wath Bank, Goathland, Whitby YO22 5NF, UK",
                "Goathland railway station is a station on the North Yorkshire Moors Railway and serves the village of Goathland in the North York Moors National Park, North Yorkshire, England. Dating from 1865, the station served for just 100 years until it closed in 1965. In 1968, Goathland became the headquarters of the fledgling North Yorkshire Moors Railway, opening to passengers again in 1973.",
                "https://www.nymr.co.uk/goathland-station",R.drawable.pic7,7);
        dbHelper.addScene("Loch Shiel", "Lochaber, Highland, Scotland",
                "Loch Shiel is a 17 1⁄2-mile-long (28 km) freshwater loch, 120 m (393 ft) deep, situated 12.4 miles west of Fort William in Lochaber, Highland, Scotland. Its nature changes considerably along its length, being deep and enclosed by mountains in the north east and shallow surrounded by bog and rough pasture in the south west, from which end the 4 km River Shiel drains to the sea in Loch Moidart near Castle Tioram. Loch Shiel is only marginally above sea level and was in fact a sea loch a few thousand years ago when sea levels (relative to Scotland) were higher.",
                "http://www.road-to-the-isles.org.uk/glenfinnan.php",R.drawable.pic8,8);
        dbHelper.addScene("Staircase at Christ Church, Oxford University", "St Aldate's, Oxford OX1 1DP, UK",
                "Christ Church is a constituent college of the University of Oxford in England.  Christchurch College’s grand 16th century stone staircase and fan-vaulted ceiling was used in the first two Harry Potter films as the staircase leading to the Great Hall. It is noteworthy that the design of the dining hall at Christ Church was an inspiration for the Great Hall at Hogwarts.",
                "http://movingknowledge.physics.ox.ac.uk/College.html",R.drawable.pic9,9);
        dbHelper.addScene("Warner Bro. Studios", "Warner Drive, Leavesden WD25 7LP, UK",
                "Warner Bros. Studios, Leavesden is an 80-hectare studio complex in Leavesden in Hertfordshire, in southeast England. Formerly known as Leavesden Film Studios, it is a film and media complex owned by Warner Bros. Warner Bros. Studios, Leavesden is one of only a few places in the United Kingdom where large scale film productions can be made. The studios contain approximately 50,000 m2 (538,196 sq ft) of flexible space and is one of the largest and most state-of-the-art secure filmmaking facilities in the world. From 2000, every one of the Harry Potter films was based out of Leavesden Studios over the following ten years.",
                "https://www.wbstudiotour.co.uk/the-tour-experience/explore#t-2",R.drawable.pic10,10);
        dbHelper.addScene("Gloucester Cathedral", "12 College Green, Gloucester GL1 2LX, UK",
                "Gloucester Cathedral, formally the Cathedral Church of St Peter and the Holy and Indivisible Trinity, in Gloucester, England, stands in the north of the city near the River Severn. It originated in 678 or 679 with the foundation of an abbey dedicated to Saint Peter (dissolved by King Henry VIII). The south porch is in the Perpendicular style, with a fan-vaulted roof, as also is the north transept, the south being transitional Decorated Gothic. The cloisters at Gloucester are the earliest surviving fan vaults, having been designed between 1351 and 1377 by Thomas de Canterbury.",
                "http://www.gloucestercathedral.org.uk/",R.drawable.pic11,11);
        dbHelper.addScene("Warner Bro. Studios", "Warner Drive, Leavesden WD25 7LP, UK",
                "Warner Bros. Studios, Leavesden is an 80-hectare studio complex in Leavesden in Hertfordshire, in southeast England. Formerly known as Leavesden Film Studios, it is a film and media complex owned by Warner Bros. Warner Bros. Studios, Leavesden is one of only a few places in the United Kingdom where large scale film productions can be made. The studios contain approximately 50,000 m2 (538,196 sq ft) of flexible space and is one of the largest and most state-of-the-art secure filmmaking facilities in the world. From 2000, every one of the Harry Potter films was based out of Leavesden Studios over the following ten years.",
                "https://www.wbstudiotour.co.uk/the-tour-experience/explore#t-2",R.drawable.pic12,12);

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
                dbHelper.addFingerprint(f.getAnchorFrequency(), f.getPointFrequency(), f.getDelta(), f.getAbsoluteTime(), i);

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
                }
                dbHelper.close();
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
                    if(match[maximum] > 10) {
                        showAndSaveSceneRunnable = new ShowAndSaveSceneRunnable();
                        showAndSaveSceneRunnable.setMatch(maximum);
                        showAndSaveSceneRunnable.run();
                    }
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

    private class ShowAndSaveSceneRunnable implements Runnable {
        int match = -1;
        String name;
        String address;
        String details;
        String link;
        int imageID;

        @Override
        public void run() {
            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            final Cursor sceneInfo = dbHelper.getSceneInfo(match);
            if (sceneInfo.moveToFirst()) {
                name = sceneInfo.getString(sceneInfo.getColumnIndex("name"));
                address = sceneInfo.getString(sceneInfo.getColumnIndex("address"));
                details = sceneInfo.getString(sceneInfo.getColumnIndex("details"));
                link = sceneInfo.getString(sceneInfo.getColumnIndex("link"));
                imageID = sceneInfo.getInt(sceneInfo.getColumnIndex("image_id"));
            }
            sceneInfo.close();
            dbHelper.close();
            final Scene scene = new Scene(name, address, details, link, imageID, match);
            listener.saveScene(scene);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRecord(true);
                    text.setText("Name:\n" + name + "\n"
                            + "Address:\n" + address + "\n"
                            + "Details:\n" + details + "\n"
                            + "Link:\n" + link);
                }
            });
        }

        public void setMatch(int match) {
            this.match = match;
        }
    }

}
