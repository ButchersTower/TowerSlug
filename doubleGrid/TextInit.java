package TowerSlug.doubleGrid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TextInit {
	static ArrayList<int[]> map1;

	public static void readMap() throws IOException {

		map1 = new ArrayList<int[]>();

		BufferedReader inputStream = null;
		System.out.println("this is called");
		try {
			InputStream is = TextInit.class.getResourceAsStream("MapSmall1.txt");
			inputStream = new BufferedReader(new InputStreamReader(is));

			System.out.println("found");

			String l;
			while ((l = inputStream.readLine()) != null) {
				System.out.println("hitt");
				String delims = "[ ]+";
				String[] tokens = l.split(delims);
				int[] e = new int[tokens.length];
				for (int i = 0; i < tokens.length; i++) {
					e[i] = Integer.parseInt(tokens[i]);
				}
				map1.add(e);
			}
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	public static ArrayList<int[]> getmap1() {
		return map1;
	}
}
