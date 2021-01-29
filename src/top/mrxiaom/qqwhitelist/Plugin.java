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

		this.message_prefix = this.getString("messages.prefix", "&7[&f������&7]&e").replace("&", "��");
		this.message_logining = this.getString("messages.logining", "&e���ڵ�¼������ $uid &7(��ϸ����������̨)").replace("&", "��");
		this.message_loginfail = this.getString("messages.loginfail", "&c��¼������ $uid ʧ��! &aԭ��: &7 $reason").replace("&", "��");
		this.message_loginsuccess = this.getString("messages.loginsuccess", "&a������ $uid ��¼�ɹ�!").replace("&", "��");
		this.message_logined = this.getString("messages.logined", "&a�������Ѿ�������! �����ظ���¼").replace("&", "��");
		this.message_destoryed = this.getString("messages.destoryed", "&a������ʵ��������!").replace("&", "��");
		this.message_destoryed = this.getString("messages.reloaded", "&a����������").replace("&", "��");
		this.message_playernotexist = this.getString("messages.playernotexist", "&c��Ҳ�����").replace("&", "��");
		
		this.message_bot_prefix = this.getString("messages.bot.prefix", "[������] ");
		this.message_bot_invalidUsername = this.getString("messages.bot.invalidUsername", "��Ч���û���");
		this.message_bot_addsuccess = this.getString("messages.bot.addsuccess", "��Ч���û���");
		this.message_bot_already = this.getString("messages.bot.already", "��� $user �Ѿ��ǰ������ˣ��벻Ҫ�ظ����");
		this.message_bot_banned = this.getString("messages.bot.addfail-banned", "��� $user �ѱ�������޷���ӵ�������").replace("&", "��");
		
		this.message_whitelist_kick = this.getString("messages.whitelist.kick", "&b�㻹û�а����������Ⱥ XXXX ��Ⱥ�ڻ���������").replace("&", "��");

		this.config_groupList = this.getConfig().getLongList("general.groupList");
		this.config_uid = this.getLong("general.qq", -1);
		this.config_password = this.getString("general.password", "null");
		this.config_allowFriendRequest = this.getBoolean("general.allowFriendRequest", false);
		this.config_autoAcceptFriendAddRequest = this.getBoolean("general.autoAcceptFriendAddRequest", false);
		this.config_nameRegex = this.getString("general.nameRegex", "[a-zA-Z0-9_]");
		this.config_prefixCommandKey = this.getString("general.prefixCommandKey", "��Ӱ�����");
		this.config_protocol = this.getString("general.protocol", "ANDROID_PAD");
		this.config_auto_login = this.getBoolean("general.auto-login", true);

		this.getLogger().info("��������������");
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
			this.bot.close(new Exception("�������رգ��ǳ�������"));
			}catch(Throwable t){t.printStackTrace();}
			this.getLogger().info("�������˺��ѵǳ�");
		}
		this.bot = null;
		this.host = null;
		this.commands = null;
		this.getLogger().info("�����ж��");
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
			this.getLogger().warning("�޷��������ļ����ҵ�������ֵ \"" + key + "\"����ʹ��Ĭ��ֵ");
			return nullValue;
		}
	}

	public int getInt(String key, int nullValue) {
		if (this.getConfig().contains(key)) {
			return this.getConfig().getInt(key);
		} else {
			this.getLogger().warning("�޷��������ļ����ҵ�������ֵ \"" + key + "\"����ʹ��Ĭ��ֵ");
			return nullValue;
		}
	}

	public boolean getBoolean(String key, boolean nullValue) {
		if (this.getConfig().contains(key)) {
			return this.getConfig().getBoolean(key);
		} else {
			this.getLogger().warning("�޷��������ļ����ҵ�������ֵ \"" + key + "\"����ʹ��Ĭ��ֵ");
			return nullValue;
		}
	}

	public String getString(String key, String nullValue) {
		if (this.getConfig().contains(key)) {
			return this.getConfig().getString(key);
		} else {
			this.getLogger().warning("�޷��������ļ����ҵ��ı���ֵ \"" + key + "\"����ʹ��Ĭ��ֵ");
			return nullValue;
		}
	}

	@Override
	public CoroutineContext getCoroutineContext() {
		return cc;
	}
}
