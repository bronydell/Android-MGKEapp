package ru.equestriadev.arch;

/**
 * Created by Bronydell on 8/9/16.
 */
public class PairDate {
    private String[] descriptions_nonSun = {
            "8:30 - 9:15\n9:25 - 10:10",
            "10:20 - 11:05\n11:25 - 12:10",
            "12:30 - 13:15\n13:25 - 14:10",
            "14:20 - 15:05\n15:15 - 16:00",
            "16:10 - 16:55\n17:00 - 17:45"
    };

    private String[] descriptions_Sun = {
            "8:30 - 9:50",
            "10:00 - 10:45\n11:00 - 12:45",
            "12:00 - 13:20",
            "13:30 - 14:50"
    };

    public String getSunday(int pair){
        if(pair>descriptions_Sun.length){
            return "Мне тебя жаль";
        }
        else{
            return descriptions_Sun[pair-1];
        }
    }

    public String getAnotherOne(int pair){
        if(pair>descriptions_nonSun.length){
            return "Мне тебя жаль";
        }
        else{
            return descriptions_nonSun[pair-1];
        }
    }
}
