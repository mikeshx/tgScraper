import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class sites {

    public static void getYTLinks(Document doc, List<String> linkList) throws IOException {
        Elements elements = doc.select("#watch-description-text a");

        for (Element link : elements) {

            if (link.absUrl("href").startsWith("https://t.me/joinchat/") ||
                    link.absUrl("href").startsWith("https://telegram.me/joinchat/")) {

                String absUrl = link.absUrl("href");
                System.out.println(absUrl);
                linkList.add(absUrl);
            }
        }
    }
}