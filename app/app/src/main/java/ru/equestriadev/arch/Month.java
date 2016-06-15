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

    static String[] daylabels = {
            "Понедельник",
            "Вторник",
            "Среда",
            "Четверг",
            "Пятница",
            "Суббота",
            "Воскресенье"
    };

    public static String getMouthNyNumber(int num)
    {
        return labels[num-1];
    }

    public static String getDatNyNumber(int num)
    {
        return daylabels[num-1];
    }
}
