package com.mycompany.mapper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author ajosse
 */
public class DateUtil {

    public static String toStringDate(Date date) {
        return new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(date);
    }
}
