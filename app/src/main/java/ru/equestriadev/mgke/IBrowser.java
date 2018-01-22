package ru.equestriadev.mgke;

import ru.equestriadev.arch.Day;

/**
 * Created by Bronydell on 1/22/18.
 */

public interface IBrowser {
    void setAdapter(final Day day);

    void setFragmentName(String name);

    void openCalendar();

}
