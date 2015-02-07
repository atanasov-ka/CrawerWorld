package crawler;

import java.net.MalformedURLException;
import java.net.URL;

public class Main {

	public static void main(String[] args) throws MalformedURLException {
		System.out.println(args.length);
		for (String string : args) {
			System.out.println(string);
		}
		
		URL url;
		String token;
		int maxDepth;
		String encoding;
		int maxSubLinksPerLink;
		
		if (args.length == 1 && args[0].equals("e")) {
			// example
			url = new URL("http://www.fmi.uni-sofia.bg");
			token = "Учебно разписание";
			maxDepth = 10;
			encoding = "UTF-8";
			maxSubLinksPerLink = 6;
			
		} else if (args.length != 5) {
			System.out.println("Required arguments:\n" + 
		"1. URL\n" + 
		"2. token\n" + 
		"3. Max depth levels\n" + 
		"4. Encoding\n" +
		"5. Max sub links per page (0 -unlimited )\n");
			
			System.out.println("You can try the example by excecuting with a single argument 'e'");
			return;
		} else {
			url = new URL(args[0]);
			token = args[1];
			maxDepth = Integer.parseInt(args[2]);
			encoding = args[3];
			maxSubLinksPerLink = Integer.parseInt(args[4]);
		}
			
		System.out.println(Crawer.searchInLinks(url, token, maxDepth, encoding, maxSubLinksPerLink));
	}
}
