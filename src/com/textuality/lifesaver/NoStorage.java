package com.textuality.lifesaver;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

public class NoStorage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nostorage);

        String problem = getIntent().getStringExtra("problem");
        if (problem != null) {
            problem = getString(R.string.horribleError) + " " + problem;
        } else {
            // This means the is-there-external-storage-I-can-use check failed
            String status = Environment.getExternalStorageState();
            if (status == Environment.MEDIA_MOUNTED_READ_ONLY)
                problem = getString(R.string.writeProtected);
            else if (status == Environment.MEDIA_NOFS)
                problem = getString(R.string.noFilesystem);
            else if (status == Environment.MEDIA_REMOVED)
                problem = getString(R.string.storageRemoved);
            else if (status == Environment.MEDIA_SHARED)
                problem = getString(R.string.storageInUse);
            else
                problem = getString(R.string.unknownProblem);
            problem = getString(R.string.cantSave) + problem;
        }

        TextView message = (TextView) findViewById(R.id.errorMessage);
        message.setText(problem);
    }
}
