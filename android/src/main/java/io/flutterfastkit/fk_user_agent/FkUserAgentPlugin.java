package io.flutterfastkit.fk_user_agent;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * FkUserAgentPlugin
 */
public class FkUserAgentPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Context applicationContext;
    private Map<String, Object> constants;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "fk_user_agent");
        channel.setMethodCallHandler(this);
        applicationContext = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if ("getProperties".equals(call.method)) {
            result.success(getProperties());
        } else {
            result.notImplemented();
        }
    }

    private Map<String, Object> getProperties() {
        if (constants != null) {
            return constants;
        }
        constants = new HashMap<>();

        PackageManager packageManager = applicationContext.getPackageManager();
        String packageName = applicationContext.getPackageName();
        String shortPackageName = packageName.substring(packageName.lastIndexOf(".") + 1);
        String applicationName = "";
        String applicationVersion = "";
        int buildNumber = 0;
        String userAgent = getUserAgent();
        String packageUserAgent = userAgent;

        try {
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            applicationName = applicationContext.getApplicationInfo().loadLabel(applicationContext.getPackageManager()).toString();
            applicationVersion = info.versionName;
            buildNumber = info.versionCode;
            packageUserAgent = shortPackageName + '/' + applicationVersion + '.' + buildNumber + ' ' + userAgent;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        constants.put("systemName", "Android");
        constants.put("systemVersion", Build.VERSION.RELEASE);
        constants.put("packageName", packageName);
        constants.put("shortPackageName", shortPackageName);
        constants.put("applicationName", applicationName);
        constants.put("applicationVersion", applicationVersion);
        constants.put("applicationBuildNumber", buildNumber);
        constants.put("packageUserAgent", packageUserAgent);
        constants.put("userAgent", userAgent);
        constants.put("webViewUserAgent", getWebViewUserAgent());

        return constants;
    }

    private String getUserAgent() {
        return System.getProperty("http.agent");

    }

    private String getWebViewUserAgent() {
        return WebSettings.getDefaultUserAgent(applicationContext);

    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        channel = null;
        applicationContext = null;
    }
}
