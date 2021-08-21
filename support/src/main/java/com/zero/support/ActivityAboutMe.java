package com.zero.support;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;


public class ActivityAboutMe extends Activity
{
    TextView appName, appVersion;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) actionBar.hide();
        setContentView(com.zero.support.R.layout.support_activity_about_me);
        appName = findViewById(com.zero.support.R.id.aboutTextViewAppName);
        appVersion = findViewById(com.zero.support.R.id.aboutTextViewAppVersion);

        try
        {
            appName.setText(getPackageManager().getApplicationLabel(getApplicationInfo()).toString());
            appVersion.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        findViewById(R.id.mainImageViewMail).setOnClickListener(p1 -> {
            Intent send = new Intent(Intent.ACTION_SEND);
            String[] mailto = {"amit.just.sh@gmail.com"};
            send.putExtra(Intent.EXTRA_EMAIL, mailto);
            send.putExtra(Intent.EXTRA_SUBJECT, "Question or Suggestion");
            send.putExtra(Intent.EXTRA_TEXT, " Hi, I have a ...");
            Intent chooser = new Intent(Intent.ACTION_SENDTO);
            chooser.setData(Uri.parse("mailto:"));
            send.setSelector(chooser);
            startActivity(Intent.createChooser(send, "Send Email Using.."));
        });
    }
}
