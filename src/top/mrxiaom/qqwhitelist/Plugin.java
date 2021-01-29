package top.mrxiaom.qqwhitelist;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import net.mamoe.mirai.BotFactoryJvm;
import net.mamoe.mirai.event.Events;
import net.mamoe.mirai.qqandroid.QQAndroidBot;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol;

public class Plugin extends JavaPlugin implements Listener, CoroutineScope{
	public QQAndroidBot bot;
	MiraiEventHost host;
	Commands commands;
	WhiteList whitelist;
	CoroutineContext cc;
	
	public String message_prefix;
	public String message_logining;
	public String message_loginfail;
	public String message_loginsuccess;
	public String message_logined;
	public String message_destoryed;
	public String message_reloaded;
	public String message_playernotexist;

	public String message_bot_prefix;
	public String message_bot_invalidUsername;
	public String message_bot_addsuccess;
	public String message_bot_already;
	public String message_bot_banned;
	
	public String message_whitelist_kick;

	public List<Long> config_groupList;
	public long config_uid;
	public String config_password;
	public boolean config_allowFriendRequest;
	public boolean config_autoAcceptFriendAddRequest;
	public String config_nameRegex;
	public String config_prefixCommandKey;
	public String config_protocol;
	public boolean config_auto_login;

	public void loadPluginConfig() {
		this.saveDefaultConfig();

		this.message_prefix = this.getString("messages.prefix", "&7[&f白名单&7]&e").replace("&", "§");
		this.message_logining = this.getString("messages.logining", "&e正在登录机器人 $uid &7(详细结果请见控制台)").replace("&", "§");
		this.message_loginfail = this.getString("messages.loginfail", "&c登录机器人 $uid 失败! &a原因: &7 $reason").replace("&", "§");
		this.message_loginsuccess = this.getString("messages.loginsuccess", "&a机器人 $uid 登录成功!").replace("&", "§");
		this.message_logined = this.getString("messages.logined", "&a机器人已经在线了! 无需重复登录").replace("&", "§");
		this.message_destoryed = this.getString("messages.destoryed", "&a机器人实例已销毁!").replace("&", "§");
		this.message_destoryed = this.getString("messages.reloaded", "&a插件重载完毕").replace("&", "§");
		this.message_playernotexist = this.getString("messages.playernotexist", "&c玩家不存在").replace("&", "§");
		
		this.message_bot_prefix = this.getString("messages.bot.prefix", "[服务器] ");
		this.message_bot_invalidUsername = this.getString("messages.bot.invalidUsername", "无效的用户名");
		this.message_bot_addsuccess = this.getString("messages.bot.addsuccess", "无效的用户名");
		this.message_bot_already = this.getString("messages.bot.already", "玩家 $user 已经是白名单了，请不要重复添加");
		this.message_bot_banned = this.getString("messages.bot.addfail-banned", "玩家 $user 已被封禁，无法添加到白名单").replace("&", "§");
		
		this.message_whitelist_kick = this.getString("messages.whitelist.kick", "&b你还没有白名单，请加群 XXXX 到群内机器人申请").replace("&", "§");

		this.config_groupList = this.getConfig().getLongList("general.groupList");
		this.config_uid = this.getLong("general.qq", -1);
		this.config_password = this.getString("general.password", "null");
		this.config_allowFriendRequest = this.getBoolean("general.allowFriendRequest", false);
		this.config_autoAcceptFriendAddRequest = this.getBoolean("general.autoAcceptFriendAddRequest", false);
		this.config_nameRegex = this.getString("general.nameRegex", "[a-zA-Z0-9_]");
		this.config_prefixCommandKey = this.getString("general.prefixCommandKey", "添加白名单");
		this.config_protocol = this.getString("general.protocol", "ANDROID_PAD");
		this.config_auto_login = this.getBoolean("general.auto-login", true);

		this.getLogger().info("插件配置载入完毕");
	}
	
	public void reloadConfig() {
		super.reloadConfig();
		this.loadPluginConfig();

		if(this.whitelist != null) this.whitelist.reloadConfig();
		else this.whitelist = new WhiteList(this);
		
		initBot();
		if (this.config_uid >= 10000 && this.config_auto_login) {
			this.getLogger().info(this.message_prefix + this.message_logining.replace("$uid", String.valueOf(this.config_uid)));
			this.bot.login();
		}
	}
	
	public void saveConfig() {
		super.saveConfig();
		
		if(this.whitelist != null) this.whitelist.saveConfig();
		else this.whitelist = new WhiteList(this);
		
	}

	public void onEnable() {
		this.whitelist = new WhiteList(this);

		this.loadPluginConfig();
		
		this.commands = new Commands(this);
		this.host = new MiraiEventHost(this);
		Bukkit.getPluginManager().registerEvents(commands, this);
		Bukkit.getPluginManager().registerEvents(this, this);
		this.initBot();
		if (this.config_uid >= 10000L && this.config_auto_login) {
			this.getLogger().info(this.message_prefix + this.message_logining.replace("$uid", String.valueOf(this.config_uid)));
			this.login();
		}
		
	}
	
	@EventHandler
	public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
		if(this.whitelist.contains(event.getName())) {
			event.allow();
		}
		else {
			event.disallow(Result.KICK_WHITELIST, this.message_whitelist_kick);
		}
	}

	public void initBot() {
		final String deviceInfoPath = this.getDataFolder().getAbsolutePath() + "\\deviceInfo.json";
		final MiraiProtocol protocol = this.getProtocolFromString(config_protocol, MiraiProtocol.ANDROID_PAD);
		
		this.bot = (QQAndroidBot)BotFactoryJvm.newBot(this.config_uid, this.config_password, new BotConfiguration() {
			{
				this.setProtocol(protocol);
				this.fileBasedDeviceInfo(deviceInfoPath);
			}
		});
	}
	public void login() {
		if(this.bot == null) {
			this.initBot();
		}
		this.bot.login();

		Events.registerEvents(bot, this.host);
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		this.commands.onCommand((ConsoleCommandSender) sender, command, label, args);
		return true;
	}

	public void onDisable() {
		if (this.bot != null) {
			try {
			this.bot.close(new Exception("服务器关闭，登出机器人"));
			}catch(Throwable t){t.printStackTrace();}
			this.getLogger().info("机器人账号已登出");
		}
		this.bot = null;
		this.host = null;
		this.commands = null;
		this.getLogger().info("插件已卸载");
	}

	public boolean isBotOnline() {
		if (this.bot == null)
			return false;
		if (this.bot._network == null)
			return false;
		return this.bot.isOnline();
	}
	
	public MiraiProtocol getProtocolFromString(String key, MiraiProtocol nullValue) {
		for (MiraiProtocol p : MiraiProtocol.values()) {
			if (p.name().equalsIgnoreCase(config_protocol)) {
				return p;
			}
		}
		return nullValue;
	}

	public long getLong(String key, long nullValue) {
		if (this.getConfig().contains(key)) {
			return this.getConfig().getLong(key);
		} else {
			this.getLogger().warning("无法在配置文件中找到长整型值 \"" + key + "\"，将使用默认值");
			return nullValue;
		}
	}

	public int getInt(String key, int nullValue) {
		if (this.getConfig().contains(key)) {
			return this.getConfig().getInt(key);
		} else {
			this.getLogger().warning("无法在配置文件中找到整数型值 \"" + key + "\"，将使用默认值");
			return nullValue;
		}
	}

	public boolean getBoolean(String key, boolean nullValue) {
		if (this.getConfig().contains(key)) {
			return this.getConfig().getBoolean(key);
		} else {
			this.getLogger().warning("无法在配置文件中找到布尔型值 \"" + key + "\"，将使用默认值");
			return nullValue;
		}
	}

	public String getString(String key, String nullValue) {
		if (this.getConfig().contains(key)) {
			return this.getConfig().getString(key);
		} else {
			this.getLogger().warning("无法在配置文件中找到文本型值 \"" + key + "\"，将使用默认值");
			return nullValue;
		}
	}

	@Override
	public CoroutineContext getCoroutineContext() {
		return cc;
	}
}
