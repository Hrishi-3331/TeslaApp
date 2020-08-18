package tesla.hrishi3331studio.vnit.teslasmartcar;

import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private int state;
    private TextView temperature;
    private Button ignition;
    private DatabaseReference mRef;
    private AlertDialog dialog;
    private TextToSpeech toSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temperature = (TextView)findViewById(R.id.car_temp);
        ignition = (Button) findViewById(R.id.car_ignition);

        mRef = FirebaseDatabase.getInstance().getReference().child("Dashboard");

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Accident Alert!").setMessage("An accident has been suspected. Please contact the driver and check his location.");
        dialog = builder.create();
    }


    private void ConvertToSpeech(final String Address){
        Log.i("LD", "Converting to speech");
        toSpeech = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    toSpeech.setLanguage(Locale.getDefault());
                    toSpeech.speak(Address, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        mRef.child("temperature").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null){
                    temperature.setText(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRef.child("ignition").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() !=  null){
                    state = Integer.valueOf(dataSnapshot.getValue().toString());
                }
                else {
                    state = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mRef.child("acc").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() !=  null){
                    if(Integer.valueOf(dataSnapshot.getValue().toString()) == 1 ) {
                        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                        try {
                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                            r.play();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.show();
                        mRef.child("acc").setValue(0);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void setIgnition(View view){
        if (state == 0){
            mRef.child("ignition").setValue(1);
            ignition.setText("Switch OFF");
            AudioManager audiomanage = (AudioManager)getSystemService(MainActivity.AUDIO_SERVICE);
            try {
                audiomanage.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }catch (Exception e){
                e.printStackTrace();
            }
            ConvertToSpeech("Driving Mode OFF");
        }else {
            mRef.child("ignition").setValue(0);
            ignition.setText("Switch ON");
            AudioManager audiomanage = (AudioManager)getSystemService(MainActivity.AUDIO_SERVICE);
            try {
                audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }catch (Exception e){
                e.printStackTrace();
            }
            ConvertToSpeech("Driving Mode ON");

        }

    }

    public void ViewMap(View view){
        Intent intent = new Intent(MainActivity.this, Map.class);
        startActivity(intent);
    }
}
