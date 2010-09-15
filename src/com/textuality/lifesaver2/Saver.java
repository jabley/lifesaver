/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.textuality.lifesaver2;

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
            float total = messages.getCount();
            denominator = total / 100.0F;
            while (messages.moveToNext()) {
            	if (message.hasField(messages, "address")) {
            		print.println(message.cursorToJSON(messages));
            		savedMessages += 1;
            		sMessageProgress.setProgress((int) (savedMessages / denominator));
            	} else {
            		total -= 1;
            		denominator = total / 100.0F;
            	}
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
