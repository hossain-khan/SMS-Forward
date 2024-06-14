package com.enixcoda.smsforward;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions(new String[] {
                Manifest.permission.SEND_SMS,
                Manifest.permission.INTERNET
        }, 0);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SharedPreferences sharedPreferences;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            updateValues(R.string.key_target_sms, R.string.target_summary_sms);

            // Preview telegram values
            updateValues(R.string.key_target_telegram, R.string.key_target_telegram_summary);

            // Preview Rocket Chat values
            updateValues(R.string.key_rocket_chat_base_url, R.string.key_rocket_chat_base_url_summary);
            updateValues(R.string.key_rocket_chat_user_id, R.string.key_rocket_chat_user_id_summary);
            updateValues(R.string.key_rocket_chat_channel, R.string.channel_title_rocket_chat_summary);
        }

        public void updateValues(@StringRes int prefKeyRes, @StringRes int prefDefaultSummaryRes) {
            final String prefKey = getString(prefKeyRes);
            String prefSummaryDefault = getString(prefDefaultSummaryRes);
            String prefSummaryValue = sharedPreferences.getString(prefKey, prefSummaryDefault);

            if(prefSummaryValue.equals("")) {
                prefSummaryValue = prefSummaryDefault;
            }

            final EditTextPreference editTextPreference = (EditTextPreference) findPreference(prefKey);
            editTextPreference.setSummary(prefSummaryValue);

            editTextPreference.setOnPreferenceChangeListener((preference, o) -> {

                String newValue = o.toString();
                sharedPreferences.edit().putString(prefKey, newValue).apply();
                editTextPreference.setSummary(newValue);
                return true;
            });
        }
    }
}
