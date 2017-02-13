/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.utils;

import java.io.File;
import java.io.IOException;

public class TempFolderUtil {

	static String tempFolder = null;

	public static String getFolder() {

		if (TempFolderUtil.tempFolder != null) {
			return TempFolderUtil.tempFolder;
		}

		if (System.getProperty("os.name").contains("Windows")) {
			TempFolderUtil.tempFolder = "C:\\temp\\";
		} else if (System.getProperty("os.name").contains("Mac")) {
			final File _uploadFolder = new File(System.getProperty("file.separator") + "Users"
					+ System.getProperty("file.separator") + System.getProperty("user.name")
					+ System.getProperty("file.separator") + "tmp"); // System.getProperty("user.dir"));
			_uploadFolder.mkdirs();
			try {
				TempFolderUtil.tempFolder = _uploadFolder.getCanonicalPath();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} else {
			final File _uploadFolder = new File(System.getProperty("file.separator") + "tmp"
					+ System.getProperty("file.separator"));
			_uploadFolder.mkdirs();

			try {
				TempFolderUtil.tempFolder = _uploadFolder.getCanonicalPath();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return TempFolderUtil.tempFolder;
	}
}
