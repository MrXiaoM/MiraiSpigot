package top.mrxiaom.qqwhitelist;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

public class Commands implements Listener {
	Plugin main;
	public Commands(Plugin main) {
		this.main = main;
	}

	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		boolean access = sender.isOp();
		if(access) {
			if(args.length >= 1) {
				if(args[0].equalsIgnoreCase("reload")) {
					main.saveDefaultConfig();
					main.reloadConfig();
					return;
				}
				if(args[0].equalsIgnoreCase("on")) {
					main.getConfig().set("enable", true);
					main.saveConfig();
					sender.sendMessage("已开启白名单功能");
					return;
				}
				if(args[0].equalsIgnoreCase("off")) {
					main.getConfig().set("enable", false);
					main.saveConfig();
					sender.sendMessage("已关闭白名单功能");
					return;
				}
				if(args[0].equalsIgnoreCase("login")) {
					if(main.isBotOnline()) { 
						sender.sendMessage(main.message_prefix + main.message_logined);
						return;
					}
					sender.sendMessage(main.message_prefix + main.message_logining.replace("$uid", String.valueOf(main.config_uid)));
					main.login();
					return;
				}
				if(args[0].equalsIgnoreCase("add")) {
					if(args.length == 2) {
						String player = args[1].toLowerCase();
						if(this.isMatchPlayerName(player)) {
							if(main.whitelist.contains(player)) {
								sender.sendMessage(main.message_prefix + main.message_bot_already);
								return;
							}
							main.whitelist.add(player, -1);
							main.whitelist.saveConfig();
							sender.sendMessage(main.message_prefix + "");
						}
						else {
							sender.sendMessage(main.message_prefix + main.message_bot_invalidUsername);
							return;
						}
					}
				}
				if(args[0].equalsIgnoreCase("remove")) {
					if(args.length == 2) {
						String player = args[1].toLowerCase();
						if(main.whitelist.contains(player)) {
							
							main.whitelist.remove(player);
							main.whitelist.saveConfig();
							return;
						}
						else {
							sender.sendMessage(main.message_prefix + main.message_playernotexist);
							return;
						}
					}
				}
				if(args[0].equalsIgnoreCase("destory")) {
					if(main.isBotOnline()) main.bot.close(new Exception("手动销毁机器人"));;
					main.bot = null;
					sender.sendMessage(main.message_prefix + main.message_destoryed);
					return;
				}
			}
			sender.sendMessage(
					  "§7[§f白名单§7] §e帮助:\n"
					+ "§b/qqwhitelist reload §f- §a重载插件并重新登录机器人\n"
					+ "§b/qqwhitelist login §f- §a登录机器人\n"
					+ "§b/qqwhitelist add [玩家] §f- §a添加某玩家为白名单\n"
					+ "§b/qqwhitelist remove [玩家] §f- §a移除某玩家的白名单\n"
					+ "§b/qqwhitelist destory §f- §a销毁机器人(登出并销毁实例)\n"
					+ "§a如果你安装了该插件的附属注册了Listener然后热卸载了\n"
					+ "§a你可能需要使用该命令销毁实例再重新登录"
					);
			return;
		}
		// 传回: Unknown command. Type "/help" for help.
		sender.sendMessage(main.getServer().spigot().getConfig().getConfigurationSection("messages").getString("unknown-command"));
	}
	
	public boolean isMatchPlayerName(String player) {
		Pattern p = Pattern.compile(main.config_nameRegex);
        Matcher m = p.matcher(player);
		return m.matches();
	}
}
