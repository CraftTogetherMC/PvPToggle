package de.crafttogether.pvptoggle.util;

import org.bukkit.ChatColor;

public class Util {

    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String format(String message, String playerName) {
        String a = message.replace("<player>", playerName);
        return ChatColor.translateAlternateColorCodes('&', a);
    }

    public static String format(String message, String playerName, String targetName) {
        String a = message.replace("<player>", playerName);
        String b = a.replace("<target>", targetName);
        return ChatColor.translateAlternateColorCodes('&', b);
    }

    public static String format(String message, String playerName, String targetName, String petName) {
        String a = message.replace("<player>", playerName);
        String b = a.replace("<target>", targetName);
        String c = b.replace("<pet>", petName);
        return ChatColor.translateAlternateColorCodes('&', c);
    }

    public static String translator(String entityName) {
        return switch (entityName) {
            case "Cat" -> "Katze";
            case "Donkey" -> "Esel";
            case "Horse" -> "Pferd";
            case "Llama" -> "Lama";
            case "Mule" -> "Maultier";
            case "Parrot" -> "Papagei";
            case "Zombie Horse" -> "Zombie Pferd";
            case "Skeleton Horse" -> "Skelett Pferd";
            default -> entityName;
        };
    }

    public static String format(String message, String playerName, long cooldownTimeMillis) {


        String cooldown;

        String a = message.replace("<player>", playerName);
        String b = a.replace("<cooldown>", formatCooldown(cooldownTimeMillis));
        return ChatColor.translateAlternateColorCodes('&', b);
    }

    private static String formatCooldown(long CooldownMilli) {
        long CooldownSec = CooldownMilli/1000;

        if (CooldownSec <= 1) {
            return "1 Sekunde";
        } else if (CooldownSec <= 59) {
            return CooldownSec + " Sekunden";
        } else if (CooldownSec <= 60+59) {
            return "1 Minute";
        } else if (CooldownSec <= 60*59+59) {
            return CooldownSec/60 + " Minuten";
        } else if (CooldownSec <= 60*60+60*59+59) {
            return "1 Stunde";
        }
        return CooldownSec/60/60 + " Stunden";
    }

}
