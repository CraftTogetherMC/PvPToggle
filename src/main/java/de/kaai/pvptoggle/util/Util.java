package de.kaai.pvptoggle.util;

import org.bukkit.ChatColor;

public class Util {
	
	//text <player> text text text
		public static String format (String message, String playerName) {
			String a = message.replace("<player>", playerName);
			return ChatColor.translateAlternateColorCodes('&', a);
		}
	
	//text <player> <target> -> text text text
	public static String format (String message, String playerName, String targetName) {
		String a = message.replace("<player>", playerName);
		String b = a.replace("<target>", targetName);
		return ChatColor.translateAlternateColorCodes('&', b);
	}
	
	//text <player> <target> <pet> -> text text text
	public static String format (String message, String playerName, String targetName, String petName) {
		String a = message.replace("<player>", playerName);
		String b = a.replace("<target>", targetName);
		String c = b.replace("<pet>", petName);
		return ChatColor.translateAlternateColorCodes('&', c);
	}
	
	//NoParameter
	public static String format (String message) {
	return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public static String translater (String entityName) {
		
		String x = "";
		switch (entityName) {
		case "Cat":
			x = "Katze";
			break;
		case "Donkey":
			x = "Esel";
			break;
		case "Horse":
			x = "Pferd";
			break;
		case "Llama":
			x = "Lama";
			break;
		case "Mule":
			x = "Maultier";
			break;
		case "Parrot":
			x = "Papagei";
			break;
		case "Zombie Horse":
			x = "Zombie Pferd";
			break;
		case "Skeleton Horse":
			x = "Skelett Pferd";
			break;

		default:
			x = entityName;
			break;
		}
		
		
		return x;
	}
	
}
