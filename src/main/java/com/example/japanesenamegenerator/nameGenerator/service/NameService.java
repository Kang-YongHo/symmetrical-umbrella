package com.example.japanesenamegenerator.nameGenerator.service;

import com.example.japanesenamegenerator.nameGenerator.responses.LastNameResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

@Service
public class NameService {


    public LastNameResponse generateName(String surName, String firstName, String gender) {

        List<String> firstNameList = Arrays.stream(firstName.split("")).toList();
        List<String> firstNameUrls = new ArrayList<>();
        String surNameUrl = String.format("https://japanese-names.info/last-names/search-result/freeword-%s/", surName);

        String genderParam = gender == null ? "" : String.format("gender-%s_", gender);
        firstNameList.forEach(firstNameString ->
                firstNameUrls.add(
                        String.format(
                                "https://japanese-names.info/first-names/search-result/%sfreeword-%s/",
                                genderParam, firstNameString)
                )
        );

        List<Element> firstNames = new ArrayList<>();
        firstNameUrls.forEach(
                url -> {
                    try {
                        Document doc = Jsoup.connect(url).get();
                        Elements names = doc.select(".name_summary");
                        List<Element> stringList = names.stream().toList();
                        firstNames.addAll(stringList);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        Random random = new Random();
        Element randomElement = firstNames.get(random.nextInt(firstNames.size()));
        String firstNamePronounce = randomElement.siblingElements().select("em").text();
        String japaneseFirstName = Objects.requireNonNull(randomElement.selectFirst("strong")).text();

        String japaneseSurName = "";
        String surNamePronounce = "";
        int households = 0;
        try {
            Document doc = Jsoup.connect(surNameUrl).get();
            Element names = doc.selectFirst(".name_summary");
            Element siblingElement = Objects.requireNonNull(names).siblingElements().first();

            String householdsText = names.text();
            String extractedNumber = householdsText.split("aprx\\.")[1].trim().replace(",", "");
            households = Integer.parseInt(extractedNumber);
            japaneseSurName = names.selectFirst("strong").text();
            surNamePronounce = siblingElement.select("em").text();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new LastNameResponse(japaneseSurName, surNamePronounce, japaneseFirstName, firstNamePronounce, households);
    }

}
