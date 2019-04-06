package ca.bischke.apps.filevault;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity
{
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Sets Activity Layout
        setContentView(R.layout.activity_settings);

        // Adds the Toolbar to the Layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Displays Back Button in Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle(R.string.settings);

        // Setup Shared Preferences
        preferences = new Preferences(this);

        Switch switchExternalIntents = findViewById(R.id.switch_external_intents);
        switchExternalIntents.setChecked(preferences.getBoolean(getString(R.string.preference_external_intents)));

        switchExternalIntents.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                preferences.setBoolean(getString(R.string.preference_external_intents), b);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        // Handles Toolbar back button click event
        onBackPressed();
        return true;
    }
}
