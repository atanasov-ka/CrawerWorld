package crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.AbstractMap.SimpleEntry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Exposes a method for finding a token in a web site
 * @author Angelo Atanasov
 *
 */
public class Crawer {
	
	private static HashSet<String> hashSetLinks = new HashSet<>();
	private static Queue<SimpleEntry<URL, Integer>> queuedLinks = new LinkedList<AbstractMap.SimpleEntry<URL, Integer>>();
	
	/**
	 * Returns the url where the token was found
	 * @param url from where to start
	 * @param token the string that we need to find
	 * @param maxDepth depth
	 * @param encoding ex: CP1251, UTF-8, etc (See the meta of the page)
	 * @return the link where the token was found or NULL if no token was found
	 */
	public static String searchInLinks(URL url, String token, int maxDepth, String encoding, int maxLinksPerLevel)
	{
		System.out.println(String.format("Url:"+ url + " token:" + token + " maxDepth:" + maxDepth + " encoding:" + encoding + " maxLinksPerLevel:" + maxLinksPerLevel));
		System.out.println("SEQ 1. INIT CRAWLER");
		
		if (maxDepth < 0)
			return null;
		
		hashSetLinks.add(url.toString());
		queuedLinks.add(new SimpleEntry<URL, Integer>(url, 1));
		
		String dataForAnalyze;
		while(!queuedLinks.isEmpty())
		{
			SimpleEntry<URL, Integer> pair = queuedLinks.poll();
			if (pair.getValue() < 0)
				return null;
			
			String link = pair.getKey().toString();
			
			// Do now craw out of this domain!
			if (!pair.getKey().getHost().equals(url.getHost())) {
				continue;
			}
			
			try {
				System.out.println("SEQ 2. GETTING RESOURCE FROM DEPTH: " + pair.getValue() + " AND URL: " + link);
				dataForAnalyze = getUrlSource(link, encoding);
			} catch (IOException e1) {
				System.err.println(e1.toString());
				continue;
			}
			
			if (dataForAnalyze.contains(token))
			{
				return "----------- FOUND -----------" + "\n\n" +
			"<result><link>" + link + "</link><depth>" + pair.getValue() + "</depth></result>";
			}
			
			System.out.println("SEQ 3. GETTING HARVEST");
			List<String> harvestedLinks = getAllLinks(dataForAnalyze);
			
			// no harvest to enqueue
			if (harvestedLinks.isEmpty()) 
				continue;
			
			int countHarvest = 0;
			for (String harvest : harvestedLinks) {
				// Make the link valid
				if (!harvest.contains("http://") && !harvest.contains("https://")) {
					if (!harvest.isEmpty() && harvest.charAt(0) != '/') {
						harvest = "/" + harvest;
					}
					harvest = url.getProtocol() + "://" + url.getHost() + harvest;
				}
				
				// loop protection (already exists in the queue)
				if (hashSetLinks.contains(harvest)){
					continue;
				} else {
					hashSetLinks.add(harvest);
				}					
				
				URL goodLink;
				try {
					goodLink = new URL(harvest);
				} catch (MalformedURLException e) {
					// Some format problem 
					System.err.println(e.toString());
					continue;
				}
				
				// truncating the rest of the potential links
				// 0 - unlimited
				countHarvest++;
				if (maxLinksPerLevel != 0 && countHarvest >= maxLinksPerLevel) {
					System.out.println("SEQ 4. TRUNCATING HARVEST LIST");
					break;
				}
				
				queuedLinks.add(new SimpleEntry<URL, Integer>(goodLink, pair.getValue() + 1));
			}
		}
		
		return "<not_found/>";
	}
	
	/**
	 * Downloads the web page
	 * @param url
	 * @param encoding
	 * @return the requested web page
	 * @throws IOException
	 */
	private static String getUrlSource(String url, String encoding) throws IOException {
        URL urlObj = new URL(url);
        URLConnection yc = urlObj.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), encoding));
        String inputLine;
        StringBuilder pageContent = new StringBuilder();
        while ((inputLine = in.readLine()) != null)
            pageContent.append(inputLine);
        in.close();

        return pageContent.toString();
    }
	
	/**
	 * Returns all links from href elements, except the javascript
	 * @param content html text
	 * @return
	 */
	private static List<String> getAllLinks(String content) {
        ArrayList<String> resultList = new ArrayList<>();
        String regex = "<a.*?href=\"((?!javascript).*?)\".*?>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            resultList.add(matcher.group(1));
        }
        return resultList;
    }
	
}
