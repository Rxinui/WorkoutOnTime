package s3a.iut.prototypechrono;

import android.app.Activity;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ModeleTimer extends CountDownTimer {

    /**
     * Représente une seconde en milliseconde.
     */
    public static final int ONE_SECOND = 1000;

    private static final int TIME_INTERVAL = ONE_SECOND;

    /**
     * Compteur de la méthode OnFinish().
     */
    public static int CPT_ONFINISH = 0;

    private long time_left;

    private MainActivity vue;

    private boolean isRunning;

    /**
     * Construit le modèle d'un timer.
     *
     * @param vue activité auquel le timer est associé.
     * @param minInFutre minute d'un timer.
     * @param secInFutre seconde d'un timer.
     */
    public ModeleTimer(MainActivity vue ,long minInFutre,long secInFutre){
        super(minInFutre*60000+secInFutre*1000,TIME_INTERVAL);
        this.time_left = minInFutre*60000+secInFutre*1000;
        this.vue = vue;
    }

    /**
     * Construit le modèle d'un timer.
     * @param vue activité auquel le timer est associé.
     * @param time_left temps en milliseconde d'un timer.
     */
    public ModeleTimer(MainActivity vue, long time_left){
        super(time_left,TIME_INTERVAL);
        this.vue = vue;
        this.time_left = time_left;
    }

    /**
     * Actualise le timer avec le temps restant.
     *
     * @param millisUntilFinished temps restant en milliseconde.
     */
    @Override
    public void onTick(long millisUntilFinished) {
        this.time_left = millisUntilFinished;
        this.vue.getBtn_reset().setEnabled(false);
        this.update();
    }

    /**
     * Vérifie si le timer est en cours d'exécution.
     *
     * @return vrai si en cours d'exécution faux sinon.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Indique si le timer est en cours d'exécution
     * @param running
     */
    public void setRunning(boolean running) {
        isRunning = running;
    }

    /**
     * Incrémente le compteur de série, et d'exercice si nécessaire lorsque le timer se termine.
     * Charge le timer suivant si existant. Met à jour la vue.
     * Joue un son lorsque le termine se finit.
     */
    @Override
    public void onFinish() {
        this.isRunning = false;
        if (!MainActivity.onModeLibre) {
            LinkedList<LinkedList<ModeleTimer>> list_timerSelected = this.vue.getList_timerSelected();
            //supprime l'exo qui est premier dans la file quand il lui reste une serie a faire
            if (list_timerSelected.getFirst().size() == 1) {
                CPT_ONFINISH = 0;
                list_timerSelected.removeFirst();
                this.vue.getList_nbSerie().removeFirst();
                this.vue.getLib_cptSerie().setText("" + (ModeleTimer.CPT_ONFINISH));
                this.vue.getLib_cptExercice().setText("" + (this.vue.getNbExo() - this.vue.getList_nbSerie().size()));
                LinkedList list_nomExo = this.vue.getList_nomExo();
                if (!list_nomExo.isEmpty()) {
                    this.vue.getLib_exerciceSession().setText(": " + list_nomExo.getFirst());
                    list_nomExo.removeFirst();
                }
            }
            //supprime la serie de l'exo qui est premier dans la file
            else {
                list_timerSelected.getFirst().removeFirst();
            }
            // on défini un temps si il en reste au moins 1 dans une session sinon on desactive le btn start.
            if (list_timerSelected.size() > 0) {
                this.setTimeLeft(list_timerSelected.getFirst().getFirst().getTimeLeft());
            } else {
                this.vue.getBtn_start().setVisibility(View.INVISIBLE);
            }
        }else{
            this.setTimeLeft(this.vue.getLibreTimeLeft());
        }
        this.vue.getBtn_start().setText("Start");
        this.vue.setColorBouton(false);
        this.vue.getBtn_reset().setEnabled(true);
        this.vue.playSound();
        this.update();
    }

    /**
     * Met à jour l'affichage du timer.
     */
    public void update(){
        this.vue.getLib_timer().setText(this.toString());
    }

    /**
     * Retourne le temps restant.
     *
     * @return temps en milliseconde
     */
    public long getTimeLeft(){
        return this.time_left;
    }

    /**
     * Définie le temps restant.
     *
     * @param time_left temps en milliseconde
     */
    public void setTimeLeft(long time_left){
        this.time_left = time_left;
    }

    /**
     * Retourne les minutes restantes.
     *
     * @return les minutes.
     */
    public long getMin(){
        return (this.time_left/ONE_SECOND)/60 ;
    }

    /**
     * Retourne les secondes restantes.
     *
     * @return les secondes.
     */
    public long getSec(){
        return (this.time_left/ONE_SECOND)%60;
    }

    /**
     * Affiche le timer sous la forme MM:SS.
     *
     * @return une chaine textuelle du timer.
     */
    public String toString(){
        return String.format(Locale.getDefault(),"%02d:%02d",this.getMin(),this.getSec());
    }

}
