package ru.equestriadev.parsing;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Bronydell on 8/10/16.
 */
public class Experiments {

    public static String ParseSubGroups(String lesson, String rooms) {
        lesson.trim();
        String numbers = lesson.replaceAll("[^-?0-9]+", " ");
        List<String> search = Arrays.asList(numbers.trim().split(" "));
        if (rooms.trim().split(" ").length == search.size()) {
            String result = "";
            for (int i = 1; i < search.size(); i++) {
                //Щас магия крч
                result += lesson.split(search.get(i))[0] + " в кабинете: <b>" +
                        rooms.trim().split(" ")[i - 1] + "</b><br>";
                lesson = lesson.substring(lesson.indexOf(search.get(i)));
            }
            result += lesson + " в кабинете: <b>" +
                    rooms.trim().split(" ")[rooms.trim().split(" ").length - 1] + "</b>";
            return result;
        } else {
            return lesson + " в кабинете(кабинетах): <b>" + rooms + "</b>";
        }
    }

    public static boolean isKn(String rooms){
        return rooms.startsWith("Кн");
    }

}
