package main.o;

public class MyLevenshtein {

	public static void main(String[] args) {/*
		// 瑕佹瘮杈冪殑涓や釜瀛楃涓�
		String str1 = "榛斿崡瑗垮窞";
		String str2 = "榛斿崡甯冧緷鏃忚嫍鏃忚嚜娌诲窞";
		levenshtein(str1, str2);*/

		String[] s = { "a", "b" };
		Object o = s;
		String[] a=(String[])o;
		System.out.println(a.length);
	}

	/**
	 * 銆�銆�DNA鍒嗘瀽 銆�銆�鎷煎瓧妫�鏌� 銆�銆�璇煶杈ㄨ瘑 銆�銆�鎶勮渚︽祴
	 * 
	 * @createTime 2012-1-12
	 */
	public static void levenshtein(String str1, String str2) {
		// 璁＄畻涓や釜瀛楃涓茬殑闀垮害銆�
		int len1 = str1.length();
		int len2 = str2.length();
		// 寤虹珛涓婇潰璇寸殑鏁扮粍锛屾瘮瀛楃闀垮害澶т竴涓┖闂�
		int[][] dif = new int[len1 + 1][len2 + 1];
		// 璧嬪垵鍊硷紝姝ラB銆�
		for (int a = 0; a <= len1; a++) {
			dif[a][0] = a;
		}
		for (int a = 0; a <= len2; a++) {
			dif[0][a] = a;
		}
		// 璁＄畻涓や釜瀛楃鏄惁涓�鏍凤紝璁＄畻宸︿笂鐨勫��
		int temp;
		for (int i = 1; i <= len1; i++) {
			for (int j = 1; j <= len2; j++) {
				if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
					temp = 0;
				} else {
					temp = 1;
				}
				// 鍙栦笁涓�间腑鏈�灏忕殑
				dif[i][j] = min(dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1, dif[i - 1][j] + 1);
			}
		}
		System.out.println("");
		// 鍙栨暟缁勫彸涓嬭鐨勫�硷紝鍚屾牱涓嶅悓浣嶇疆浠ｈ〃涓嶅悓瀛楃涓茬殑姣旇緝
		System.out.println("宸紓姝ラ锛�" + dif[len1][len2]);
		// 璁＄畻鐩镐技搴�
		float similarity = 1 - (float) dif[len1][len2] / Math.max(str1.length(), str2.length());
		System.out.println("鐩镐技搴︼細" + similarity);
	}

	// 寰楀埌鏈�灏忓��
	private static int min(int... is) {
		int min = Integer.MAX_VALUE;
		for (int i : is) {
			if (min > i) {
				min = i;
			}
		}
		return min;
	}

}
