package io.github.volyx;

import com.github.jknack.handlebars.Options;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TemplateHelpers {
    static final DateTimeFormatter MMMddyyyyFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public static CharSequence dateFormat(String dateString, Options options) {
        LocalDateTime date = LocalDateTime.parse(dateString);
        return MMMddyyyyFmt.format(date);
    }
}
