package com.predicto.news;

import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.net.URL;
import java.util.*;

@Service
public class RssFeedService {

    private final List<RssSource> sources = List.of(
        new RssSource("HLTV", "https://www.hltv.org/rss/news", "CS2", "NEWS"),
        new RssSource("Liquipedia CS2", "https://liquipedia.net/counterstrike/index.php?title=Special:RecentChanges&feed=rss", "CS2", "NEWS"),
        new RssSource("LoL Esports", "https://lolesports.com/en-US/rss", "LOL", "NEWS"),
        new RssSource("F1", "https://www.formula1.com/content/fom-website/en/latest/all.xml", "F1", "NEWS"),
        new RssSource("Dot Esports LoL", "https://dotesports.com/league-of-legends/feed", "LOL", "NEWS"),
        new RssSource("Dot Esports CS2", "https://dotesports.com/counter-strike/feed", "CS2", "NEWS")
    );

    public List<RssItem> fetchAll() {
        List<RssItem> items = new ArrayList<>();
        for (RssSource source : sources) {
            try {
                items.addAll(fetch(source));
            } catch (Exception e) {
                // skip failed sources
            }
        }
        return items;
    }

    private List<RssItem> fetch(RssSource source) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new URL(source.url()).openStream());
        NodeList items = doc.getElementsByTagName("item");
        List<RssItem> result = new ArrayList<>();
        for (int i = 0; i < Math.min(items.getLength(), 5); i++) {
            Element item = (Element) items.item(i);
            String title = getTag(item, "title");
            String link = getTag(item, "link");
            String description = getTag(item, "description");
            String pubDate = getTag(item, "pubDate");
            result.add(new RssItem(title, link, description, pubDate, source));
        }
        return result;
    }

    private String getTag(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        return nl.getLength() > 0 ? nl.item(0).getTextContent().trim() : "";
    }
}
