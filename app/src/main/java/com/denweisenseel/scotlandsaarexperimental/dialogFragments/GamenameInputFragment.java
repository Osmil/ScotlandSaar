package com.denweisenseel.scotlandsaarexperimental.dialogFragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.denweisenseel.scotlandsaarexperimental.R;

/**
 * Created by RasSv on 19.09.2017.
 */
public class GamenameInputFragment extends DialogFragment {

    private static final String TAG = "GameNameInput";
    OnGamenameInputListener inputListener;

    public static GamenameInputFragment newInstance() {
        GamenameInputFragment f = new GamenameInputFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.gamename_input, container, false);

        final EditText gamenameInput = v.findViewById(R.id.gamenameInput_edittext_input);

        gamenameInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                switch (i) {
                    case EditorInfo.IME_ACTION_DONE:
                        if (!gamenameInput.getText().toString().matches("[a-zA-Z]+")) {
                            Toast.makeText(getActivity(), "Gamename darf nur aus Buchstaben bestehen.", Toast.LENGTH_LONG).show();
                            Log.v("TEST", "TESTGamename");
                        }
                        else
                        {
                            inputListener.onInput(gamenameInput.getText().toString());
                        }
                        break;
                }
                return false;
            }
        });

        return v;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        try
        {
            inputListener = (OnGamenameInputListener) activity;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }



    }

    public interface OnGamenameInputListener

    {
        void onInput(String string);
    }

}
