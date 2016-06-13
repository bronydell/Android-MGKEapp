package ru.equestriadev.arch;

/**
 * Created by Bronydell on 6/13/16.
 */
public class Month {
    static String[] labels = {
            "Январь",
            "Фераль",
            "Март",
            "Апрель",
            "Май",
            "Июнь",
            "Июль",
            "Август",
            "Сентябрь",
            "Октябрь",
            "Ноябрь",
            "Декабрь"
    };

    public static String getMouthNyNumber(int num)
    {
        return labels[num-1];
    }
}
