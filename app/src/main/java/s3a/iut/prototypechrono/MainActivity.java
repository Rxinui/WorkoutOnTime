package s3a.iut.prototypechrono;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.LinkedList;
import s3a.iut.prototypechrono.chrono.database.TimerDb;

import static s3a.iut.prototypechrono.SettingsActivity.*;

public class MainActivity extends AppCompatActivity {

    /**
     * Code d'activityForResult pour les timers.
     */
    static final int TIMER_CODE_RESULT = 10;

    /**
     * Code d'extra pour les timers.
     */
    static final String TIMER_EXTRA_NAME = "nom_session";

    /**
     * Son du timer chargé.
     */
    static int TIMER_SOUND = 0;

    /**
     * Code d'activityForResult pour l'activité SettingsActivity.
     */
    static int SETTINGS_CODE_RESULT = 20;


    /**
     * Code d'extra pour le son choisi.
     */
    static final String SOUND_EXTRA_NAME = "soundSelected";


    /**
     * Code d'extra pour le mode Timer Libre.
     */
    static final String LIBRE_EXTRA_NAME = "timerLibre";

    /**
     * Code d'activityForResult pour le mode Timer Libre.
     */
    static final int LIBRE_CODE_RESULT = 30;

    /**
     * Mode Timer Libre activé ou non.
     */
    static boolean onModeLibre;

    static SoundPool soundPool;

    private Button btn_start, btn_reset;

    private TextView lib_timer, lib_session, lib_exerciceSession, lib_cptSerie, lib_cptExercice;

    private ModeleTimer modele;

    private Intent intent_add, intent_settings;

    private FloatingActionButton btn_showTimerList;

    private TimerDb db_timer;

    private int nbExo, libreTimeLeft;

    private ActionMenuItemView menu_add;

    // LinkedList pour utiliser getFirst()/removeFirst()
    private LinkedList<String> list_nomExo;

    private LinkedList<Integer> list_nbSerie;

    private LinkedList<LinkedList<ModeleTimer>> list_timerSelected;

    /**
     * Listener du bouton Start.
     */
    private View.OnClickListener btn_startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (modele.isRunning()){
                pauseTimer();
            }else{
                startTimer();
            }
        }
    };

    /**
     * Listener du bouton qui affiche TimerListActivity.
     */
    private View.OnClickListener btn_showListTimerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent_timerList = new Intent(MainActivity.this,TimerListActivity.class);
            startActivityForResult(intent_timerList,TIMER_CODE_RESULT);
        }
    };

    /**
     * Listener du bouton qui affiche TimerPickerActivity.
     */
    private View.OnClickListener btn_timerLibreListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent_timerLibre = new Intent(MainActivity.this,TimerPickerActivity.class);
            startActivityForResult(intent_timerLibre,LIBRE_CODE_RESULT);
        }
    };

    /**
     * Listener du bouton reset.
     */
    private View.OnClickListener btn_resetListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!modele.isRunning()){
               resetTimer();
            }
        }
    };


    /**
     * Récupère soit les réglages du timer, soit le timer choisi.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode != 0 || data!=null) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == TIMER_CODE_RESULT) {
                String nom_session = data.getStringExtra(TIMER_EXTRA_NAME);
                lib_session.setText(nom_session);
                Toast.makeText(MainActivity.this, nom_session + " timer.", Toast.LENGTH_LONG).show();
                list_timerSelected = new LinkedList<LinkedList<ModeleTimer>>();
                list_nomExo = new LinkedList<>();
                /* On recupere les exos de cette session */
                Cursor dataExercice = db_timer.getExercice(nom_session);
                nbExo = dataExercice.getCount();
                list_nbSerie = new LinkedList<>();
                dataExercice.moveToFirst();
                for (int i = 0; !dataExercice.isAfterLast(); i++) {
                    int id_exercice = dataExercice.getInt(0);
                    list_nomExo.add(dataExercice.getString(1));
                    /* On recupere les series de cette session */
                    Cursor dataSerie = db_timer.getSerie(id_exercice);
                    list_nbSerie.add(dataSerie.getCount());
                    dataSerie.moveToFirst();
                    LinkedList<ModeleTimer> list_timerTmp = new LinkedList<>();
                    while (!dataSerie.isAfterLast()) {
                        list_timerTmp.add(new ModeleTimer(this, dataSerie.getInt(1), dataSerie.getInt(2)));
                        dataSerie.moveToNext();
                    }
                    list_timerSelected.add(list_timerTmp);
                    dataExercice.moveToNext();
                }
                modele.setTimeLeft(list_timerSelected.getFirst().getFirst().getTimeLeft());
                modele.update();
                ModeleTimer.CPT_ONFINISH = 0;
                lib_exerciceSession.setText(": "+list_nomExo.getFirst());
                list_nomExo.removeFirst();
                lib_cptExercice.setText("0");
                lib_cptSerie.setText("0");
                btn_start.setVisibility(View.VISIBLE);
            }else if (requestCode == SETTINGS_CODE_RESULT){
                int soundSelected = data.getIntExtra(SOUND_EXTRA_NAME,0);
                if (soundSelected==0){
                    TIMER_SOUND = 0;
                }else{
                    TIMER_SOUND = soundPool.load(this,soundSelected,1);
                }
                onModeLibre = data.getBooleanExtra(LIBRE_EXTRA_NAME,false);
                menu_add = (ActionMenuItemView) findViewById(R.id.menu_add);
                if (onModeLibre){
                    btn_showTimerList.setOnClickListener(btn_timerLibreListener);
                    btn_showTimerList.setImageDrawable(getResources().getDrawable(R.drawable.ic_timer_white_96dp));
                    menu_add.setVisibility(View.INVISIBLE);
                }else{
                    btn_showTimerList.setOnClickListener(btn_showListTimerListener);
                    btn_showTimerList.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_white_96dp));
                    menu_add.setVisibility(View.VISIBLE);
                    libreTimeLeft = 0;
                }modele.setTimeLeft(0);
                modele.update();
                lib_session.setText("");
                lib_exerciceSession.setText("");
                lib_cptExercice.setText("0");
                lib_cptSerie.setText("0");
            }else if (requestCode == LIBRE_CODE_RESULT){
                libreTimeLeft = data.getIntExtra(LIBRE_EXTRA_NAME,0);
                modele.setTimeLeft(libreTimeLeft);
                modele.update();
                lib_session.setText("");
                lib_exerciceSession.setText("");
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent_add = new Intent(this, AddActivity.class);
        intent_settings = new Intent(this, SettingsActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder().setMaxStreams(4).build();
        } else {
            soundPool = new SoundPool(4, AudioManager.STREAM_ALARM, 0);
        }
        SharedPreferences preferences_settings = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        if (preferences_settings.getBoolean(SettingsActivity.PREFERENCES_KEY[0], false)) {
            int soundSelected = preferences_settings.getInt(SettingsActivity.PREFERENCES_KEY[1], R.raw.sound1);
            TIMER_SOUND = soundPool.load(this, soundSelected, 1);
        }
        // Programmation Bouton
        btn_start = (Button) findViewById(R.id.btn_start);
        lib_timer = (TextView) findViewById(R.id.lib_timer);
        lib_session = (TextView) findViewById(R.id.lib_session);
        lib_cptSerie = (TextView) findViewById(R.id.lib_cpt_serie);
        lib_cptExercice = (TextView) findViewById(R.id.lib_cpt_exercice);
        lib_exerciceSession = (TextView) findViewById(R.id.lib_exerciceSession);
        btn_start.setOnClickListener(btn_startListener);
        btn_reset = (Button) findViewById(R.id.btn_reset);
        btn_reset.setOnClickListener(btn_resetListener);
        btn_showTimerList = (FloatingActionButton) findViewById(R.id.btn_showTimerList);
        btn_showTimerList.setOnClickListener(btn_showListTimerListener);
        onModeLibre = false;
        db_timer = new TimerDb(this);
        btn_reset.setEnabled(false);
        modele = new ModeleTimer(this, 0, 0);
        modele.update();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }


    /**
     * Joue le son du timer.
     */
    void playSound(){
        soundPool.play(TIMER_SOUND,1,1,0,0,1);
    }

    /**
     * Déclenche le timer.
     */
    @SuppressLint("ResourceAsColor")
    private void startTimer(){
        modele = new ModeleTimer(this,modele.getTimeLeft());
        if (modele.getTimeLeft()>0){
            modele.start();
            modele.setRunning(true);
            btn_start.setText("Pause");
            setColorBouton(true);
            ModeleTimer.CPT_ONFINISH++;
            lib_cptSerie.setText(""+(ModeleTimer.CPT_ONFINISH));
        }
    }

    /**
     * Met le timer en pause.
     */
    @SuppressLint("ResourceAsColor")
    private void pauseTimer(){
        modele.cancel();
        modele.setRunning(false);
        modele.setTimeLeft(modele.getTimeLeft());
        btn_start.setText("Start");
        setColorBouton(false);
        btn_reset.setEnabled(true);
        ModeleTimer.CPT_ONFINISH--;
    }

    /**
     * Réinitialise le timer.
     */
    private void resetTimer(){
        if (!onModeLibre && !TextUtils.isEmpty(lib_session.getText())){
            list_timerSelected = new LinkedList<LinkedList<ModeleTimer>>();
            Cursor dataExercice = db_timer.getExercice(lib_session.getText().toString());
            nbExo = dataExercice.getCount();
            list_nbSerie = new LinkedList<>();
            list_nomExo.clear();
            dataExercice.moveToFirst();
            for (int i =0; !dataExercice.isAfterLast(); i++) {
                int id_exercice = dataExercice.getInt(0);
                list_nomExo.add(dataExercice.getString(1));
                /* On recupere les series de cette session */
                Cursor dataSerie = db_timer.getSerie(id_exercice);
                list_nbSerie.add(dataSerie.getCount());
                dataSerie.moveToFirst();
                LinkedList<ModeleTimer> list_timerTmp = new LinkedList<>();
                while (!dataSerie.isAfterLast()) {
                    list_timerTmp.add(new ModeleTimer(this,dataSerie.getInt(1),dataSerie.getInt(2)));
                    dataSerie.moveToNext();
                }
                list_timerSelected.add(list_timerTmp);
                dataExercice.moveToNext();
            }modele.setTimeLeft(list_timerSelected.getFirst().getFirst().getTimeLeft());
            lib_exerciceSession.setText(": "+list_nomExo.getFirst());
            list_nomExo.removeFirst();
        }else{
         modele.setTimeLeft(libreTimeLeft);
        }
        modele.update();
        ModeleTimer.CPT_ONFINISH=0;
        lib_cptExercice.setText("0");
        lib_cptSerie.setText("0");
        btn_start.setVisibility(View.VISIBLE);
    }

    /**
     * Modifie la couleur background d'un bouton.
     *
     * @param isPressed true si le bouton est pressé false sinon
     */
    void setColorBouton(boolean isPressed){
        if (isPressed) {
            btn_start.setBackgroundColor(getResources().getColor(R.color.btnBackgroundPause));
        } else {
            btn_start.setBackgroundColor(getResources().getColor(R.color.btnBackgroundPrimary));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_add:
                startActivity(intent_add);
                return true;
            case R.id.menu_settings:
                startActivityForResult(intent_settings,SETTINGS_CODE_RESULT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Renvoie le nombre d'exercice.
     *
     * @return le nombre d'exercice.
     */
    public int getNbExo() {
        return nbExo;
    }

    /**
     * Renvoie la liste du nombre de série.
     *
     * @return liste du nombre de série.
     */
    public LinkedList<Integer> getList_nbSerie() {
        return list_nbSerie;
    }

    /**
     * Renvoie le TextView qui compte les séries.
     *
     * @return TextView qui compte les séries.
     */
    public TextView getLib_cptSerie() {
        return lib_cptSerie;
    }

    /**
     * Renvoie le TextView qui compte les exercices.
     *
     * @return TextView qui compte les exercices.
     */
    public TextView getLib_cptExercice() {
        return lib_cptExercice;
    }

    /**
     * Renvoie la liste du timer choisi.
     *
     * @return la liste du timer choisi.
     */
    public LinkedList<LinkedList<ModeleTimer>> getList_timerSelected(){
        return list_timerSelected;
    }

    /**
     * Renvoie le bouton start.
     *
     * @return le bouton start.
     */
    public Button getBtn_start() {
        return btn_start;
    }

    /**
     * Renvoie le bouton reset.
     *
     * @return le bouton reset.
     */
    public Button getBtn_reset() {
        return btn_reset;
    }

    /**
     * Renvoie le TextView du timer.
     *
     * @return TextView du timer.
     */
    public TextView getLib_timer() {
        return lib_timer;
    }

    /**
     * Retourne la liste des noms des exercices.
     *
     * @return la liste des noms des exercices.
     */
    public LinkedList<String> getList_nomExo() {
        return list_nomExo;
    }

    /**
     * Retourne le TextView qui affiche le nom de l'exercice en cours.
     *
     * @return TextView qui affiche le nom de l'exercice en cours
     */
    public TextView getLib_exerciceSession() {
        return lib_exerciceSession;
    }

    /**
     * Retourne le temps restant du mode Timer Libre.
     *
     * @return le temps restant du mode Timer Libre.
     */
    public int getLibreTimeLeft(){
        return libreTimeLeft;
    }
}
