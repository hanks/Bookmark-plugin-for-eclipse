package bookmark.utils;

import java.io.File;

public class PathController {

	public static String conversion(String path) {
		String ret = path;
		String separatore = File.separator;
		String sepAlfa = "/";
		String sepBeta = "\\";
		if (path.lastIndexOf(sepAlfa) != -1 || path.lastIndexOf(sepBeta) != -1) {
			if (path.lastIndexOf(separatore) == -1) {
				// devo switchare
				if (separatore.equals(sepAlfa)) {
					ret = path.replace(sepBeta, sepAlfa);
				} else if (separatore.equals(sepBeta)) {
					ret = path.replace(sepAlfa, sepBeta);
				}
			}
		}
		return ret;
	}

	public static boolean check(String path) {
		boolean ret = false;
		String sepCheck = "";
		String sepAlfa = "/";
		String sepBeta = "\\";
		if (!File.separator.equals(sepAlfa)) {
			sepCheck = sepAlfa;
		} else if (!File.separator.equals(sepBeta)) {
			sepCheck = sepBeta;
		}
		if (!sepCheck.equals("") && path.lastIndexOf(sepCheck) != -1) {
			ret = true;
		}
		return ret;
	}
	
}
