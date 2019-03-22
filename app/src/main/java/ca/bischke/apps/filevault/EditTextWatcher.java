package ca.bischke.apps.filevault;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class EditTextWatcher implements TextWatcher
{
    private EditText editText;

    public EditTextWatcher(EditText editText)
    {
        this.editText = editText;
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
    {
        if (editText.getError() != null)
        {
            editText.setError(null);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

    @Override
    public void afterTextChanged(Editable editable) { }
}
