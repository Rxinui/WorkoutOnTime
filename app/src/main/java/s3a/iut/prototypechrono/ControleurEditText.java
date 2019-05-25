package s3a.iut.prototypechrono;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

public class ControleurEditText implements TextWatcher {

    private EditText input;

    public ControleurEditText(EditText input){
        this.input = input;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (TextUtils.isEmpty(this.input.getText())){
            this.input.setError("Input empty");
        }else {
            this.input.setError(null);
        }
    }
}
