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

    static String[] daylabelsmon = {
            "Понедельник",
            "Вторник",
            "Среда",
            "Четверг",
            "Пятница",
            "Суббота",
            "Воскресение"
    };

    static String[] daylabels = {
            "Воскресение",
            "Понедельник",
            "Вторник",
            "Среда",
            "Четверг",
            "Пятница",
            "Суббота"
    };

    static String[] short_daylabels = {
            "Вс",
            "Пн",
            "Вт",
            "Ср",
            "Чт",
            "Пт",
            "Сб"
    };
    public static String getMouthNyNumber(int num)
    {
        return labels[num];
    }

    public static String getDatNyNumber(int num)
    {
        return daylabels[num-1];
    }

    public static String getDatNyNumberMon(int num)
    {
        return daylabelsmon[num-1];
    }

    public static String getShortDate(int num)
    {
        return short_daylabels[num-1];
    }


}
