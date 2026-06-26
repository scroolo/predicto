package com.predicto.news;

public record RssItem(String title, String link, String description, String pubDate, RssSource source) {}
