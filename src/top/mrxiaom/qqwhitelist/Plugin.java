package top.mrxiaom.qqwhitelist;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.internal.QQAndroidBot;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol;

public class Plugin extends JavaPlugin implements Listener{
	
	public QQAndroidBot bot;
	MiraiEventHost host;
	Commands commands;
	WhiteList whitelist;
	
	// ���������õģ�д�������ע�������EventListener�ر�
	public List<Runnable> onLogin = Lists.<Runnable>newArrayList();
	
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

	public long config_uid;
	public String config_password;
	public boolean config_auto_login;
	public boolean config_allowFriendRequest;
	public boolean config_autoAcceptFriendAddRequest;
	public String config_nameRegex;
	public String config_prefixCommandKey;
	public String config_protocol;
	public List<Long> config_groupList;

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
			this.login();
		}
	}
	
	public void saveConfig() {
		super.saveConfig();
		
		if(this.whitelist != null) this.whitelist.saveConfig();
		else this.whitelist = new WhiteList(this);
	}
	
	public void onEnable() {
		this.whitelist = new WhiteList(this);
		this.commands = new Commands(this);
		this.host = new MiraiEventHost(this);
		Bukkit.getPluginManager().registerEvents(commands, this);
		Bukkit.getPluginManager().registerEvents(this, this);
		
		this.getLogger().info("������NoSuchProviderException�쳣Ϊjar��ȫ��֤��ͨ������");
		this.getLogger().info("�ѳ��Բ�׽���Ҳ�׽��������Ӱ������ʹ�ã������");
		
		this.addDefaultLoginTask();
		this.reloadConfig();
	}
	
	public void addDefaultLoginTask() {
		this.onLogin.add(new Runnable() {
			@Override
			public void run() {
				bot.getEventChannel().registerListenerHost(host);
			}
		});
	}
	
	// ���ø� EventHost �ˣ�������ע����һ���¼�
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
		try {
			this.getLogger().info("���ڳ�ʼ��������ʵ��");
			final String deviceInfoPath = this.getDataFolder().getAbsolutePath() + "\\deviceInfo.json";
			final MiraiProtocol protocol = this.getProtocolFromString(config_protocol, MiraiProtocol.ANDROID_PAD);
			this.getLogger().info("ʹ��Э��: " + protocol.name());
			this.bot = (QQAndroidBot)BotFactory.INSTANCE.newBot(this.config_uid, this.config_password, new BotConfiguration() {
				{
					this.setProtocol(protocol);
					this.fileBasedDeviceInfo(deviceInfoPath);
				}
			});
			this.getLogger().info("��ʼ�����");
		} catch(Throwable t) {
			// ��ΪĳЩԭ��jar��֤�ز�ͨ��������Ҫ���˵�����쳣
			// ��׽��������
			//if (!t.getMessage().contains("cannot authenticate")) {
				this.getLogger().warning("��ʼ��������ʱ����һ���쳣: " + t.getLocalizedMessage());
			//}
		}
		if(this.bot != null) {
			this.bot.getEventChannel().registerListenerHost(this.host);
		}
	}
	
	public void login() {
		try {
			if(this.bot == null) {
				this.initBot();
			}
			this.bot.login();
			
			for(Runnable r : onLogin) {
				r.run();
			}
		} catch(Throwable t) {
			this.getLogger().warning("��¼������ʱ����һ���쳣: ");
			this.getLogger().warning(t.getLocalizedMessage());
		}
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		this.commands.onCommand(sender, command, label, args);
		return true;
	}

	public void onDisable() {
		if (this.bot != null) {
			try {
				this.bot.close(new Exception("�������رգ��ǳ�������"));
			}catch(Throwable t){
				this.getLogger().warning("ж�ز���ǳ�������ʱ����һ���쳣: " + t.getLocalizedMessage());
			}
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
}
