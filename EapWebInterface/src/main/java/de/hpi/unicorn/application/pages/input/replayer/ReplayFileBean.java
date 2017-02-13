package de.hpi.unicorn.application.pages.input.replayer;

import java.io.Serializable;

public class ReplayFileBean implements Serializable {

	private static final long serialVersionUID = 1L;
	public String name;
	public String filePath;
	public String eventTypeName;
	public FileType type;

	public enum FileType {
		CSV, XML_ZIP;
	}

	public ReplayFileBean(String name, String filePath, String eventTypeName, FileType type) {
		this.name = name;
		this.filePath = filePath;
		this.eventTypeName = eventTypeName;
		this.type = type;
	}

	@Override
	public String toString() {
		return name + " (" + eventTypeName + ")";
	}

}
