/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hpi.unicorn.adapter.BoschIot.BoschIotAdapter;
import de.hpi.unicorn.adapter.GoodsTag.GoodsTagAdapter;
import de.hpi.unicorn.adapter.tfl.TflAdapter;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import de.hpi.unicorn.configuration.EapConfiguration;

/**
 * TODO add description and function of the class here
 *
 * @author Jan Selke
 * @version 0.1, Mar 13, 2015
 */
public final class AdapterManager {

	private static AdapterManager instance;
	private final Map<String, EventAdapter> adapters = new HashMap<String, EventAdapter>();
	private final List<String> runningAdapters = new ArrayList<String>();

	// Singleton
	private AdapterManager() {

	}

	public static synchronized AdapterManager getInstance() {
		if (AdapterManager.instance == null) {
			AdapterManager.instance = new AdapterManager();
		}
		return AdapterManager.instance;
	}

	public EventAdapter create(final String name, final AdapterType type) {
		EventAdapter adapter = null;
		if (this.adapters.containsKey(name)) {
			return null;
		}
		switch (type) {
			case NokiaHere:
				adapter = new NokiaHereAdapter(name);
				this.adapters.put(name, adapter);
				return adapter;
			case TransportForLondon:
				adapter = new TflAdapter(name);
				this.adapters.put(name, adapter);
				return adapter;
			case BoschIot:
				adapter = new BoschIotAdapter(name);
				this.adapters.put(name, adapter);
				return adapter;
			case GoodsTag:
				adapter = new GoodsTagAdapter(name);
				this.adapters.put(name, adapter);
				return adapter;
			default:
				return null;
		}
	}

	public EventAdapter get(final String name) {
		return this.adapters.get(name);
	}

	public EventAdapter remove(final String name) {
		if (this.runningAdapters.contains(name)) {
			final EventAdapter adapter = this.adapters.get(name);
			adapter.stop();
			this.runningAdapters.remove(name);
		}
		return this.adapters.remove(name);
	}

	public void start(final String name) {
		final EventAdapter adapter = this.adapters.get(name);
		if (adapter == null) {
			return;
		}
		this.runningAdapters.add(name);
		adapter.start(EapConfiguration.defaultInterval);
	}

	public boolean stop(final String name) {
		final EventAdapter adapter = this.adapters.get(name);
		if (adapter == null) {
			return false;
		}
		this.runningAdapters.remove(name);
		// try {
		// org.quartz.Scheduler scheduler =
		// StdSchedulerFactory.getDefaultScheduler();
		// scheduler.getContext().remove(name);
		// } catch (SchedulerException e) {
		// e.printStackTrace();
		// }
		return adapter.stop();
	}

	public boolean startAll() {
		try {
			StdSchedulerFactory.getDefaultScheduler().resumeAll();
			return true;
		} catch (final SchedulerException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean stopAll() {
		try {
			StdSchedulerFactory.getDefaultScheduler().standby();
			return true;
		} catch (final SchedulerException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void startNokiaHereAdapterForDemoRoute() {

		NokiaHereAdapter na = (NokiaHereAdapter) create("AMS-Calais", AdapterType.NokiaHere);
		if (na == null) {
			na = (NokiaHereAdapter) get("AMS-Calais");
		}
		// Area-coordinates represent the route from Amsterdam Airport to
		// Eurotunnel Calais
		na.setAreaForCorridor(50, 52.309, 4.763, 52.31, 4.76, 52.309, 4.758, 52.308, 4.754, 52.307, 4.75, 52.308, 4.746, 52.308, 4.745, 52.306, 4.744, 52.303, 4.744, 52.3, 4.742, 52.295, 4.736, 52.292, 4.732, 52.287, 4.725, 52.284, 4.721, 52.28, 4.715, 52.274, 4.707, 52.267, 4.697, 52.262, 4.689, 52.252, 4.675, 52.242, 4.661, 52.238, 4.655, 52.232, 4.647, 52.227, 4.64, 52.222, 4.632, 52.219, 4.629, 52.215, 4.626, 52.213, 4.625, 52.21, 4.623, 52.206, 4.62, 52.202, 4.617, 52.199, 4.613, 52.192, 4.607, 52.185, 4.599, 52.181, 4.593, 52.179, 4.587, 52.174, 4.574, 52.17, 4.566, 52.167, 4.562, 52.164, 4.558, 52.162, 4.556, 52.16, 4.552, 52.158, 4.549, 52.155, 4.543, 52.152, 4.537, 52.149, 4.53, 52.148, 4.526, 52.147, 4.523, 52.146, 4.519, 52.144, 4.515, 52.14, 4.51, 52.139, 4.508, 52.135, 4.503, 52.132, 4.497, 52.13, 4.493, 52.126, 4.484, 52.121, 4.476, 52.116, 4.468, 52.108, 4.457, 52.103, 4.449, 52.094, 4.432, 52.085, 4.415, 52.078, 4.405, 52.077, 4.404, 52.074, 4.397, 52.071, 4.39, 52.068, 4.383, 52.064, 4.377, 52.061, 4.374, 52.058, 4.37, 52.053, 4.364, 52.049, 4.36, 52.046, 4.351, 52.044, 4.347, 52.042, 4.348, 52.034, 4.352, 52.03, 4.355, 52.027, 4.359, 52.026, 4.36, 52.021, 4.366, 52.018, 4.369, 52.015, 4.373, 52.011, 4.378, 52.006, 4.383, 52.005, 4.384, 52.0, 4.387, 51.995, 4.389, 51.987, 4.392, 51.983, 4.394, 51.978, 4.396, 51.973, 4.398, 51.969, 4.401, 51.964, 4.404, 51.957, 4.41, 51.951, 4.415, 51.945, 4.421, 51.941, 4.426, 51.935, 4.433, 51.932, 4.437, 51.931, 4.439, 51.931, 4.441, 51.934, 4.446, 51.935, 4.45, 51.936, 4.457, 51.938, 4.462, 51.94, 4.467, 51.941, 4.48, 51.942, 4.486, 51.943, 4.492, 51.944, 4.495, 51.945, 4.505, 51.947, 4.515, 51.949, 4.527, 51.945, 4.531, 51.945, 4.531, 51.937, 4.535, 51.932, 4.537, 51.925, 4.537, 51.921, 4.535, 51.917, 4.534, 51.911, 4.537, 51.908, 4.539, 51.903, 4.542, 51.898, 4.547, 51.897, 4.55, 51.893, 4.559, 51.887, 4.562, 51.88, 4.565, 51.874, 4.57, 51.868, 4.579, 51.863, 4.588, 51.86, 4.593, 51.852, 4.601, 51.848, 4.604, 51.847, 4.606, 51.836, 4.617, 51.83, 4.622, 51.824, 4.625, 51.82, 4.627, 51.815, 4.631, 51.813, 4.636, 51.812, 4.64, 51.81, 4.646, 51.807, 4.649, 51.805, 4.65, 51.799, 4.649, 51.795, 4.648, 51.789, 4.649, 51.781, 4.65, 51.776, 4.649, 51.767, 4.647, 51.759, 4.645, 51.738, 4.635, 51.731, 4.632, 51.721, 4.633, 51.712, 4.639, 51.703, 4.647, 51.699, 4.649, 51.693, 4.652, 51.688, 4.657, 51.683, 4.661, 51.679, 4.666, 51.672, 4.672, 51.665, 4.677, 51.657, 4.683, 51.651, 4.688, 51.645, 4.691, 51.639, 4.696, 51.635, 4.699, 51.615, 4.713, 51.607, 4.718, 51.602, 4.721, 51.598, 4.722, 51.594, 4.723, 51.587, 4.723, 51.584, 4.723, 51.579, 4.725, 51.577, 4.726, 51.57, 4.729, 51.563, 4.732, 51.559, 4.735, 51.556, 4.736, 51.55, 4.738, 51.545, 4.739, 51.538, 4.74, 51.531, 4.742, 51.525, 4.744, 51.52, 4.745, 51.505, 4.743, 51.498, 4.742, 51.491, 4.738, 51.485, 4.734, 51.474, 4.729, 51.46, 4.723, 51.452, 4.721, 51.445, 4.717, 51.432, 4.711, 51.422, 4.707, 51.413, 4.702, 51.406, 4.695, 51.397, 4.684, 51.392, 4.676, 51.381, 4.662, 51.368, 4.647, 51.362, 4.64, 51.353, 4.63, 51.343, 4.619, 51.337, 4.611, 51.331, 4.601, 51.325, 4.59, 51.319, 4.579, 51.309, 4.557, 51.305, 4.548, 51.302, 4.541, 51.294, 4.526, 51.284, 4.512, 51.276, 4.504, 51.269, 4.494, 51.267, 4.484, 51.267, 4.472, 51.268, 4.463, 51.267, 4.449, 51.265, 4.439, 51.261, 4.432, 51.258, 4.429, 51.253, 4.426, 51.244, 4.425, 51.238, 4.426, 51.236, 4.428, 51.234, 4.433, 51.229, 4.441, 51.223, 4.448, 51.219, 4.45, 51.216, 4.449, 51.213, 4.447, 51.208, 4.442, 51.203, 4.438, 51.197, 4.434, 51.195, 4.433, 51.193, 4.428, 51.191, 4.423, 51.19, 4.416, 51.19, 4.41, 51.192, 4.403, 51.195, 4.393, 51.197, 4.385, 51.198, 4.381, 51.201, 4.376, 51.203, 4.373, 51.208, 4.369, 51.214, 4.361, 51.215, 4.354, 51.214, 4.349, 51.211, 4.344, 51.207, 4.333, 51.203, 4.323, 51.199, 4.316, 51.193, 4.307, 51.187, 4.296, 51.183, 4.286, 51.179, 4.275, 51.171, 4.256, 51.165, 4.245, 51.161, 4.237, 51.157, 4.227, 51.151, 4.207, 51.147, 4.194, 51.143, 4.183, 51.142, 4.178, 51.139, 4.163, 51.137, 4.151, 51.134, 4.138, 51.133, 4.13, 51.132, 4.122, 51.131, 4.115, 51.13, 4.102, 51.129, 4.09, 51.127, 4.082, 51.124, 4.073, 51.119, 4.066, 51.111, 4.058, 51.101, 4.045, 51.093, 4.031, 51.087, 4.02, 51.082, 4.013, 51.077, 3.998, 51.074, 3.985, 51.073, 3.955, 51.071, 3.933, 51.07, 3.917, 51.069, 3.901, 51.067, 3.887, 51.064, 3.876, 51.056, 3.847, 51.053, 3.835, 51.05, 3.823, 51.048, 3.812, 51.046, 3.803, 51.044, 3.791, 51.041, 3.78, 51.039, 3.773, 51.037, 3.763, 51.035, 3.757, 51.033, 3.753, 51.03, 3.746, 51.029, 3.744, 51.027, 3.741, 51.026, 3.738, 51.023, 3.734, 51.021, 3.732, 51.014, 3.727, 51.013, 3.726, 51.012, 3.721, 51.013, 3.714, 51.015, 3.707, 51.019, 3.692, 51.021, 3.683, 51.022, 3.679, 51.026, 3.666, 51.03, 3.651, 51.034, 3.637, 51.037, 3.628, 51.038, 3.622, 51.041, 3.611, 51.046, 3.587, 51.05, 3.572, 51.051, 3.567, 51.053, 3.558, 51.058, 3.533, 51.06, 3.52, 51.064, 3.498, 51.069, 3.476, 51.074, 3.454, 51.076, 3.447, 51.082, 3.432, 51.091, 3.408, 51.1, 3.386, 51.107, 3.369, 51.112, 3.354, 51.118, 3.332, 51.121, 3.322, 51.126, 3.305, 51.132, 3.284, 51.138, 3.262, 51.142, 3.247, 51.146, 3.231, 51.151, 3.219, 51.156, 3.21, 51.16, 3.202, 51.161, 3.197, 51.163, 3.191, 51.164, 3.184, 51.166, 3.172, 51.171, 3.149, 51.175, 3.138, 51.181, 3.122, 51.185, 3.109, 51.188, 3.1, 51.19, 3.092, 51.192, 3.086, 51.193, 3.074, 51.192, 3.069, 51.191, 3.064, 51.186, 3.044, 51.182, 3.027, 51.179, 3.019, 51.174, 3.005, 51.171, 2.989, 51.169, 2.971, 51.166, 2.956, 51.162, 2.944, 51.158, 2.935, 51.153, 2.921, 51.147, 2.9, 51.144, 2.887, 51.142, 2.866, 51.14, 2.854, 51.138, 2.841, 51.136, 2.824, 51.133, 2.812, 51.129, 2.801, 51.125, 2.79, 51.118, 2.768, 51.113, 2.757, 51.11, 2.75, 51.105, 2.744, 51.096, 2.735, 51.089, 2.729, 51.086, 2.724, 51.08, 2.713, 51.077, 2.707, 51.073, 2.7, 51.07, 2.695, 51.062, 2.685, 51.058, 2.675, 51.056, 2.669, 51.056, 2.661, 51.056, 2.657, 51.059, 2.648, 51.061, 2.644, 51.065, 2.633, 51.067, 2.62, 51.068, 2.604, 51.066, 2.592, 51.062, 2.581, 51.057, 2.562, 51.054, 2.552, 51.052, 2.546, 51.048, 2.536, 51.045, 2.53, 51.043, 2.52, 51.042, 2.512, 51.042, 2.504, 51.043, 2.494, 51.044, 2.484, 51.043, 2.474, 51.042, 2.468, 51.04, 2.46, 51.039, 2.452, 51.037, 2.446, 51.033, 2.438, 51.029, 2.43, 51.027, 2.425, 51.024, 2.417, 51.022, 2.413, 51.02, 2.407, 51.017, 2.401, 51.017, 2.399, 51.016, 2.395, 51.014, 2.387, 51.013, 2.382, 51.013, 2.379, 51.013, 2.376, 51.012, 2.373, 51.012, 2.368, 51.012, 2.362, 51.012, 2.353, 51.012, 2.347, 51.011, 2.337, 51.01, 2.331, 51.008, 2.325, 51.007, 2.322, 51.005, 2.314, 51.004, 2.31, 51.003, 2.306, 51.002, 2.299, 50.999, 2.291, 50.997, 2.286, 50.995, 2.28, 50.994, 2.275, 50.993, 2.272, 50.992, 2.268, 50.991, 2.266, 50.991, 2.261, 50.991, 2.254, 50.99, 2.246, 50.989, 2.24, 50.988, 2.236, 50.986, 2.231, 50.982, 2.227, 50.978, 2.224, 50.972, 2.222, 50.968, 2.22, 50.966, 2.218, 50.963, 2.215, 50.961, 2.212, 50.96, 2.207, 50.959, 2.2, 50.959, 2.192, 50.958, 2.184, 50.958, 2.175, 50.958, 2.167, 50.958, 2.161, 50.958, 2.154, 50.956, 2.147, 50.954, 2.141, 50.953, 2.137, 50.951, 2.131, 50.951, 2.127, 50.951, 2.12, 50.951, 2.116, 50.949, 2.109, 50.946, 2.103, 50.944, 2.098, 50.941, 2.087, 50.938, 2.076, 50.937, 2.07, 50.936, 2.062, 50.934, 2.053, 50.934, 2.047, 50.932, 2.036, 50.932, 2.028, 50.931, 2.016, 50.932, 2.009, 50.933, 1.996, 50.934, 1.988, 50.935, 1.979, 50.935, 1.972, 50.936, 1.966, 50.937, 1.955, 50.937, 1.944, 50.937, 1.938, 50.937, 1.927, 50.936, 1.916, 50.936, 1.91, 50.935, 1.906, 50.935, 1.898, 50.933, 1.889, 50.933, 1.886, 50.934, 1.881, 50.936, 1.875, 50.935, 1.871, 50.935, 1.865, 50.936, 1.86, 50.936, 1.857, 50.936, 1.85, 50.936, 1.847, 50.938, 1.84, 50.939, 1.836, 50.941, 1.828, 50.941, 1.821, 50.941, 1.815, 50.94, 1.811, 50.938, 1.806, 50.935, 1.804, 50.93, 1.803, 50.929, 1.803, 50.928, 1.801, 50.927, 1.801, 50.926, 1.803, 50.926, 1.804, 50.928, 1.803, 50.932, 1.804, 50.936, 1.805, 50.938, 1.807, 50.94, 1.811, 50.939, 1.814, 50.939, 1.814);
		start("AMS-Calais");

	}

	public void stopAndRemoveNokiaHereAdapterForDemoRoute() {

		stop("AMS-Calais");
		remove("AMS-Calais");
		System.out.println("Removed job 'AMS-Calais'");
	}

	public void startTflAdapter(String... modeNames) {
		String adapterName = "TransportForLondon";
		TflAdapter adapter = (TflAdapter) create(adapterName, AdapterType.TransportForLondon);

		if (adapter != null) {
			adapter.setModes(modeNames);
			start(adapterName);
		}
	}

	public void stopAndRemoveTflAdapter() {
		String adapterName = "TransportForLondon";
		stop(adapterName);
		remove(adapterName);
	}

	public void startBoschIotAdapter() {
		String adapterName = "BoschIot";
		BoschIotAdapter adapter = (BoschIotAdapter) create(adapterName, AdapterType.BoschIot);

		if (adapter != null) {
			start(adapterName);
		}
	}

	public void stopAndRemoveBoschIotAdapter() {
		String adapterName = "BoschIot";
		stop(adapterName);
		remove(adapterName);
	}

	public void startGoodsTagAdapter() {
		String adapterName = "GoodsTag";
		GoodsTagAdapter adapter = (GoodsTagAdapter) create(adapterName, AdapterType.GoodsTag);

		if (adapter != null) {
			start(adapterName);
		}
	}

	public void stopAndRemoveGoodsTagAdapter() {
		String adapterName = "GoodsTag";
		stop(adapterName);
		remove(adapterName);
	}

}
