/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.edifact.importer;

import java.io.File;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;

import de.hpi.unicorn.importer.FileUtils;
import de.hpi.unicorn.importer.edifact.EdifactImporter;

public class EdifactConverterTest {

	@Test
	public void convertFileBerman() throws Exception {
		final String path = "src/test/resources/EdifactFiles/1_BERMAN.txt";
		this.parseFile(path);
	}

	@Test
	public void convertFileIFtmcs() throws Exception {
		final String path = "src/test/resources/EdifactFiles/2_IFTMCS.txt";
		this.parseFile(path);
	}

	@Test
	public void convertFileCoprar() throws Exception {
		final String path = "src/test/resources/EdifactFiles/3_COPRAR.txt";
		this.parseFile(path);
	}

	@Test
	public void convertFileCoarri() throws Exception {
		final String path = "src/test/resources/EdifactFiles/5_COARRI.txt";
		this.parseFile(path);
	}

	@Test
	public void convertFileCopino() throws Exception {
		final String path = "src/test/resources/EdifactFiles/6_COPINO.txt";
		this.parseFile(path);
	}

	private void parseFile(final String path) throws Exception {
		final String outPutpath = path.substring(0, path.indexOf(".")) + ".xml";
		final StreamResult result = EdifactImporter.getInstance().convertEdiFileToXML(path);
		FileUtils.writeResultToFile(result, outPutpath);
		// System.out.println("Edifact-File <" + path + "> converted to <" +
		// path + ">.");
		final File output = new File(outPutpath);
		Assert.assertTrue("Output was not created", output.exists());
		output.delete();
		// System.out.println("deleted " + output.getAbsolutePath());
	}

	@Test
	public void convertAll() throws Exception {
		// convert all testfiles
		final File folder = new File("src/test/resources/EdifactFiles");
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile() && fileEntry.getPath().endsWith(".txt")) {
				final String path = folder.getPath() + "/" + fileEntry.getName();
				this.parseFile(path);
			}
		}
	}

}
