package ru.equestriadev.arch;

/**
 * Created by Bronydell on 8/9/16.
 */
public class PairDate {
    private String[] descriptions_nonSat = {
            "8:30 - 9:15\n9:25 - 10:10",
            "10:20 - 11:05\n11:25 - 12:10",
            "12:30 - 13:15\n13:25 - 14:10",
            "14:20 - 15:05\n15:15 - 16:00",
            "16:10 - 16:55\n17:00 - 17:45"
    };

    private String[] descriptions_Sat_kn = {
            "9:00 - 10:20",
            "10:30 - 11:15\n11:35 - 12:20",
            "12:35 - 13:55",
            "14:05 - 15:25"
    };

    private String[] descriptions_nonSat_kn = {
            "9:00 - 9:45\n9:55 - 10:40",
            "11:00 - 11:45\n12:05 - 12:50",
            "13:10 - 13:55\n14:05 - 14:50",
            "15:00 - 15:45\n15:55 - 16:40",
            "16:50 - 17:35\n17:50 - 17:35(Не точно)"
    };

    private String[] descriptions_Sat = {
            "8:30 - 9:50",
            "10:00 - 10:45\n11:00 - 11:45",
            "12:00 - 13:20",
            "13:30 - 14:50"
    };

    public String getSaturday(int pair, boolean isKn) {
        if(isKn)
            if (pair > descriptions_Sat_kn.length) {
                return "Мне тебя жаль";
            } else {
                return descriptions_Sat_kn[pair - 1];
            }
        else
        if (pair > descriptions_Sat.length) {
            return "Мне тебя жаль";
        } else {
            return descriptions_Sat[pair - 1];
        }
    }

    public String getAnotherOne(int pair, boolean isKn) {
        if(isKn)
            if (pair > descriptions_nonSat_kn.length) {
                return "Мне тебя жаль";
            } else {
                return descriptions_nonSat_kn[pair - 1];
            }
        else
            if (pair > descriptions_nonSat.length) {
                return "Мне тебя жаль";
            } else {
                return descriptions_nonSat[pair - 1];
            }

    }
}
