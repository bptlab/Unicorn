/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.xml;

import java.io.PrintStream;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.lib.OutputURIResolver;

public class PrintStreamURIResolver implements OutputURIResolver {

	private final PrintStream outputs;

	public PrintStreamURIResolver(final PrintStream outputsIn) {
		this.outputs = outputsIn;
	}

	@Override
	public void close(final Result arg0) throws TransformerException {
	}

	@Override
	public OutputURIResolver newInstance() {
		return new PrintStreamURIResolver(this.outputs);
	}

	@Override
	public Result resolve(final String href, final String base) throws TransformerException {
		System.setOut(this.outputs);
		return new StreamResult(System.out);
	}

}
