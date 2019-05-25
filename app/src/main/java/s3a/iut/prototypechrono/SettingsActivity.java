package s3a.iut.prototypechrono;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    /**
     * Map contenant comme clé l'identifiant d'un radiobouton associés à un son de timer et comme valeur le raw du son.
     */
    static final Map<Integer,Integer> map_rawSound = new HashMap<Integer, Integer>(){
        {
            put(R.id.rb_sound1,R.raw.sound1);
            put(R.id.rb_sound2,R.raw.sound2);
            put(R.id.rb_sound3,R.raw.sound3);
            put(R.id.rb_sound4,R.raw.sound4);
        }
    };

    /**
     * Nom du fichier persistant des préférences de l'activité SettingsActivity.
     */
    static final String PREFERENCES_NAME = "SETTINGS_PREFERENCES";

    /**
     * Tableau contenant les clés du fichier persistant des préférences de l'activité SettingsActivity.
     */
    static final String[] PREFERENCES_KEY = {"SOUND_ENABLED","SOUND_SELECTED","LIBRE_ENABLED"};

    private SharedPreferences preferences_settings;

    private RadioGroup rg_sound;

    private RadioButton rb_sound1;

    private Switch sw_enable_sound, sw_timer_libre;

    private Button btn_save;

    private TextView lib_timerSound;

    /**
     * Listener du bouton de sauvegarde.
     */
    private View.OnClickListener btn_saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((RadioButton) findViewById(rg_sound.getCheckedRadioButtonId())).setChecked(true);
            Intent intent_main = new Intent();
            boolean timerLibre;
            SharedPreferences.Editor editeurPreferences = preferences_settings.edit();
            if (sw_enable_sound.isChecked()){
                int soundSelected = map_rawSound.get(rg_sound.getCheckedRadioButtonId());
                intent_main.putExtra(MainActivity.SOUND_EXTRA_NAME,soundSelected);
                editeurPreferences.putBoolean(PREFERENCES_KEY[0],true);
                editeurPreferences.putInt(PREFERENCES_KEY[1],soundSelected);
            }else{
                editeurPreferences.putBoolean(PREFERENCES_KEY[0],false);
                intent_main.putExtra(MainActivity.SOUND_EXTRA_NAME,0);
            }
            timerLibre = (sw_timer_libre.isChecked() ? true : false);
            intent_main.putExtra(MainActivity.LIBRE_EXTRA_NAME,timerLibre);
            editeurPreferences.putBoolean(PREFERENCES_KEY[2],timerLibre);
            editeurPreferences.apply();
            setResult(MainActivity.SETTINGS_CODE_RESULT,intent_main);
            finish();
        }
    };

    /**
     * Listener des composants Switch.
     */
    private Switch.OnCheckedChangeListener sw_enable_soundListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked){
                rg_sound.setVisibility(View.VISIBLE);
                lib_timerSound.setVisibility(View.VISIBLE);
            }else{
                rg_sound.setVisibility(View.INVISIBLE);
                lib_timerSound.setVisibility(View.INVISIBLE);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        rg_sound = (RadioGroup) findViewById(R.id.rg_sound);
        rb_sound1 = (RadioButton) findViewById(R.id.rb_sound1);
        btn_save = (Button) findViewById(R.id.btn_save);
        sw_enable_sound = (Switch) findViewById(R.id.sw_enable_sound);
        sw_enable_sound.setOnCheckedChangeListener(sw_enable_soundListener);
        sw_timer_libre = (Switch) findViewById(R.id.sw_timer_libre);
        lib_timerSound = (TextView) findViewById(R.id.lib_settings_sounds);
        btn_save.setOnClickListener(btn_saveListener);
        preferences_settings = getSharedPreferences(PREFERENCES_NAME,MODE_PRIVATE);
        rb_sound1.setChecked(true);
        restaurerPreferences();
        // Back Button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Restaure l'état des composants enregistré dans le fichier persistant.
     */
    private void restaurerPreferences(){
        if (preferences_settings.getBoolean(PREFERENCES_KEY[0],false)){
            rb_sound1.setChecked(true);
            rg_sound.clearCheck();
            switch(preferences_settings.getInt(PREFERENCES_KEY[1],0)){
                case R.raw.sound1:
                    rg_sound.check(R.id.rb_sound1);
                    break;
                case R.raw.sound2:
                    rg_sound.check(R.id.rb_sound2);
                    break;
                case R.raw.sound3:
                    rg_sound.check(R.id.rb_sound3);
                    break;
                case R.raw.sound4:
                    rg_sound.check(R.id.rb_sound4);
                    break;
            }
        }else{
            sw_enable_sound.setChecked(false);
            sw_enable_soundListener.onCheckedChanged(sw_enable_sound,false);
        }if (preferences_settings.getBoolean(PREFERENCES_KEY[2],false)){
            sw_timer_libre.setChecked(true);
        }else{
            sw_timer_libre.setChecked(false);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        /* ne sauvegarde rien --> effet Cancel */
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                break;
            case R.id.menu_about:
                afficherItemAbout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Affiche une boîte de dialogue du menu tem About/A propos.
     */
    private void afficherItemAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_about);
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

}
