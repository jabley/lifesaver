package com.textuality.lifesaver;

import java.io.PrintStream;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Saver extends Activity {

    private Context context;
    private Columns call = ColumnsFactory.calls();
    private Columns message = ColumnsFactory.messages();
    private TextView cpLabel, mpLabel;
    private ProgressBar sCallProgress, sMessageProgress;
    private Handler handler = new Handler();
    private int savedCalls, savedMessages;

    @Override
    protected void onCreate(Bundle mumble) {
        super.onCreate(mumble);
        setContentView(R.layout.save);
        context = this;

        cpLabel = (TextView) findViewById(R.id.sCpLabel);
        mpLabel = (TextView) findViewById(R.id.sMpLabel);
        sCallProgress = (ProgressBar) findViewById(R.id.sCallProgress);
        sMessageProgress = (ProgressBar) findViewById(R.id.sMessageProgress);

        findViewById(R.id.saveBottom).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(LifeSaver.comeBack(context));
            }
        });

        new Thread(new CopyIn()).start();
    }

    class CopyIn implements Runnable {
        private Cursor calls, messages;

        public CopyIn() {
            calls = getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                    null, null, null);
            messages = getContentResolver().query(Uri.parse("content://sms/"),
                    null, null, null, null);
            String saving = getString(R.string.saving);
            cpLabel.setText(saving + " " + calls.getCount() + " "
                    + getString(R.string.calls) + "...");
            mpLabel.setText(saving + " " + messages.getCount() + " "
                    + getString(R.string.messages) + "...");
        }

        public void run() {
            PrintStream print = Files.printCallLog(context);
            print.println(calls.getCount()); // first line of saved file is the
                                             // line count
            savedCalls = 0;
            float denominator = calls.getCount() / 100.0F;
            while (calls.moveToNext()) {
                print.println(call.cursorToJSON(calls));
                savedCalls += 1;
                sCallProgress.setProgress((int) (savedCalls / denominator));
            }
            calls.close();
            print.close();
            handler.post(new Runnable() {
                String msg = getString(R.string.saved) + " " + savedCalls + " "
                        + getString(R.string.calls) + ".";

                public void run() {
                    cpLabel.setText(msg);
                }
            });

            print = Files.printMessageLog(context);
            print.println(messages.getCount());
            savedMessages = 0;
            denominator = messages.getCount() / 100.0F;
            while (messages.moveToNext()) {
                print.println(message.cursorToJSON(messages));
                savedMessages += 1;
                sMessageProgress
                        .setProgress((int) (savedMessages / denominator));
            }
            print.close();
            messages.close();
            handler.post(new Runnable() {
                public void run() {
                    String msg = getString(R.string.saved) + " "
                            + savedMessages + " "
                            + getString(R.string.messages) + ".";
                    mpLabel.setText(msg);
                    findViewById(R.id.savedOK).setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
