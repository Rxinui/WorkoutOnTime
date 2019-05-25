package s3a.iut.prototypechrono;

import android.widget.NumberPicker;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)//For backward-compability

/**
 * Créer un Picker adapté au modèle timer.
 */
public class TimerPicker extends NumberPicker {

    public TimerPicker(Context context) {
        super(context);
    }

    public TimerPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        processAttributeSet(attrs);
    }

    public TimerPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        processAttributeSet(attrs);
    }

    /**
     * Définie les attributs 'min' et 'max' dans les balises XML du composant.
     *
     * @param attrs
     */
    private void processAttributeSet(AttributeSet attrs) {
        this.setMinValue(attrs.getAttributeIntValue(null, "min", 0));
        this.setMaxValue(attrs.getAttributeIntValue(null, "max", 0));
    }
}
