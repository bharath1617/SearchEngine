package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

public class Crawler {
    public static HashSet<String> urlHash;
    public int MAX_DEPTH = 2;
    public static Connection connection = null;
    public Crawler() {
        //Initialize urlHash
        urlHash = new HashSet<>();
        connection = DatabaseConnection.getConnection();
    }
    public static void getPageTextAndLinks(String url, int depth){
        // if urlHash does not contains url
        if(!urlHash.contains(url)) {
            if (urlHash.add(url)) {
                System.out.println(url);
            }
            try {
                //connecting to the webpage and get webpage as document object
                Document document = Jsoup.connect(url).timeout(5000).get();
                // get text inside that document/webpage
                String text = document.text().length()>1000?document.text().substring(0, 999): document.text();
                //get title of webpage
                String title = document.title();
                System.out.println(title+"\n"+text);
                // prepare an insert command
                PreparedStatement preparedStatement = connection.prepareStatement("Insert into pages values(?, ?, ?)");

                preparedStatement.setString(1, title);
                preparedStatement.setString(2, url);
                preparedStatement.setString(3, text);
                preparedStatement.executeUpdate();
                // increasing the depth
                depth++;
                 // limiting the depth
                if(depth == 2){
                    return;
                }
                // get all links/anchor tage that are present in the webpage
                Elements availableLinksOnPage = document.select("a[href]");
                // for every link call this is function recursively
                for(Element currentLink: availableLinksOnPage) {
                      getPageTextAndLinks(currentLink.attr("abs:href"), depth);
               }
            } catch(SQLException | IOException ioException){
                ioException.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        Crawler.getPageTextAndLinks("https://www.javatpoint.com", 0);
        Crawler.getPageTextAndLinks("https://www.geeksforgeeks.org",0);
    }
}