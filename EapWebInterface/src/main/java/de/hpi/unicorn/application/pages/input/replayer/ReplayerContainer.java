package de.hpi.unicorn.application.pages.input.replayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplayerContainer {
	public static Map<Date, EventReplayer> replayers = new HashMap<Date, EventReplayer>();
	public static Map<String, List<ReplayFileBean>> files = new HashMap<String, List<ReplayFileBean>>();

	public static Map<Date, EventReplayer> getReplayers() {
		return replayers;
	}

	public static EventReplayer getReplayer(Date creationDate) {
		return replayers.get(creationDate);
	}

	public static Map<Date, EventReplayer> addReplayer(EventReplayer replayer) {
		replayers.put(new Date(), replayer);
		return replayers;
	}

	public static Map<Date, EventReplayer> removeReplayer(Date creationDate) {
		replayers.remove(creationDate);
		return replayers;
	}

	public static void cleanReplayers() {
		replayers.clear();
	}

	public static Map<String, List<ReplayFileBean>> getFileBeans() {
		return files;
	}

	public static List<ReplayFileBean> getFileBeans(String category) {
		return files.get(category);
	}

	public static Map<String, List<ReplayFileBean>> addFileBean(String category, ReplayFileBean bean) {
		if (!files.containsKey(category)) {
			files.put(category, new ArrayList<ReplayFileBean>());
		}
		files.get(category).add(bean);
		return files;
	}

	public static Map<String, List<ReplayFileBean>> removeFileBean(String category, ReplayFileBean bean) {
		if (files.containsKey(category)) {
			files.get(category).remove(bean);
			if (files.get(category).isEmpty()) {
				files.remove(category);
			}
		}
		return files;
	}

	public static Map<String, List<ReplayFileBean>> removeCategory(String category) {
		files.remove(category);
		return files;
	}

	public static void cleanFileBeans() {
		files.clear();
	}

}
