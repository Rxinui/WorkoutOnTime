package s3a.iut.prototypechrono;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.ArrayList;

import s3a.iut.prototypechrono.chrono.database.TimerDb;

public class TimerListActivity extends AppCompatActivity {

    private TimerDb db_timer;

    private ListView liste_timer;

    private TimerAdapter adapter;

    /**
     * Listener pour modifier un timer existant.
     */
    private AdapterView.OnItemLongClickListener liste_onLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent_modify = new Intent(TimerListActivity.this, ModifyActivity.class);
            String nom_session = liste_timer.getAdapter().getItem(position).toString();
            nom_session = nom_session.split("/")[0];
            intent_modify.putExtra(MainActivity.TIMER_EXTRA_NAME,nom_session);
            finish();
            startActivity(intent_modify);
            return true;
        }

    };

    /**
     * Listener pour séléctionner le timer à lancer.
     */
    private AdapterView.OnItemClickListener liste_onClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent_main = new Intent();
            String nom_session = liste_timer.getAdapter().getItem(position).toString();
            intent_main.putExtra(MainActivity.TIMER_EXTRA_NAME,nom_session.split("/")[0]);
            setResult(MainActivity.TIMER_CODE_RESULT,intent_main);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_list);
        liste_timer = findViewById(R.id.liste_timer);
        liste_timer.setOnItemClickListener(liste_onClickListener);
        liste_timer.setOnItemLongClickListener(liste_onLongClickListener);
        db_timer = new TimerDb(this);
        fillListView();
        // back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Remplie la ListView par les timers présents dans la base de donnée.
     */
    private void fillListView() {
        ArrayList<String> liste_data = new ArrayList<>();
        String nom_session = "";
        int id_exercice, nb_exercice, minTotal, secTotal;
        Cursor data = db_timer.getSession();
        data.moveToFirst();
        while (!data.isAfterLast()) {
            nom_session = data.getString(0);
            minTotal = 0;
            secTotal = 0;
            Cursor dataExercice = db_timer.getExercice(nom_session);
            dataExercice.moveToFirst();
            while (!dataExercice.isAfterLast()) {
                id_exercice = dataExercice.getInt(0);
                Cursor dataSerie = db_timer.getSerie(id_exercice);
                dataSerie.moveToFirst();
                while (!dataSerie.isAfterLast()) {
                    minTotal+=dataSerie.getInt(1);
                    secTotal+=dataSerie.getInt(2);
                    dataSerie.moveToNext();
                }
                dataExercice.moveToNext();
            }
            nb_exercice = dataExercice.getCount();
            liste_data.add(nom_session+"/"+getResources().getString(R.string.lib_timerList_exo)+" "+nb_exercice+"/"+getResources().getString(R.string.lib_timerList_duree)+" "+secToMin(minTotal,secTotal));
            data.moveToNext();
        }
        adapter = new TimerAdapter(this, R.layout.layout_liste, liste_data);
        liste_timer.setAdapter(adapter);
    }

    /**
     * Calcule la durée total d'un timer en MM:SS.
     *
     * @param min
     * @param sec
     * @return une chaine textuelle MM:SS
     */
    private String secToMin(int min, int sec){
        String ch="";
        while (sec>60){
            min++;
            sec-=60;
        }
        if (min>0) ch+=min+"m";
        ch+=sec+"s";
        return ch;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_timerlist,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        /* ne sauvegarde rien --> effet Cancel */
        switch(item.getItemId()){
            case android.R.id.home:
                this.finish();
                return super.onOptionsItemSelected(item);
            case R.id.menu_remove:
                deleteItemSelected();
                break;
            default:

        }return super.onOptionsItemSelected(item);
    }

    /**
     * Supprime les timers séléctionnés de la base de donnée.
     */
    private void deleteItemSelected(){
        for (int i = 0; i<liste_timer.getCount();i++){
            CheckBox cb = (CheckBox) liste_timer.findViewWithTag("checkbox"+i);
            if (cb.isChecked()){
                String nom_session = liste_timer.getAdapter().getItem(i).toString();
                nom_session = nom_session.split("/")[0];
                adapter.remove(nom_session);
                db_timer.deleteSession(nom_session);
            }
        }adapter.notifyDataSetChanged();
        finish();
        startActivity(getIntent());
    }



}
