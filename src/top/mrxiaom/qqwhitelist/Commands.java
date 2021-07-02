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
					sender.sendMessage("�ѿ�������������");
					return;
				}
				if(args[0].equalsIgnoreCase("off")) {
					main.getConfig().set("enable", false);
					main.saveConfig();
					sender.sendMessage("�ѹرհ���������");
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
					if(main.isBotOnline()) main.bot.close(new Exception("�ֶ����ٻ�����"));;
					main.bot = null;
					sender.sendMessage(main.message_prefix + main.message_destoryed);
					return;
				}
			}
			sender.sendMessage(
					  "��7[��f��������7] ��e����:\n"
					+ "��b/qqwhitelist reload ��f- ��a���ز�������µ�¼������\n"
					+ "��b/qqwhitelist login ��f- ��a��¼������\n"
					+ "��b/qqwhitelist add [���] ��f- ��a���ĳ���Ϊ������\n"
					+ "��b/qqwhitelist remove [���] ��f- ��a�Ƴ�ĳ��ҵİ�����\n"
					+ "��b/qqwhitelist destory ��f- ��a���ٻ�����(�ǳ�������ʵ��)\n"
					+ "��a����㰲װ�˸ò���ĸ���ע����ListenerȻ����ж����\n"
					+ "��a�������Ҫʹ�ø���������ʵ�������µ�¼"
					);
			return;
		}
		// ����: Unknown command. Type "/help" for help.
		sender.sendMessage(main.getServer().spigot().getConfig().getConfigurationSection("messages").getString("unknown-command"));
	}
	
	public boolean isMatchPlayerName(String player) {
		Pattern p = Pattern.compile(main.config_nameRegex);
        Matcher m = p.matcher(player);
		return m.matches();
	}
}
