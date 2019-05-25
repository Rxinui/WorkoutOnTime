package s3a.iut.prototypechrono;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import s3a.iut.prototypechrono.chrono.database.TimerDb;

public class ModifyActivity extends AppCompatActivity {

    private static final int NB_MAX_EXO = 10;

    private EditText input_name, input_exo1,input_timeMinute, input_timeSecond;

    private Button btn_update;

    private TimerDb db_timer;

    private FloatingActionButton btn_updateSet, btn_rmSet;

    private LinearLayout layout_scroll;

    private ScrollView scroll_view;

    private int cpt_exo;

    private int[] list_cpt_set;

    private Map<Integer,LinkedList<RelativeLayout>> map_setLayout;

    private LinkedList<LinearLayout> list_exoLayout;

    private LinkedList<View> list_divider;

    /**
     * Listener pour supprimer un exercice.
     */

    private View.OnLongClickListener btn_rmExoListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            System.out.println(cpt_exo);
            LinkedList<RelativeLayout> listTmp = map_setLayout.get(cpt_exo);
            /* supprime toutes les series d'un exo */
            for (RelativeLayout tmp : listTmp){
                layout_scroll.removeView(tmp);
                list_cpt_set[cpt_exo]--;
            }
            listTmp.clear();
            if (cpt_exo!=0)
                removeLastExo();
            return true;
        }
    };


    /**
     * Listener pour supprimer une série.
     */

    private View.OnClickListener btn_rmSetListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /* remove set */
            if (cpt_exo == 0 && map_setLayout.get(cpt_exo).size() <= 1){
                return;
            }
            else if (map_setLayout.get(cpt_exo).size()>1){
                removeLastSet();
            }else{
                /* remove exo */
                if (list_exoLayout.size()>0){
                    removeLastSet();
                    removeLastExo();
                }
            }
        }
    };

    /**
     * Listener pour ajouter un exercice.
     */

    private View.OnLongClickListener btn_updateExoListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (cpt_exo<NB_MAX_EXO-1){
                cpt_exo++;
                map_setLayout.put(cpt_exo,new LinkedList<RelativeLayout>());
                list_divider.add(createDivider());
                layout_scroll.addView(list_divider.getLast());
                LinearLayout layout_exo = new LinearLayout(ModifyActivity.this);
                layout_exo.setOrientation(LinearLayout.HORIZONTAL);
                TextView lib_exo = new TextView(ModifyActivity.this);
                lib_exo.setText(getResources().getString(R.string.lib_nb_exo)+" "+(cpt_exo+1));
                EditText input_exo = new EditText(ModifyActivity.this);
                input_exo.setTag("exo"+cpt_exo);
                input_exo.addTextChangedListener(new ControleurEditText(input_exo));
                layout_exo.addView(lib_exo);
                layout_exo.addView(input_exo);
                applyExoParams(lib_exo,input_exo);
                layout_scroll.addView(layout_exo);
                list_exoLayout.add(layout_exo);
                btn_updateSet.callOnClick();

            }
            return true;
        }
    };

    /**
     * Listener pour ajouter une série.
     */

    private View.OnClickListener btn_updateSetListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (cpt_exo==0 && map_setLayout.get((0)).size()==1){
                list_cpt_set[cpt_exo]=2;
            }else{
                list_cpt_set[cpt_exo]++;
            }
            RelativeLayout layout_set = new RelativeLayout(ModifyActivity.this);
            TextView lib_set = new TextView(ModifyActivity.this);
            EditText input_min = new EditText(ModifyActivity.this);
            EditText input_sec = new EditText(ModifyActivity.this);
            input_min.setId(View.generateViewId());
            input_sec.setId(View.generateViewId());
            input_min.setTag("exo"+cpt_exo+"_min"+list_cpt_set[cpt_exo]);
            input_sec.setTag("exo"+cpt_exo+"_sec"+list_cpt_set[cpt_exo]);
            layout_set.addView(lib_set);
            layout_set.addView(input_min);
            layout_set.addView(input_sec);
            layout_scroll.addView(layout_set);
            map_setLayout.get((cpt_exo)).add(layout_set);
            applySetParams(lib_set,input_min,input_sec);
            lib_set.setText(getResources().getString(R.string.lib_nb_serie)+" "+list_cpt_set[cpt_exo]);
            scroll_view.fullScroll(View.FOCUS_DOWN);
        }
    };


    /**
     * Listener pour créer un timer et l'enregistrer dans la base de donnée.
     */

    private View.OnClickListener btn_updateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            db_timer.deleteSession(getIntent().getStringExtra(MainActivity.TIMER_EXTRA_NAME));
            String nom_session = input_name.getText().toString();
            if (nom_session.isEmpty()){
                afficherDialogError(getResources().getString(R.string.dialog_input_empty));
                return;
            }
            if (db_timer.hasSession(nom_session)) {
                afficherDialogError(getResources().getString(R.string.dialog_session_exist));
                return;
            }
            db_timer.insertSession(nom_session);
            int nbExercice = list_exoLayout.size();
            int[] tab_id_exercice = new int[nbExercice];
            Cursor dataExercice;
            EditText input_exo;
            /* AJOUT EXERCICES */
            for (int i=0; i<nbExercice;i++){
                input_exo = (EditText) list_exoLayout.get(i).findViewWithTag("exo" + i);
                if (TextUtils.isEmpty(input_exo.getText())){
                    db_timer.deleteSession(nom_session);
                    afficherDialogError(getResources().getString(R.string.dialog_input_empty));
                    return;
                }
                db_timer.insertExercice(input_exo.getText().toString(), nom_session);
                dataExercice = db_timer.getExercice(nom_session);
                dataExercice.moveToLast();
                tab_id_exercice[i] = dataExercice.getInt(0); // on recupere l'id de l'exercice.
            }
            /* AJOUT SERIES */
            EditText input_min, input_sec;
            int id_exercice;
            for (int i = 0; i<map_setLayout.size();i++){
                LinkedList<RelativeLayout> list_setLayout = map_setLayout.get(i);
                id_exercice = tab_id_exercice[i];
                for (int j = 0; j<list_setLayout.size();j++){
                    input_min = (EditText) list_setLayout.get(j).findViewWithTag("exo"+i+"_min"+(j+1));
                    input_sec = (EditText) list_setLayout.get(j).findViewWithTag("exo"+i+"_sec"+(j+1));
                    if (TextUtils.isEmpty(input_min.getText()))
                        input_min.setText("0");
                    if (TextUtils.isEmpty(input_sec.getText()))
                        input_sec.setText("0");
                    db_timer.insertSerie(input_min.getText().toString(),input_sec.getText().toString(),id_exercice);
                }
            }
            Intent intent_main = new Intent(ModifyActivity.this, MainActivity.class);
            startActivity(intent_main);
            finish();
        }
    };

    /**
     * Initialise les composants présents par défaut.
     */
    private void init(){
        cpt_exo = 0;
        input_name = (EditText) findViewById(R.id.input_name);
        input_name.setTag("session");
        input_name.addTextChangedListener(new ControleurEditText(input_name));
        input_name.setFocusable(true);
        input_exo1 = (EditText) findViewById(R.id.input_exo1);
        input_exo1.setTag("exo"+cpt_exo);
        input_exo1.addTextChangedListener(new ControleurEditText(input_exo1));
        input_timeMinute = (EditText) findViewById(R.id.input_timeMinute);
        input_timeMinute.setTag("exo"+cpt_exo+"_min"+1);
        input_timeSecond = (EditText) findViewById(R.id.input_timeSecond);
        input_timeSecond.setTag("exo"+cpt_exo+"_sec"+1);
        btn_update = findViewById(R.id.btn_add);
        btn_update.setOnClickListener(btn_updateListener);
        btn_updateSet = (FloatingActionButton) findViewById(R.id.btn_addSet);
        btn_updateSet.setOnClickListener(btn_updateSetListener);
        btn_updateSet.setOnLongClickListener(btn_updateExoListener);
        btn_rmSet = (FloatingActionButton) findViewById(R.id.btn_rmSet);
        btn_rmSet.setOnClickListener(btn_rmSetListener);
        btn_rmSet.setOnLongClickListener(btn_rmExoListener);
        layout_scroll = (LinearLayout) findViewById(R.id.scroll_activity);
        scroll_view = (ScrollView) findViewById(R.id.scroll_view);
        map_setLayout = new HashMap<Integer,LinkedList<RelativeLayout>>();
        map_setLayout.put(0,new LinkedList<RelativeLayout>());
        map_setLayout.get(0).add((RelativeLayout) findViewById(R.id.layout_exo1_set1));
        list_exoLayout = new LinkedList<>();
        list_exoLayout.add((LinearLayout) findViewById(R.id.layout_exo1));
        list_cpt_set = new int[NB_MAX_EXO];
        list_divider = new LinkedList<>();
        // Database
        db_timer = new TimerDb(this);
        // Back Button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        init();
        btn_update.setText(getResources().getText(R.string.btn_update_timer));
        String nom_session = getIntent().getStringExtra(MainActivity.TIMER_EXTRA_NAME);
        input_name.setText(nom_session);
        /* On recupere les exos de cette session */
        Cursor dataExercice = db_timer.getExercice(nom_session);
        dataExercice.moveToFirst();
        input_exo1.setText(dataExercice.getString(1));
        for (int i=0; !dataExercice.isAfterLast() ; i++){
            int id_exercice = dataExercice.getInt(0);
            if (i>0) {
                btn_updateExoListener.onLongClick(btn_updateSet);
                EditText input_exo = (EditText) list_exoLayout.get(i).findViewWithTag("exo" + i);
                input_exo.setText(dataExercice.getString(1));
            }
            /* On recupere les series de cette session */
            Cursor dataSerie = db_timer.getSerie(id_exercice);
            dataSerie.moveToFirst();
            for (int j=0; !dataSerie.isAfterLast() ; j++){
                if (j>0){
                    btn_updateSetListener.onClick(btn_updateSet);
                }
                EditText input_min = (EditText) map_setLayout.get(i).get(j).findViewWithTag("exo"+i+"_min"+(j+1));
                EditText input_sec = (EditText) map_setLayout.get(i).get(j).findViewWithTag("exo"+i+"_sec"+(j+1));
                input_min.setText(dataSerie.getString(1));
                input_sec.setText(dataSerie.getString(2));
                dataSerie.moveToNext();
            }dataExercice.moveToNext();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        /* ne sauvegarde rien --> effet Cancel */
        if (item.getItemId()== android.R.id.home)
            this.finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Applique des paramètres au layout d'exercice et ses composants.
     *
     * @param lib_exo
     * @param input_exo
     */
    @SuppressLint({"NewApi", "ResourceType"})
    private void applyExoParams(TextView lib_exo, EditText input_exo){
        LinearLayout.LayoutParams lib_exo_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams input_exo_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lib_exo.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        input_exo.setHint(getResources().getString(R.string.lib_add_hint));
        lib_exo.setLayoutParams(lib_exo_params);
        input_exo.setLayoutParams(input_exo_params);
        setMarginsInDp(lib_exo,10,0,10,0);
        /* FONCTIONNE PAS SUR API 19 */
//        lib_exo.setTypeface(getResources().getFont(R.font.gravity_book));
//        input_exo.setTypeface(getResources().getFont(R.font.gravity_book));
    }

    /**
     * Applique des paramètres au layout de série et ses composants.
     *
     * @param lib_set
     * @param input_min
     * @param input_sec
     */
    @SuppressLint("NewApi")
    private void applySetParams(TextView lib_set, EditText input_min, EditText input_sec){
        RelativeLayout.LayoutParams input_min_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams input_sec_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams lib_set_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        input_min_params.addRule(RelativeLayout.START_OF,input_sec.getId());
        input_sec_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lib_set_params.addRule(RelativeLayout.CENTER_VERTICAL);
        input_min.setLayoutParams(input_min_params);
        input_sec.setLayoutParams(input_sec_params);
        lib_set.setLayoutParams(lib_set_params);
        /* FONCTIONNE PAS SUR API 19 */
//        lib_set.setTypeface(getResources().getFont(R.font.gravity_book));
//        input_min.setTypeface(getResources().getFont(R.font.gravity_book));
//        input_sec.setTypeface(getResources().getFont(R.font.gravity_book));
        input_min.setInputType(InputType.TYPE_CLASS_NUMBER);
        input_sec.setInputType(InputType.TYPE_CLASS_NUMBER);
        lib_set.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        input_min.setHint("min");
        input_sec.setHint("sec");
        input_min.setEms(4);
        input_sec.setEms(4);
        InputFilter[] timerFilter = new InputFilter[]{new InputFilter.LengthFilter(2)};
        input_min.setFilters(timerFilter);
        input_sec.setFilters(timerFilter);
        setMarginsInDp(lib_set,30,0,10,0);
    }


    /**
     * Définie les marges d'un composant.
     *
     * @param view
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    private void setMarginsInDp (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,left,getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,top,getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,right,getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,bottom,getResources().getDisplayMetrics()));
            view.requestLayout();
        }
    }

    /**
     * Construit un séparateur.
     *
     * @return un séparateur.
     */
    private View createDivider(){
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#E2E2E2"));
        LinearLayout.LayoutParams divider_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,2,getResources().getDisplayMetrics()));
        divider.setLayoutParams(divider_params);
        setMarginsInDp(divider,5,10,5,10);
        return divider;
    }

    /**
     * Supprime le layout et ses composants de la dernière série d'un exercice.
     */
    private void removeLastSet(){
        map_setLayout.get((cpt_exo)).getLast().removeAllViewsInLayout();
        layout_scroll.removeView(map_setLayout.get((cpt_exo)).getLast());
        map_setLayout.get((cpt_exo)).removeLast();
        list_cpt_set[cpt_exo]--;
    }

    /**
     * Supprime le layout et ses composants d'un exercice.
     */
    private void removeLastExo(){
        layout_scroll.removeView(list_divider.getLast());
        layout_scroll.removeView(list_exoLayout.getLast());
        list_divider.removeLast();
        list_exoLayout.removeLast();
        cpt_exo--;
    }

    /**
     * Affiche une boîte de dialogue d'erreur.
     *
     * @param message à afficher.
     */
    private void afficherDialogError(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(ModifyActivity.this);
        builder.setMessage(message);
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