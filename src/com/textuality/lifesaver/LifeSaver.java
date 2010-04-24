package com.textuality.lifesaver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class LifeSaver extends Activity {
    private Context context;
    private TextView saveText, restoreText;
    private ImageView saveBuoy, restoreBuoy;
    private final static long DURATION = 1000L;
    private Intent nextStep;

    @Override
    public void onCreate(Bundle mumble) {
        super.onCreate(mumble);
        context = this;
        setContentView(R.layout.main);

        saveBuoy = (ImageView) findViewById(R.id.topBuoy);
        restoreBuoy = (ImageView) findViewById(R.id.bottomBuoy);
        saveText = (TextView) findViewById(R.id.topText);
        restoreText = (TextView) findViewById(R.id.bottomText);

        findViewById(R.id.mainTop).setOnClickListener(saveClick);
        findViewById(R.id.mainBottom).setOnClickListener(restoreClick);
    }

    private OnClickListener saveClick = new OnClickListener() {
        public void onClick(View v) {
            if (!haveStorage(true))
                return;
            if (Files.backupExists()) {
                showDialog(0);
            } else {
                goSave();
            }
        }
    };

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
        nextStep = new Intent(context, Saver.class);
        nextStep.setData(uri);
        saveAnimation();
    }

    private OnClickListener restoreClick = new OnClickListener() {
        public void onClick(View v) {
            if (haveStorage(false)) {
                Uri uri = Uri
                        .parse("content://com.textuality.lifesaver.Restoring/");
                nextStep = new Intent(context, Restorer.class);
                nextStep.setData(uri);
                restoreAnimation();
            }
        }
    };

    private boolean haveStorage(boolean needToWrite) {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;
        else if ((needToWrite == false)
                && status.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
            return true;
        else {
            Uri uri = Uri.parse("content://com.textuality.lifesaver.NoStorage/");
            Intent intent = new Intent(context, NoStorage.class);
            intent.setData(uri);
            startActivity(intent);
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // they may have been blanked by the transfer animation
        saveBuoy.setVisibility(View.VISIBLE);
        restoreBuoy.setVisibility(View.VISIBLE);
        saveText.setVisibility(View.VISIBLE);
        restoreText.setVisibility(View.VISIBLE);
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
            startActivity(nextStep);

            // so that the next view slides in smoothly
            saveBuoy.setVisibility(View.GONE);
            saveText.setVisibility(View.GONE);
            restoreBuoy.setVisibility(View.GONE);
            restoreText.setVisibility(View.GONE);
        }
    };

    private void restoreAnimation() {
        Animation roll = roll(restoreBuoy, true);
        Animation fade = fade();
        saveBuoy.startAnimation(fade);
        saveText.startAnimation(fade);
        restoreText.startAnimation(fade);
        restoreBuoy.startAnimation(roll);

        fade.setAnimationListener(toNextStep);
    }

    private void saveAnimation() {
        Animation roll = roll(saveBuoy, false);
        Animation fade = fade();
        saveBuoy.startAnimation(roll);
        saveText.startAnimation(fade);
        restoreText.startAnimation(fade);
        restoreBuoy.startAnimation(fade);

        fade.setAnimationListener(toNextStep);
    }

    public static Intent comeBack(Context context) {
        Uri uri = Uri.parse("content://com.textuality.lifesaver.LifeSaver/");
        Intent intent = new Intent(context, LifeSaver.class);
        intent.setData(uri);
        return intent;
    }

}
