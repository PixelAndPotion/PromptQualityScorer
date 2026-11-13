package com.eduvos.promptqualityscorer.ui;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Renders the model's basic Markdown while preserving the original for export. */
public final class SpecificationFormatter {
    private static final Pattern INLINE = Pattern.compile("\\*\\*(.+?)\\*\\*|`([^`]+)`");
    private SpecificationFormatter() {
    }

    public static CharSequence format(String markdown) {
        SpannableStringBuilder out = new SpannableStringBuilder();
        boolean code = false;
        for (String raw : markdown.split("\n", -1)) {
            String line = raw.trim();
            if (line.startsWith("```")) {
                code = !code;
                out.append('\n');
                continue;
            }
            if (!code && line.matches("[-*_]{3,}")) {
                out.append('\n');
                continue;
            }
            boolean heading = !code && line.matches("#{1,6}\\s+.*");
            boolean bullet = !code && line.matches("[-*+]\\s+.*");
            if (heading && out.length() > 0) {
                out.append('\n');
            }
            String body;
            if (code) {
                body = raw;
            } else if (heading) {
                body = line.replaceFirst("^#{1,6}\\s+", "");
            } else if (bullet) {
                body = line.replaceFirst("^[-*+]\\s+", "");
            } else {
                body = line;
            }
            int start = out.length();
            if (code) {
                out.append(body);
            } else {
                appendInline(out, body);
            }
            int end = out.length();
            out.append('\n');
            if (end > start) {
                if (heading) {
                    out.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    out.setSpan(new RelativeSizeSpan(1.12f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (bullet) {
                    out.setSpan(new BulletSpan(14), start, out.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (code) {
                    out.setSpan(new TypefaceSpan("monospace"), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            if (heading) {
                out.append('\n');
            }
        }
        return out;
    }

    private static void appendInline(SpannableStringBuilder out, String line) {
        Matcher matcher = INLINE.matcher(line);
        int cursor = 0;
        while (matcher.find()) {
            out.append(line, cursor, matcher.start());
            int start = out.length();
            if (matcher.group(1) != null) {
                out.append(matcher.group(1));
                out.setSpan(new StyleSpan(Typeface.BOLD), start, out.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                out.append(matcher.group(2));
                out.setSpan(new TypefaceSpan("monospace"), start, out.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            cursor = matcher.end();
        }
        out.append(line, cursor, line.length());
    }
}
