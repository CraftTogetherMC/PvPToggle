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
        switch (entityName) {
            case "Cat":
                return "Katze";
            case "Donkey":
                return "Esel";
            case "Horse":
                return "Pferd";
            case "Llama":
                return "Lama";
            case "Mule":
                return "Maultier";
            case "Parrot":
                return "Papagei";
            case "Zombie Horse":
                return "Zombie Pferd";
            case "Skeleton Horse":
                return "Skelett Pferd";
        }
        return entityName;
    }
}
