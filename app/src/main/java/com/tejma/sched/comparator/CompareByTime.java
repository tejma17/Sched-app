package com.tejma.sched.comparator;

import com.tejma.sched.POJO.Lecture;

import java.util.Calendar;
import java.util.Comparator;

public class CompareByTime implements Comparator<Lecture>
{
    public int compare(Lecture a, Lecture b)
    {
        String[] lecTimeDivideda = a.getTime().split(":");
        int houra = Integer.parseInt(lecTimeDivideda[0]);
        int minutesa =  Integer.parseInt(lecTimeDivideda[1]);
        String[] lecTimeDividedb = b.getTime().split(":");
        int hourb = Integer.parseInt(lecTimeDividedb[0]);
        int minutesb =  Integer.parseInt(lecTimeDividedb[1]);
        Calendar timea = Calendar.getInstance();
        timea.set(Calendar.HOUR_OF_DAY, houra);
        timea.set(Calendar.MINUTE, minutesa);
        timea.set(Calendar.SECOND, 0);
        Calendar timeb = Calendar.getInstance();
        timeb.set(Calendar.HOUR_OF_DAY, hourb);
        timeb.set(Calendar.MINUTE, minutesb);
        timeb.set(Calendar.SECOND, 0);
        return Long.compare(timea.getTimeInMillis(), timeb.getTimeInMillis());
    }
}