package ahmed_elkady.projecand;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.beacodes.BeacodeScanner;
import android.support.v7.app.ActionBar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements BeacodeScanner.Listener{
         Drawable play ;//play  icon
         Drawable stop ;//pause icon
         //buttons declaration
         Button help;
         Button police;
         Button following;
         boolean helpPlaying=false;
         boolean policePlaying=false;
         boolean followingPlaying=false;
         MediaPlayer helpMp;
         MediaPlayer policeMp;
         MediaPlayer followingMp;
         ImageView hImage; //help image
         ImageView hImageG; //help image grey
         ImageView pImage;  //police image
         ImageView pImageG;//police image grey
         ImageView fImage; //following image
         ImageView fImageG;//following image grey
         TextView distress;
         TextView policeCounter;
         TextView helpCounter;
         TextView followingCounter;
         int pCounter; //police counter
         int hCounter; //help counter
         int fCounter; //following counter
         boolean flash=true;
         //checkbox and shared preferences to remember last state

         CheckBox check ;
         SharedPreferences pref;
         SharedPreferences.Editor editor;


         //main view variables
//         ListView listenList;
//         List<String> list;
//         ArrayAdapter<String> arrayAdapter;

         //notifications
         NotificationCompat.Builder notifications;
         NotificationManager mNotificationManager;
         String message;
         boolean appOpen=false;


    //beacodes variables for mic

    private static final int MIC_REQUEST_CODE = 1;
    private static boolean isDialogShown = false;
    // private TextView mTextMessage;

    //navigating between views
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    findViewById(R.id.send).setVisibility(View.GONE);
                    findViewById(R.id.settings).setVisibility(View.GONE);
                    findViewById(R.id.home).setVisibility(View.VISIBLE);

                    if (!isDialogShown) {
                        checkPermissions();}
                    if(BeacodeScanner.getState() != BeacodeScanner.State.RUNNING)
                        BeacodeScanner.start();
                    return true;

                case R.id.navigation_dashboard:
                    findViewById(R.id.send).setVisibility(View.VISIBLE);
                    findViewById(R.id.settings).setVisibility(View.GONE);
                    findViewById(R.id.home).setVisibility(View.GONE);

                    if(BeacodeScanner.getState() != BeacodeScanner.State.STOPPED && !check.isChecked())
                        BeacodeScanner.stop();
                    return true;

                case R.id.navigation_notifications:
                    findViewById(R.id.send).setVisibility(View.GONE);
                    findViewById(R.id.settings).setVisibility(View.VISIBLE);
                    findViewById(R.id.home).setVisibility(View.GONE);

                    if(BeacodeScanner.getState() != BeacodeScanner.State.STOPPED && !check.isChecked())
                        BeacodeScanner.stop();
                    return true;
            }
            return false;
        }

    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing variable values
        play= this.getResources().getDrawable(R.drawable.icons8_play);
        stop= this.getResources().getDrawable(R.drawable.icons8_stop);
        help= (Button)findViewById(R.id.help);
        police= (Button)findViewById(R.id.police);
        following= (Button)findViewById(R.id.following);

        helpMp = MediaPlayer.create(this, R.raw.help_me);
        policeMp = MediaPlayer.create(this, R.raw.contact_the_police);
        followingMp = MediaPlayer.create(this, R.raw.someone_is_following);

        helpMp.setLooping(true);
        followingMp.setLooping(true);
        policeMp.setLooping(true);

        //checkbox setup
        check = (CheckBox) findViewById(R.id.checkBox);
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        check.setChecked(pref.getBoolean("listen", false));

//intialization of images and values for listening page

        hImage= findViewById(R.id.help_image);
        hImageG= findViewById(R.id.help_image_grey);
        pImage=findViewById(R.id.police_image);
        pImageG=findViewById(R.id.police_image_grey);
        fImage=findViewById(R.id.following_image);
        fImageG=findViewById(R.id.following_image_grey);
        policeCounter=findViewById(R.id.police_counter);
        helpCounter=findViewById(R.id.help_counter);
        followingCounter=findViewById(R.id.following_counter);
        pCounter=0;
        fCounter=0;
        hCounter=0;
        distress=findViewById(R.id.distress);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        ////////////////////////////////////////////////////////////////
        //setting up beacodes
        BeacodeScanner.setListener(this);
        BeacodeScanner.setProfile(1);
        // listen on all standard channels
        long[] channelSpecs = new long[8];
        for (int i = 0; i < 8; ++i)
            channelSpecs[i] = BeacodeScanner.getStandardChannelSpec(i);
        BeacodeScanner.setChannelSpecs(channelSpecs);


        if(check.isChecked()){
            BeacodeScanner.start();
        }

        /////////////////////////////////////////////////////////////////


        //push notifications setup
        notifications = new NotificationCompat.Builder(this);
        notifications.setSmallIcon(R.drawable.icons8_helping_hand);
        notifications.setContentTitle("di/stress");


        Intent notificationIntent = new Intent(this, MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        notifications.setContentIntent(intent);


        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifications.setVibrate(new long[] { 100, 100, 100, 100, 100 });

    }


    @Override
    protected void onResume() {

        super.onResume();
        appOpen=true;

        //removing notifications if the app is opened
        mNotificationManager.cancel(0);
        if(check.isChecked() && BeacodeScanner.getState() != BeacodeScanner.State.RUNNING)
            BeacodeScanner.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        appOpen=false;
        if(!check.isChecked())
            BeacodeScanner.stop();
    }


    //checkbox checking
    public void itemClicked(View v) {
        //code to check if this checkbox is checked!
        CheckBox checkBox = (CheckBox) v;
        if (checkBox.isChecked()) {
            editor.putBoolean("listen", true);
            editor.commit();
            if(BeacodeScanner.getState()!= BeacodeScanner.State.RUNNING)
            BeacodeScanner.start();
        } else {
            editor.putBoolean("listen", false);
            editor.commit();
        }
    }


    //Called when the user touches the button for controlling which messages are played
    public void playMessage(View view) {


            if(view.getId()==R.id.help){

                helpPlaying=!helpPlaying;
                changeIcon(1,helpPlaying);
                if(helpPlaying){


                    helpMp.start();

                    if(followingMp.isPlaying()) {
                        followingMp.pause();
                        followingPlaying = false;
                        changeIcon(2, followingPlaying);
                    }
                    if(policeMp.isPlaying()) {
                        policeMp.pause();
                        policePlaying = false;
                        changeIcon(3, policePlaying);
                    }

                }else{
                    helpMp.pause();
                }
            }
        else if(view.getId()==R.id.following){
             followingPlaying=!followingPlaying;
                changeIcon(2,followingPlaying);
                if(followingPlaying){

                    followingMp.start();

                    if(helpMp.isPlaying()) {
                        helpMp.pause();
                        helpPlaying = false;
                        changeIcon(1, helpPlaying);
                    }
                    if(policeMp.isPlaying()) {
                        policeMp.pause();
                        policePlaying = false;
                        changeIcon(3, policePlaying);
                    }
                }else{

                    followingMp.pause();



                }
            }
        else if(view.getId()==R.id.police){
             policePlaying=!policePlaying;
                changeIcon(3,policePlaying);
                if(policePlaying){

                    policeMp.start();

                    if(followingMp.isPlaying()) {
                        followingMp.pause();
                        followingPlaying = false;
                        changeIcon(2, followingPlaying);
                    }
                    if(helpMp.isPlaying()) {
                        helpMp.pause();
                        helpPlaying = false;
                        changeIcon(1, helpPlaying);
                    }

                }else{

                    policeMp.pause();

                }
            }
    }


//changing icons method
    public  void changeIcon(int butNo,boolean playing){
        if(playing){
        switch (butNo){

            case 1: help.setCompoundDrawablesRelativeWithIntrinsicBounds(stop,null,null,null);
                    break;
            case 2: following.setCompoundDrawablesRelativeWithIntrinsicBounds(stop,null,null,null);
                break;
            case 3: police.setCompoundDrawablesRelativeWithIntrinsicBounds(stop,null,null,null);
                break;
        }

    }else{
            switch (butNo){

                case 1: help.setCompoundDrawablesRelativeWithIntrinsicBounds(play,null,null,null);
                    break;
                case 2: following.setCompoundDrawablesRelativeWithIntrinsicBounds(play,null,null,null);
                    break;
                case 3: police.setCompoundDrawablesRelativeWithIntrinsicBounds(play,null,null,null);
                    break;
            }
        }
    }





////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    //beacodes methods

    @Override
    public void onScannerStateChange(BeacodeScanner.State oldState, BeacodeScanner.State newState) {
    }
    @Override
    public void onPartialMessage(int messageId, int percent) {
    }
    @Override
    public void onPartialMessageCancelled(int messageId) {
    }
    @Override
    public void onMessage(int messageId, BeacodeScanner.Message
            message) {

flash=!flash;

        this.message=message.toString();
switch (this.message.charAt(0)){
    case 'c':pCounter++;                //c for contact the police
            policeCounter.setText(Integer.toString(pCounter));
            policeCounter.setTextColor(Color.parseColor("#fd4582"));
            pImage.setVisibility(View.VISIBLE);
            pImageG.setVisibility(View.GONE);
            hImageG.setVisibility(View.VISIBLE);
            hImage.setVisibility(View.GONE);
            fImageG.setVisibility(View.VISIBLE);
            fImage.setVisibility(View.GONE);
            break;
    case 'h':hCounter++;                //h for help me
        helpCounter.setText(Integer.toString(hCounter));
        helpCounter.setTextColor(Color.parseColor("#fd4582"));
        hImage.setVisibility(View.VISIBLE);
        hImageG.setVisibility(View.GONE);
        pImageG.setVisibility(View.VISIBLE);
        pImage.setVisibility(View.GONE);
        fImageG.setVisibility(View.VISIBLE);
        fImage.setVisibility(View.GONE);
        break;
    case 's':fCounter++;                //s for someone is following
        followingCounter.setText(Integer.toString(fCounter));
        followingCounter.setTextColor(Color.parseColor("#fd4582"));
        fImage.setVisibility(View.VISIBLE);
        fImageG.setVisibility(View.GONE);
        pImageG.setVisibility(View.VISIBLE);
        pImage.setVisibility(View.GONE);
        hImageG.setVisibility(View.VISIBLE);
        hImage.setVisibility(View.GONE);
        break;

}

distress.setText(this.message);
if(flash){  /// to make distress signal flash if we are recieving any
    distress.setTextColor(Color.parseColor("#fd4582"));

}else{
    distress.setTextColor(Color.parseColor("#e9ebee"));

}


        if(!appOpen) {
            notifications.setContentText(this.message);
            SystemClock.sleep(100);
            // notificationID allows you to update the notification later on.
            mNotificationManager.notify(0, notifications.build());
        }
        
    }


////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    // methods for gaining permission for using the mic
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MIC_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // permission denied
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECORD_AUDIO)) {
                    // permission denied with selecting "never ask again"
                    AlertDialog alertDialog = new
                            AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Microphone required");
                    alertDialog.setMessage("Please allow microphone permission in the Android settings (Settings -> Applications -> Di/Stress).");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    isDialogShown = false;
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    isDialogShown = true;
                }
            }
        }
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    { Manifest.permission.RECORD_AUDIO }, MIC_REQUEST_CODE);
        }
    }



}
