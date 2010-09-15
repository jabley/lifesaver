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

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;

public class LifeSaver extends Activity {
    private Context mContext;
    private TextView mSaveText, mRestoreText;
    private ImageView mSaveBuoy, mRestoreBuoy;
    private final static long DURATION = 1000L;
    private Intent mNextStep;

    @Override
    public void onCreate(Bundle mumble) {
        super.onCreate(mumble);
        mContext = this;
        setContentView(R.layout.main);

        mSaveBuoy = (ImageView) findViewById(R.id.topBuoy);
        mRestoreBuoy = (ImageView) findViewById(R.id.bottomBuoy);
        mSaveText = (TextView) findViewById(R.id.topText);
        mRestoreText = (TextView) findViewById(R.id.bottomText);

        findViewById(R.id.mainTop).setOnClickListener(saveClick);
        findViewById(R.id.mainBottom).setOnClickListener(restoreClick);
    }

    private OnClickListener saveClick = new OnClickListener() {
        public void onClick(View v) {
        	try {
        		if (Files.checkStorage(mContext)) {
        			showDialog(0);
        		}
        		else {
        			goSave();
        		}
        	} catch (IOException e) {
        		goDie(e);
            }
        }
    };
    
	public void goDie(Throwable e) {
		goDie(e.getMessage());
	}
	
	public void goDie(String s) {
		Uri uri = Uri.parse("content://com.textuality.lifesaver.NoStorage/");
		Intent intent = new Intent(mContext, NoStorage.class);
		intent.setData(uri);
		intent.putExtra("problem", s);
		mContext.startActivity(intent);		
	}

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.overwriteCheck));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.wipeIt),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        goSave();
                    }
                });
        builder.setNegativeButton(getString(R.string.doNotOverWrite),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    private void goSave() {
        Uri uri = Uri.parse("content://com.textuality.lifesaver.Saver/");
        mNextStep = new Intent(mContext, Saver.class);
        mNextStep.setData(uri);
        saveAnimation();
    }

    private OnClickListener restoreClick = new OnClickListener() {
        public void onClick(View v) {
        	try {
        		if (Files.checkStorage(mContext)) {
                    Uri uri = Uri.parse("content://com.textuality.lifesaver.Restoring/");
                    mNextStep = new Intent(mContext, Restorer.class);
                    mNextStep.setData(uri);
                    restoreAnimation();
        		} else {
        			goDie(new Exception(getString(R.string.noSavedLifeFound)));
        		}
        	} catch (IOException e) {
        		goDie(new Exception(getString(R.string.unknownProblem)));
        	}
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // they may have been blanked by the transfer animation
        mSaveBuoy.setVisibility(View.VISIBLE);
        mRestoreBuoy.setVisibility(View.VISIBLE);
        mSaveText.setVisibility(View.VISIBLE);
        mRestoreText.setVisibility(View.VISIBLE);
    }

    private AnimationSet roll(ImageView buoy, boolean left) {
        AnimationSet roll = new AnimationSet(false);
        float degrees = 360F;
        float target = (float) buoy.getWidth();
        boolean landscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        if (left) {
            if (!landscape) {
                degrees = -degrees;
                target = -target;
            }
        } else {
            if (landscape) {
                degrees = -degrees;
                target = -target;
            }
        }
        RotateAnimation spin = new RotateAnimation(0, degrees,
                buoy.getWidth() / 2, buoy.getHeight() / 2);
        spin.setDuration(DURATION);
        TranslateAnimation move = new TranslateAnimation(0F, target, 0F, 0F);
        move.setDuration(DURATION);
        roll.addAnimation(spin);
        roll.addAnimation(move);
        return roll;
    }

    private AlphaAnimation fade() {
        AlphaAnimation a = new AlphaAnimation(1.0F, 0.0F);
        a.setDuration(DURATION);
        return a;
    }

    AnimationListener toNextStep = new AnimationListener() {

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            startActivity(mNextStep);

            // so that the next view slides in smoothly
            mSaveBuoy.setVisibility(View.GONE);
            mSaveText.setVisibility(View.GONE);
            mRestoreBuoy.setVisibility(View.GONE);
            mRestoreText.setVisibility(View.GONE);
        }
    };

    private void restoreAnimation() {
        Animation roll = roll(mRestoreBuoy, true);
        Animation fade = fade();
        mSaveBuoy.startAnimation(fade);
        mSaveText.startAnimation(fade);
        mRestoreText.startAnimation(fade);
        mRestoreBuoy.startAnimation(roll);

        fade.setAnimationListener(toNextStep);
    }

    private void saveAnimation() {
        Animation roll = roll(mSaveBuoy, false);
        Animation fade = fade();
        mSaveBuoy.startAnimation(roll);
        mSaveText.startAnimation(fade);
        mRestoreText.startAnimation(fade);
        mRestoreBuoy.startAnimation(fade);

        fade.setAnimationListener(toNextStep);
    }

    public static Intent comeBack(Context context) {
        Uri uri = Uri.parse("content://com.textuality.lifesaver.LifeSaver/");
        Intent intent = new Intent(context, LifeSaver.class);
        intent.setData(uri);
        return intent;
    }

}
