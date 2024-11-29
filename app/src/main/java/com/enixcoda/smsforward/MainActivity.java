package com.enixcoda.smsforward;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "onCreate: SMS Forwarder started");

        requestRequiredPermissions();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        checkDefaultSmsApp();
    }

    public void requestRequiredPermissions() {
        Log.d("MainActivity", "requestRequiredPermissions: Checking SMS receive, send, and internet permissions");
        String[] permissions = {
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.INTERNET
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "requestRequiredPermissions: Permission not granted: " + permission);
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            Log.d("MainActivity", "requestSmsReceivePermission: Requesting necessary permissions");
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            Log.i("MainActivity", "requestSmsReceivePermission: All necessary permissions granted");
        }
    }

    public void checkDefaultSmsApp() {
        Log.d("MainActivity", "checkDefaultSmsApp: Checking default SMS app");
        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(getPackageName())) {
            Log.d("MainActivity", "checkDefaultSmsApp: Setting default SMS app");
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
            startActivity(intent);
        } else {
            Log.i("MainActivity", "checkDefaultSmsApp: Default SMS app is already set");

            // Test the SMS forwarding using one of the services
            // REFACTOR NEEDED
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

            // Preview Twilio values
            updateValues(R.string.key_twilio_account_sid, R.string.key_twilio_account_sid_summary);
            updateValues(R.string.key_twilio_auth_token, R.string.key_twilio_auth_token_summary);
            updateValues(R.string.key_twilio_from, R.string.key_twilio_from_title);
            updateValues(R.string.key_twilio_to, R.string.key_twilio_to_title);
        }

        /**
         * Updates the summary of an EditTextPreference with the current value from SharedPreferences.
         *
         * @param prefKeyRes            The resource ID of the preference key.
         * @param prefDefaultSummaryRes The resource ID of the default summary text.
         */
        public void updateValues(@StringRes int prefKeyRes, @StringRes int prefDefaultSummaryRes) {
            final String prefKey = getString(prefKeyRes);
            String prefSummaryDefault = getString(prefDefaultSummaryRes);
            String prefSummaryValue = sharedPreferences.getString(prefKey, prefSummaryDefault);

            if (prefSummaryValue.equals("")) {
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
