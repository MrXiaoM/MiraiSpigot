package top.mrxiaom.qqwhitelist;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.message.FriendMessageEvent;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;
import net.mamoe.mirai.message.data.QuoteReply;

public class MiraiEventHost extends SimpleListenerHost{
	Plugin main;
	public MiraiEventHost(Plugin main) {
		this.main = main;
	}
	
	@EventHandler
	private ListeningStatus onNewFriendRequest(NewFriendRequestEvent event) {
		if(main.config_autoAcceptFriendAddRequest) event.accept();
		return ListeningStatus.LISTENING;
	}
	
	@EventHandler
	private ListeningStatus onBotOnline(BotOnlineEvent event) {
		main.getLogger().info(main.message_prefix + main.message_loginsuccess.replace("$uid", String.valueOf(event.getBot().getId())));
		return ListeningStatus.LISTENING;
	}
	
	@EventHandler
	private ListeningStatus onFriendMessage(FriendMessageEvent event) {
		if(main.config_allowFriendRequest) {
			String msg = event.getMessage().contentToString();
			String key = main.config_prefixCommandKey;
			if(msg.toLowerCase().startsWith(key.toLowerCase())) {
				if(msg.length() > key.length()) {
					String player = msg.substring(key.length()).trim();
					if(this.isMatchPlayerName(player)) {
						for(OfflinePlayer ban : Bukkit.getBannedPlayers()) {
							if(ban.getName().equalsIgnoreCase(player)) {
								event.getSender().sendMessage(new QuoteReply(event.getSource()).plus(main.message_bot_prefix + main.message_bot_banned.replace("$user", player)));
								return ListeningStatus.LISTENING;
							}
						}
						if(main.whitelist.contains(player)) {
							event.getSender().sendMessage(new QuoteReply(event.getSource()).plus(main.message_bot_prefix + main.message_bot_already.replace("$user", player)));
							return ListeningStatus.LISTENING;
						}
						main.whitelist.add(player);
						main.whitelist.saveConfig();
						main.getLogger().info("玩家 " + player + " 通过QQ "+ event.getSender().getId() + " 添加了自己为白名单");
						//Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "whitelist add " + player);
						event.getSender().sendMessage(new QuoteReply(event.getSource()).plus(main.message_bot_prefix + main.message_bot_addsuccess.replace("$user", player)));
					}
					else {
						event.getSender().sendMessage(new QuoteReply(event.getSource()).plus(main.message_bot_prefix + main.message_bot_invalidUsername));
					}
				}
				else {
					event.getSender().sendMessage(new QuoteReply(event.getSource()).plus(main.message_bot_prefix + main.message_bot_invalidUsername));
				}
			}
		}
		return ListeningStatus.LISTENING;
	}

	@EventHandler
	private ListeningStatus onGroupMessage(GroupMessageEvent event) {
		if(main.config_groupList.contains(event.getGroup().getId())) {
			String msg = event.getMessage().contentToString();
			String key = main.config_prefixCommandKey;
			if(msg.toLowerCase().startsWith(key.toLowerCase())) {
				if(msg.length() > key.length()) {
					String player = msg.substring(key.length()).trim();
					if(this.isMatchPlayerName(player)) {
						for(OfflinePlayer ban : Bukkit.getBannedPlayers()) {
							if(ban.getName().equalsIgnoreCase(player)) {
								event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(main.message_bot_prefix + main.message_bot_banned.replace("$user", player)));
								return ListeningStatus.LISTENING;
							}
						}
						if(main.whitelist.contains(player)) {
							event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(main.message_bot_prefix + main.message_bot_already.replace("$user", player)));
							return ListeningStatus.LISTENING;
						}
						main.whitelist.add(player);
						main.whitelist.saveConfig();
						main.getLogger().info("玩家 " + player + " 通过QQ群 " + event.getGroup().getId() + " 的成员 "+ event.getSender().getId() + " 添加了自己为白名单");
						//Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "whitelist add " + player);
						event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(main.message_bot_prefix + main.message_bot_addsuccess.replace("$user", player)));
					}
					else {
						event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(main.message_bot_prefix + main.message_bot_invalidUsername));
					}
				}
				else {
					event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(main.message_bot_prefix + main.message_bot_invalidUsername));
				}
			}
		}
		return ListeningStatus.LISTENING;
	}
	
	public boolean isMatchPlayerName(String player) {
		Pattern p = Pattern.compile(main.config_nameRegex);
        Matcher m = p.matcher(player);
		return m.matches();
	}
}
