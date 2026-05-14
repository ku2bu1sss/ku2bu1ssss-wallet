package pl.ku2bu1ssWallet.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String colorize(String message) {
        if (message == null) {
            return null;
        }

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = ChatColor.of("#" + hexCode).toString();
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static String stripColors(String message) {
        if (message == null) {
            return null;
        }

        String result = HEX_PATTERN.matcher(message).replaceAll("");

        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', result));
    }

    public static String formatCurrency(double amount, boolean withDecimals) {
        if (withDecimals) {
            return String.format("%.2f zł", amount);
        } else {
            return String.format("%.0f zł", amount);
        }
    }
}
