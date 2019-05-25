package s3a.iut.prototypechrono;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Créer la vue contenant le picker minute et seconde pour le mode Timer Libre.
 */
public class TimerPickerActivity extends AppCompatActivity {

    private TimerPicker picker_min, picker_sec;

    private Button btn_saveTimerPicker;

    /**
     * Listener du bouton sauvegarder.
     */
    private View.OnClickListener btn_saveTimerPickerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent_main = new Intent(TimerPickerActivity.this, MainActivity.class);
            intent_main.putExtra(MainActivity.LIBRE_EXTRA_NAME,picker_min.getValue()*60000+picker_sec.getValue()*1000);
            setResult(MainActivity.LIBRE_CODE_RESULT,intent_main);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_picker);
        picker_min = (TimerPicker) findViewById(R.id.picker_min);
        picker_sec = (TimerPicker) findViewById(R.id.picker_sec);
        btn_saveTimerPicker = (Button) findViewById(R.id.btn_saveTimerPicker);
        btn_saveTimerPicker.setOnClickListener(btn_saveTimerPickerListener);
    }
}
