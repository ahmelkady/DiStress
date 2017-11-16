package ahmed_elkady.projecand;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.beacodes.BeacodeScanner;
import java.util.HashMap;

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


    //beacodes variables for mic

    private static final int MIC_REQUEST_CODE = 1;
    private static boolean isDialogShown = false;
    // private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    findViewById(R.id.send).setVisibility(View.GONE);
                    return true;
                case R.id.navigation_dashboard:
                    findViewById(R.id.send).setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_notifications:
                    findViewById(R.id.send).setVisibility(View.GONE);
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


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


    /** Called when the user touches the button */
    public void playMessage(View view) {


            if(view.getId()==R.id.help){

               helpPlaying=!helpPlaying;
                changeIcon(1,helpPlaying);
                if(helpPlaying){


                            helpMp.start();


                }else{
                    helpMp.pause();
                }
            }
        else if(view.getId()==R.id.following){
             followingPlaying=!followingPlaying;
                changeIcon(2,followingPlaying);
                if(followingPlaying){


                    followingMp.start();


                }else{
                    followingMp.pause();
                }
            }
        else if(view.getId()==R.id.police){
             policePlaying=!policePlaying;
                changeIcon(3,policePlaying);
                if(policePlaying){

                    policeMp.start();

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
            default:return;
        }

    }else{
            switch (butNo){

                case 1: help.setCompoundDrawablesRelativeWithIntrinsicBounds(play,null,null,null);
                    break;
                case 2: following.setCompoundDrawablesRelativeWithIntrinsicBounds(play,null,null,null);
                    break;
                case 3: police.setCompoundDrawablesRelativeWithIntrinsicBounds(play,null,null,null);
                    break;
                default:return;
            }
        }
    }

    //beacodes methods

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
    }

}
