package com.palladium.atomichub.categories;

import android.content.ContentResolver;
import android.os.Bundle;
import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import android.provider.Settings;
import android.os.SystemProperties;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.palladium.support.preferences.SystemSettingSwitchPreference;
import com.android.settings.Utils;
import android.os.ServiceManager;
import com.palladium.atomichub.*;
import android.app.ActionBar;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;
import java.util.ArrayList;
import java.util.List;
import com.android.internal.widget.LockPatternUtils;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)

public class frag_misc extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private IOverlayManager mOverlayService;
    private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
    private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";
    private static final String SCROLLINGCACHE_DEFAULT = "2";
    private static final String FP_KEYSTORE = "fp_unlock_keystore";
    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";

    private SystemSettingSwitchPreference mFingerprintUnlock;
    private ListPreference mScrollingCachePref;
    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ps_misc);
        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));
        //Feature Additon!
        mScrollingCachePref = (ListPreference) findPreference(SCROLLINGCACHE_PREF);
        mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
        mScrollingCachePref.setOnPreferenceChangeListener(this);

        mFingerprintUnlock = (SystemSettingSwitchPreference) findPreference(FP_KEYSTORE);

        if (mFingerprintUnlock != null) {
           if (LockPatternUtils.isDeviceEncryptionEnabled()) {
               mFingerprintUnlock.setEnabled(false);
               mFingerprintUnlock.setSummary(R.string.fp_encrypt_warning);
            } else {
               mFingerprintUnlock.setEnabled(true);
               mFingerprintUnlock.setSummary(R.string.fp_unlock_keystore_summary);
            }
        }

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SwitchPreference) findPreference(FINGERPRINT_VIB);
        if (!mFingerprintManager.isHardwareDetected()){
            prefScreen.removePreference(mFingerprintVib);
        } else {
            mFingerprintVib.setChecked((Settings.System.getInt(getContentResolver(),
                    Settings.System.FINGERPRINT_SUCCESS_VIB, 1) == 1));
            mFingerprintVib.setOnPreferenceChangeListener(this);
        }

    }
    @Override
    public int getMetricsCategory() {
        return MetricsEvent.PALLADIUM;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        ContentResolver resolver = getActivity().getContentResolver();
        boolean result = false;
        if (preference == mScrollingCachePref) {
            if (newValue != null) {
                SystemProperties.set(SCROLLINGCACHE_PERSIST_PROP, (String) newValue);
            }
            return true;
        } else if (preference == mFingerprintVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.FINGERPRINT_SUCCESS_VIB, value ? 1 : 0);
            return true;
        }
        return false;
    }


    /**
     * For Seaech
     */

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.ps_misc;
                    result.add(sir);
                    return result;
                }

           @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };

}