package TowerSlug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TextInit {
	static ArrayList<int[]> mapGround;
	static ArrayList<int[]> mapTowers;

	public static void readMap() throws IOException {

		mapGround = new ArrayList<int[]>();
		mapTowers = new ArrayList<int[]>();

		BufferedReader inputStream = null;

		try {
			InputStream is = TextInit.class.getResourceAsStream("res/MapGround.txt");
			inputStream = new BufferedReader(new InputStreamReader(is));

			String l;
			while ((l = inputStream.readLine()) != null) {
				String delims = "[ ]+";
				String[] tokens = l.split(delims);
				int[] e = new int[tokens.length];
				for (int i = 0; i < tokens.length; i++) {
					e[i] = Integer.parseInt(tokens[i]);
				}
				mapGround.add(e);
				
				
			}
			inputStream = null;
			is = TextInit.class.getResourceAsStream("res/MapTowers.txt");
			inputStream = new BufferedReader(new InputStreamReader(is));

			l = null;
			while ((l = inputStream.readLine()) != null) {
				String delims = "[ ]+";
				String[] tokens = l.split(delims);
				int[] e = new int[tokens.length];
				for (int i = 0; i < tokens.length; i++) {
					e[i] = Integer.parseInt(tokens[i]);
				}
				mapTowers.add(e);
				
				
			}
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	public static ArrayList<int[]> getmapGround() {
		return mapGround;
	}
	public static ArrayList<int[]> getmapTowers() {
		return mapTowers;
	}
}
