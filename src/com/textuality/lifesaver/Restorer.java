package com.textuality.lifesaver;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Restorer extends Activity {

    private ProgressBar callProgress, messageProgress;
    private Activity context;
    private TextView cpLabel, mpLabel;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle mumble) {
        super.onCreate(mumble);
        setContentView(R.layout.restore);
        context = this;

        callProgress = (ProgressBar) findViewById(R.id.callProgress);
        messageProgress = (ProgressBar) findViewById(R.id.messageProgress);
        cpLabel = (TextView) findViewById(R.id.cpLabel);
        mpLabel = (TextView) findViewById(R.id.mpLabel);
        findViewById(R.id.restoreBottom).setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        startActivity(LifeSaver.comeBack(context));
                    }
                });

        new Thread(new CopyOut()).start();
    }

    class CopyOut implements Runnable {

        private BufferedReader callLog, messageLog;
        private int callTotal, messageTotal;
        private int restored;

        public CopyOut() {
            callLog = Files.readCallLog(context);
            messageLog = Files.readMessageLog(context);
            try {
                callTotal = Integer.parseInt(callLog.readLine().trim());
                messageTotal = Integer.parseInt(messageLog.readLine().trim());
            } catch (IOException e) {
                Files.die(context, e);
            }
            cpLabel.setText(callTotal + " " + getString(R.string.savedCalls));
            mpLabel.setText(messageTotal + " "
                    + getString(R.string.savedMessages));
        }

        public void run() {
            Columns call = ColumnsFactory.calls();
            Columns message = ColumnsFactory.messages();
            Boolean exists = new Boolean(true);

            try {
                Uri uri = CallLog.Calls.CONTENT_URI;
                Cursor cursor = getContentResolver().query(uri, null, null,
                        null, null);
                HashMap<String, Boolean> logged = new HashMap<String, Boolean>();
                while (cursor.moveToNext()) {
                    logged.put(call.cursorToKey(cursor), exists);
                }
                cursor.close();

                restored = restore(callLog, logged, ColumnsFactory.calls(),
                        uri, callProgress, null, callTotal);
                handler.post(new Runnable() {
                    public void run() {
                        String msg = getString(R.string.restored) + " "
                                + restored + " " + getString(R.string.calls)
                                + ".";
                        cpLabel.setText(msg);
                    }
                });

                uri = Uri.parse("content://sms/");
                cursor = getContentResolver()
                        .query(uri, null, null, null, null);
                logged.clear();
                while (cursor.moveToNext())
                    logged.put(message.cursorToKey(cursor), exists);
                cursor.close();

                restored = restore(messageLog, logged, ColumnsFactory
                        .messages(), uri, messageProgress, "thread_id",
                        messageTotal);
                handler.post(new Runnable() {
                    public void run() {
                        String msg = getString(R.string.restored) + " "
                                + restored + " " + getString(R.string.messages)
                                + ".";
                        mpLabel.setText(msg);
                        findViewById(R.id.restoredOK).setVisibility(
                                View.VISIBLE);
                    }
                });

            } catch (Exception e) {
                Files.die(context, e);
            }
        }
    }

    private int restore(BufferedReader file, HashMap<String, Boolean> logged,
            Columns r, Uri uri, ProgressBar progress, String zeroField,
            int total) throws Exception {
        int added = 0;
        ContentResolver cr = getContentResolver();
        int count = 0;
        String line;
        float denominator = ((float) total) / 100.0F;
        while ((line = file.readLine()) != null) {
            JSONObject json = new JSONObject(line);
            String key = r.jsonToKey(json);
            if (logged.get(key) == null) {
                ContentValues cv = r.jsonToContentValues(json);
                if (zeroField != null)
                    cv.put(zeroField, 0);
                cr.insert(uri, cv);
                added++;
            }
            count += 1;
            progress.setProgress((int) (count / denominator));
        }
        file.close();
        return added;
    }
}
