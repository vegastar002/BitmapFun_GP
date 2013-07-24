package com.android.hardcore.crashreport;
import java.io.File;
import java.util.ArrayList;

import net.tsz.afinal.FinalBitmap;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.example.ui.newVersionReceiver;
import com.free.hardcore.wp9.R;


/**
 * <p>
 * Base class for any application which need to send crash reports. The final
 * application has to extend this class and at least implement the abstract
 * method {@link #getReportEmail()} by returning a email that reports will be
 * sent to.
 * </p>
 * Needs permission :android.permission.WRITE_EXTERNAL_STORAGE
 */
public abstract class CrashReportingApplication extends Application implements OnSharedPreferenceChangeListener {

	protected static final String LOG_TAG = "ACRA";
    private static final String CRASH_FILE = "crash.txt";
    
    public static Context appContext;
    public static FinalBitmap mFinalBitmap = null;
    
    /**
     * Bundle key for the text of cancel button in the crash dialog.
     *
     * @see #getCrashResources()
     */
    public static final String RES_BUTTON_CANCEL = "RES_BUTTON_CANCEL";
    /**
     * Bundle key for the text of report button in the crash dialog.
     *
     * @see #getCrashResources()
     */
    public static final String RES_BUTTON_REPORT = "RES_BUTTON_REPORT";
    /**
     * Bundle key for the text of restart button in the crash dialog.
     * If the text is not specified, the restart button will not show.
     *
     * @see #getCrashResources()
     */
    public static final String RES_BUTTON_RESTART = "RES_BUTTON_RESTART";
    /**
     * Bundle key for the icon in the crash dialog.
     *
     * @see #getCrashResources()
     */
    public static final String RES_DIALOG_ICON = "RES_DIALOG_ICON";
    /**
     * Bundle key for the title in the crash dialog.
     *
     * @see #getCrashResources()
     */
    public static final String RES_DIALOG_TITLE = "RES_DIALOG_TITLE";
    /**
     * Bundle key for the text in the crash dialog.
     *
     * @see #getCrashResources()
     */
    public static final String RES_DIALOG_TEXT = "RES_DIALOG_TEXT";

    /**
     * Bundle key for the subject of the report email.
     *
     * @see #getCrashResources()
     */
    public static final String RES_EMAIL_SUBJECT = "RES_EMAIL_SUBJECT";
    /**
     * Bundle key for the body of the report email.
     *
     * @see #getCrashResources()
     */
    public static final String RES_EMAIL_TEXT = "RES_EMAIL_TEXT";

    /**
     * The key of the application default SharedPreference where you can put a
     * 'true' Boolean value to disable ACRA.
     */
    public static final String PREF_ENABLE_ACRA = "acra.enable";
    
    public static String packName = "";
    public static String pape = "";
    public static ArrayList<Byte> svdatList = new ArrayList<Byte>();
    public static byte [] svtte = new byte[10];

    public void onCreate() {
        super.onCreate();

        appContext = getApplicationContext();
        mFinalBitmap = FinalBitmap.create(getApplicationContext());
        mFinalBitmap.configLoadfailImage(R.drawable.error);
        mFinalBitmap.configLoadingImage(R.drawable.empty_photo);
		
        final SharedPreferences prefs = getACRASharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);
        
        // If the application default shared preferences contains true for the
        // key "acra.disable", do not activate ACRA. Also checks the alternative
        // opposite setting "acra.enable" if "acra.disable" is not found.
        boolean enableAcra = true;
        try {
            enableAcra = prefs.getBoolean(PREF_ENABLE_ACRA,isCrashReportEnableByDefault());
        } catch (final Exception e) {
            e.printStackTrace();
            // In case of a ClassCastException
        } finally{
            System.gc();
        }

        if (enableAcra) {
            initAcra();
        }
    }
    
	public String getAppName() {
		String appName = "";
		final PackageManager packageManager = getPackageManager();
		try {
			final ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
			appName = (String) applicationInfo.loadLabel(packageManager);
		} catch (final NameNotFoundException e) {
			e.printStackTrace();
		} finally {
			System.gc();
		}
		return appName;
	}
	
	public String getVersionName() {
		final PackageManager manager = getPackageManager();
		final String name = getPackageName();
		try {
			final PackageInfo info = manager.getPackageInfo(name, 0);
			return "v" + info.versionName;
		} catch (final NameNotFoundException e) {
			return "";
		} finally {
			System.gc();
		}
	}
	
	
    /**
     * Activate ACRA.
     */
    private void initAcra() {
        // Initialise ErrorReporter with all required data
        final ErrorReporter errorReporter = ErrorReporter.getInstance();
        String report = "";
        report = getReportEmail();
        errorReporter.setReportEmail(report);
        // Activate the ErrorReporter
        errorReporter.init(this);
    }

    /**
     * Implement this method by returning a email that reports will be sent to.
     *
     * @return The Id of your GoogleDoc Form generated by importing ACRA's
     *         spreadsheet template.
     */
    public abstract String getReportEmail();

    /**
     * Override this method to modify strings showed in crash dialog. Return a Bundle
     * containing :
     * <ul>
     * <li>{@link #RES_BUTTON_REPORT}</li>
     * <li>{@link #RES_BUTTON_CANCEL}</li>
     * <li>{@link #RES_DIALOG_ICON}</li>
     * <li>{@link #RES_DIALOG_TITLE}</li>
     * <li>{@link #RES_DIALOG_TEXT}</li>
     * <li>{@link #RES_EMAIL_SUBJECT}</li>
     * <li>{@link #RES_EMAIL_TEXT}</li>
     * </ul>
     *
     * @return A Bundle containing the resource String necessary to interact with
     *         the user.
     */
    public abstract Bundle getCrashResources();

    /**
     * Override this method to change the location of crash report file.
     * The file should be on external storage,otherwise the email can't send this file as attachment.
     * @return
     */
    public File getCrashReportFile() {
        return new File(Environment.getExternalStorageDirectory(),CRASH_FILE);
    }

    /**
     * Called when user click "restart" button of crash dialog.
     * @see #RES_BUTTON_RESTART
     */
    public void onRestart() {

    }

    /**
     * Called when application crashed.
     * @param t	the thread that has an uncaught exception
     * @param e the exception that was thrown
     */
    public void onCrashed(final Thread t, final Throwable e) {

    }


	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
            final String key) {
        if (PREF_ENABLE_ACRA.equals(key)) {
            Boolean enableAcra = true;
            try {
                enableAcra = sharedPreferences.getBoolean(key, isCrashReportEnableByDefault());
            } catch (final Exception e) {
                e.printStackTrace();
            } finally{
                System.gc();
            }
            if (enableAcra) {
                initAcra();
            } else {
                ErrorReporter.getInstance().disable();
            }
        }
    }

    public void setCrashReportEnable(final boolean enable){
        final SharedPreferences prefs = getACRASharedPreferences();
        final Editor editor = prefs.edit();
        editor.putBoolean(PREF_ENABLE_ACRA, enable);
        editor.commit();
    }

    /**
     * Override this method if you need to store "acra.disable" or "acra.enable"
     * in a different SharedPrefence than the application's default.
     *
     * @return The Shared Preferences where ACRA will check the value of the
     *         setting which disables/enables it's action.
     */
    public SharedPreferences getACRASharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    public boolean isCrashReportEnableByDefault(){
        return true;
    }

    public boolean isDebuggable() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
}
