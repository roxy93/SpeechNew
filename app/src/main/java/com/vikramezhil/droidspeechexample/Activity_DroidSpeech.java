package com.vikramezhil.droidspeechexample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;
import com.vikramezhil.droidspeech.OnDSPermissionsListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import static com.vikramezhil.droidspeechexample.Function.createFile;
import static com.vikramezhil.droidspeechexample.Function.write;
import static com.vikramezhil.droidspeechexample.Function.writeToSDFile;
import static com.vikramezhil.droidspeechexample.Function.msToString;

/**
 * Droid Speech Example Activity
 *
 * @author Vikram Ezhil
 */

public class Activity_DroidSpeech extends Activity implements OnClickListener, OnDSListener, OnDSPermissionsListener
{
    public final String TAG = "Activity_DroidSpeech";

    private DroidSpeech droidSpeech;
    private TextView finalSpeechResult;
    private ImageView start, stop;
    private Boolean newFile = true;
    private Boolean firstWord = true;
    private long tFin = 0;
    private long tIni = 0;
    private long timeTotal =0;
    private long seg = 0;
    private int min = 0;
    private int hora = 0;
    private String tiempo = "";
    File file = createFile ();

    // MARK: Activity Methods

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Setting the layout;[.
        setContentView(R.layout.activity_droid_speech);

        // Initializing the droid speech and setting the listener
        droidSpeech = new DroidSpeech(this, getFragmentManager());
        droidSpeech.setOnDroidSpeechListener(this);
        droidSpeech.setShowRecognitionProgressView(true);
        droidSpeech.setOneStepResultVerify(false);
        droidSpeech.setRecognitionProgressMsgColor(Color.WHITE);
        droidSpeech.setOneStepVerifyConfirmTextColor(Color.WHITE);
        droidSpeech.setOneStepVerifyRetryTextColor(Color.WHITE);
        droidSpeech.setContinuousSpeechRecognition(true);

        finalSpeechResult = findViewById(R.id.finalSpeechResult);

        start = findViewById(R.id.start);
        start.setOnClickListener(this);

        stop = findViewById(R.id.stop);
        stop.setOnClickListener(this);

    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if(stop.getVisibility() == View.VISIBLE)
        {
            stop.performClick();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(stop.getVisibility() == View.VISIBLE)
        {
            stop.performClick();
        }
    }

    // MARK: OnClickListener Method

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.start:

                // Starting droid speech
                droidSpeech.startDroidSpeechRecognition();

                // Setting the view visibilities when droid speech is running
                start.setVisibility(View.GONE);
                stop.setVisibility(View.VISIBLE);
                tIni = System.currentTimeMillis();//tiempo de referencia
                break;

            case R.id.stop:

                // Closing droid speech
                droidSpeech.closeDroidSpeechOperations();

                stop.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);

                break;
        }
    }

    // MARK: DroidSpeechListener Methods

    @Override
    public void onDroidSpeechSupportedLanguages(String currentSpeechLanguage, List<String> supportedSpeechLanguages)
    {
        Log.i(TAG, "Current speech language = " + currentSpeechLanguage);
        Log.i(TAG, "Supported speech languages = " + supportedSpeechLanguages.toString());

        if(supportedSpeechLanguages.contains("es-ES"/*ta-IN*/))
        {
            // Setting the droid speech preferred language as tamil if found
            droidSpeech.setPreferredLanguage("es-ES");

            // Setting the confirm and retry text in tamil
            droidSpeech.setOneStepVerifyConfirmText("Confirm");
            droidSpeech.setOneStepVerifyRetryText("Retry");
        }
    }

    @Override
    public void onDroidSpeechRmsChanged(float rmsChangedValue)
    {
         Log.i(TAG, "Rms change value = " + rmsChangedValue);
    }

    @Override
    public void onDroidSpeechLiveResult(String liveSpeechResult)
    {
        Log.i(TAG, "Live speech result = " + liveSpeechResult);

        if(newFile){
            write(file); //crear por primera vez el archivo
            newFile = false;
        }

        if (firstWord) {// primera palabra para reconocer

            tFin = System.currentTimeMillis();//ver si debo cambiar los 1000
            timeTotal = tFin - tIni;
            tiempo = msToString(timeTotal, true);
            writeToSDFile(tiempo,file);
            firstWord = false;
        }

        //tFin = System.currentTimeMillis(); //tiempo final
    }

    @Override
    public void onDroidSpeechFinalResult(String finalSpeechResult)//finalSpeechResult is the final text
    {
        // Setting the final speech result
        this.finalSpeechResult.setText(finalSpeechResult);

        firstWord = true;//la siguiente palabra es la primera de un nuevo párrafo

        if (finalSpeechResult != "") {//words2
            //writeToSDFile(" S" + hora + ":" + min + ":" + seg + " ",file);
            tFin = System.currentTimeMillis();
            timeTotal = tFin - tIni;
            tiempo = msToString(timeTotal, false);
            writeToSDFile(finalSpeechResult + " ",file);
            writeToSDFile(tiempo + '\n',file);
        }

        if(droidSpeech.getContinuousSpeechRecognition())
        {
            int[] colorPallets1 = new int[] {Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA};
            int[] colorPallets2 = new int[] {Color.YELLOW, Color.RED, Color.CYAN, Color.BLUE, Color.GREEN};

            // Setting random color pallets to the recognition progress view
            droidSpeech.setRecognitionProgressViewColors(new Random().nextInt(2) == 0 ? colorPallets1 : colorPallets2);
        }
        else
        {
            stop.setVisibility(View.GONE);
            start.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDroidSpeechClosedByUser()
    {
        stop.setVisibility(View.GONE);
        start.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDroidSpeechError(String errorMsg)
    {
        // Speech error
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();

        stop.post(new Runnable()
        {
            @Override
            public void run()
            {
                // Stop listening
                stop.performClick();
            }
        });
    }

    // MARK: DroidSpeechPermissionsListener Method

    @Override
    public void onDroidSpeechAudioPermissionStatus(boolean audioPermissionGiven, String errorMsgIfAny)
    {
        if(audioPermissionGiven)
        {
            start.post(new Runnable()
            {
                @Override
                public void run()
                {
                    // Start listening
                    start.performClick();
                }
            });
        }
        else
        {
            if(errorMsgIfAny != null)
            {
                // Permissions error
                Toast.makeText(this, errorMsgIfAny, Toast.LENGTH_LONG).show();
            }

            stop.post(new Runnable()
            {
                @Override
                public void run()
                {
                    // Stop listening
                    stop.performClick();
                }
            });
        }
    }

}

class Function {

    public static File createFile (){
        File root = android.os.Environment.getExternalStorageDirectory();

        File dir = new File (root.getAbsolutePath() + "/folder");

        if (dir.exists()){
            dir.delete();
        }
        dir.mkdirs();

        File file = new File(dir, "text.txt");

        if (dir.exists()){
            file.delete();
        }

        return file;
    }

    public static void writeToSDFile(String speechToTextData, File file){

        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

        try {
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(speechToTextData);
            bw.flush();
            bw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write (File file) {
        try {
            FileOutputStream f = new FileOutputStream(file);
            f.write("".getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String msToString (long ms, Boolean start) {
        long seg = 0;
        int min = 0;
        int hora = 0;
        seg = ms/1000;
        String tiempo= "";

        if (seg<60){
            min = 0;
        }
        else{
            min = (int) (seg/60);
            seg = seg - (min*60);
        }

        if (min<60){
            hora = 0;

        }
        else{
            hora = (min/60);
            min = min - (hora*60);
        }
        if (start==true){
            tiempo = " S" + hora + ":" + min + ":" + seg + " ";
        }else{
            tiempo = " F" + hora + ":" + min + ":" + seg + " ";
        }

        return tiempo;
    }
}
