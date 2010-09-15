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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;

public class Files {

	private static final String LIFESAVER_DIR = "LifeSaver-F";
	private static final String CALL_LOG = "CallLog";
	private static final String MESSAGE_LOG = "MessageLog";

	static File mDirectory = null; 

	/**
	 * Multi-purpose function.  Looks around for places that a life might have been saved; assuming one is found,
	 *  sets mDirectory appropriately, then returns true or false to signal whether the whether a life has been 
	 *  saved.  If there's no likely directory and one can't be created, throws an IOException.
	 * @return true or false depending on whether a life has been saved
	 */
	public static boolean checkStorage(Context context) throws IOException {
		if (mDirectory != null)  
			return (new File(mDirectory, CALL_LOG).exists());

		ArrayList<File> candidates = new ArrayList<File>();
		File esd = Environment.getExternalStorageDirectory();
		candidates.add(new File(esd, "external_sd")); // Samsung Galaxy
		candidates.add(esd);
		candidates.add(new File("/sdcard"));

		// already a saved life?
		for (File base : candidates) {
			File dir = new File(base, LIFESAVER_DIR);
			if (dir.exists() && dir.isDirectory()) {
				File log = new File(dir, CALL_LOG);
				if (log.canRead()) {
					mDirectory = dir;
					return true;
				} else {
					throw new IOException(context.getString(R.string.cantReadLife));
				}
			}
		}

		// OK, take the first one base dir that actually exists
		for (File base : candidates) {
			if (base.exists() && base.isDirectory()) {
				mDirectory = new File(base, LIFESAVER_DIR);
				mDirectory.mkdir();
				return false;
			}
		}

		// no good candidate base directory
		throw new IOException(context.getString(R.string.unknownProblem));
	}

	public static PrintStream printCallLog(Context context) {
		return printer(CALL_LOG);
	}

	public static BufferedReader readCallLog(Context context) {
		return reader(CALL_LOG);
	}

	public static PrintStream printMessageLog(Context context) {
		return printer(MESSAGE_LOG);
	}

	public static BufferedReader readMessageLog(Context context) {
		return reader(MESSAGE_LOG);
	}

	private static BufferedReader reader(String name) {
		try {
			return new BufferedReader(new FileReader(new File(mDirectory, name)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static PrintStream printer(String name) {
		try {
			File log = new File(mDirectory, name);
			if (log.exists())
				log.delete();
			log.createNewFile();
			return new PrintStream(new FileOutputStream(log));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
