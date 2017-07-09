import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class tgscraper {
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println("usage: google_url | n_pages");
            System.exit(0);
        }

        int          nPages   = Integer.parseInt(args[1]);
        List<String> linkList = getLinks(nPages, args);

        linkList = cleanList(linkList);
        writeList(linkList);
    }

    public static String newLine  = System.getProperty("line.separator");

    /* Get the links */
    public static List<String> getLinks(int nPages, String[] args) throws IOException {

        List<String> linkList = new ArrayList<>();
        String       newURL;

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
        }
        // Returns the 'dirty' list of links that will be cleaned later
        return linkList;
    }

    /* Clean the list of links */
    public static List<String> cleanList(List<String> linkList) {

        List<String> cleanList = new ArrayList<String>();
        Set<String>  hs        = new HashSet<>();
        String       tme       = "https://t.me/joinchat/";
        String       tme_2     = "https://telegram.me/joinchat/";

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

        // Remove duplicates from the list by adding the contents to a set
        hs.addAll(cleanList);
        cleanList.clear();
        cleanList.addAll(hs);

        for (int j = 0; j < cleanList.size(); j++) {
            System.out.println(j+1 + ": " + cleanList.get(j));
        }
        System.out.println("Found " + cleanList.size() + " links" +newLine);

        return cleanList;
    }

    /* Write the list of links to a file */
    public static void writeList (List<String> linkList) throws IOException {

        Properties   prop      = new Properties();
        InputStream  input     = null;

        String       pathName  = "";
        String       fileName  = "tg_links.txt";

        // Get the path name from config.properties
        try {
            input = new FileInputStream("config.properties");
            prop.load(input);
            pathName = prop.getProperty("path_name");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Writing links to: " + pathName.concat(fileName) + " ..." +newLine);
        File path = new File(pathName);

        // Write the list of links to a text file
        if (path.exists() && path.isDirectory()) {

            FileWriter writer = new FileWriter(pathName.concat(fileName));
            for(String str: linkList) {
                writer.write(str + System.getProperty("line.separator"));
            }
            writer.close();
            System.out.println("Done.");

        } else {
            System.out.println("The specified directory does not exist");
            System.exit(1);
        }
    }
}