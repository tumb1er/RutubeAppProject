package ru.rutube.RutubeAPI.models;

import ru.rutube.RutubeAPI.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 25.05.13
 * Time: 11:34
 * To change this template use File | Settings | File Templates.
 */
public class Category {
    private static final Map<String, Integer> MAP;
    static {
        MAP = new HashMap<String, Integer>();
        MAP.put("auto_moto", 2);
        MAP.put("films_series", 5);
        MAP.put("music", 6);
        MAP.put("adult_multfilms", 7);
        MAP.put("news_events", 8);
        MAP.put("animals", 10);
        MAP.put("travel_nature", 11);
        MAP.put("private_ads", 13);
        MAP.put("human_society", 15);
        MAP.put("sport", 16);
        MAP.put("education", 17);
        MAP.put("erotics", 18);
        MAP.put("humor", 19);
        MAP.put("pc_games", 22);
        MAP.put("hobby", 35);
        MAP.put("anime", 41);
        MAP.put("kids_multfilms", 42);
        MAP.put("tvshow", 43);
        MAP.put("health_beauty", 44);
        MAP.put("tech_internet", 45);
    }
    public static int getCategoryId(String value) {
        return MAP.get(value);
    }
}
