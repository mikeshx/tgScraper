package com.tgscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class tgscraper {
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println("usage: google_url n_pages");
            System.exit(0);
        }

        int          nPages   = Integer.parseInt(args[1]);
        List<String> linkList = getLinks(nPages, args);

        cleanList(linkList);
    }

    public static List<String> getLinks(int nPages, String[] args) throws IOException {

        List<String> linkList = new ArrayList<>();
        String       newLine  = System.getProperty("line.separator"), newURL;

        for (int i = 1; i <= nPages; i++) {
            System.out.println("Page " + i + newLine);

            newURL = args[0].concat("&start=") + i + '0';
            System.out.println("Parsing " + newURL + newLine);

            Document doc = Jsoup.connect(newURL)
                    .userAgent("\tMozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20100101 Firefox/10.0")
                    .timeout(3000).get();

            // Extract the page links located at the a tag inside the h3
            for (Element pageLinks : doc.select("h3.r a")) {

                String title = pageLinks.text();
                String pageLink = pageLinks.attr("href");
                boolean found = false;

                System.out.println("Title: " + title + " ---> " + pageLink);

                // Open the page url to get a tg link
                try {
                    Elements elements = null;

                    // Connect to a google url
                    Document docLink = Jsoup.connect(pageLink)
                            .userAgent("\tMozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20100101 Firefox/10.0")
                            .ignoreHttpErrors(true)
                            .timeout(60000).get();

                    // If we find a youtube link, we call a function to parse it
                    if (pageLink.startsWith("https://www.youtube.com/")) {
                        sites.getYTLinks(docLink, linkList);
                    } else {

                        // Select all the elements of the document
                        elements = docLink.body().select("*");

                        // Get the text of each element and
                        // Check if there is a valid tg link
                        for (Element element : elements) {
                            String cleanString;

                            if (element.ownText().matches("(.*)https://t.me/joinchat/AAAAA(.*)") ||
                                    element.ownText().matches("(.*)https://telegram.me/joinchat/(.*)") ||
                                    element.ownText().matches("(.*)https://t.me/joinchat/(.*)") && !found) {

                                // Remove spaces from the results and add it to a list
                                cleanString = element.ownText().replaceAll("\\s+","");
                                linkList.add(cleanString);

                                System.out.println("Link found: " + cleanString + newLine);
                                found = true;
                            }
                        }
                    }

                } catch (IOException e) {
                    System.out.println("Error: " + e);
                }
            }
            System.out.println("linkList size: " + linkList.size() +newLine);
        }

        // Returns the 'dirty' list of links that will be cleaned later
        return linkList;
    }

    public static void cleanList(List<String> linkList) {

        List<String> cleanList = new ArrayList<String>();
        Set<String>  hs        = new HashSet<>();
        String       tme       = "https://t.me/joinchat/";
        String       tme_2     = "https://telegram.me/joinchat/";

        // Clean the unnecessary output from the previous list
        for (int i = 0; i < linkList.size(); i++) {

            Pattern pattern = Pattern.compile(tme);
            Matcher matcher = pattern.matcher(linkList.get(i));

            Pattern pattern_2 = Pattern.compile(tme_2);
            Matcher matcher_2 = pattern_2.matcher(linkList.get(i));

            while (matcher.find()) {
                if (matcher.end() + 22 <= linkList.get(i).length()) {
                    cleanList.add(linkList.get(i).substring(matcher.start(), matcher.end() + 22));
                }
            }

            while (matcher_2.find()) {
                if (matcher_2.end() + 22 <= linkList.get(i).length()) {
                    cleanList.add(linkList.get(i).substring(matcher_2.start(), matcher_2.end() + 22));
                }
            }
        }

        // Remove duplicates from the list
        // By adding the contents to a Set
        hs.addAll(cleanList);
        cleanList.clear();
        cleanList.addAll(hs);

        for (int j = 0; j < cleanList.size(); j++) {
            System.out.println(cleanList.get(j));
        }
        System.out.println("cleanList size: " + cleanList.size());
    }
}