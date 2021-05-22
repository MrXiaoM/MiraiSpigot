package top.mrxiaom.qqwhitelist;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class WhiteList {
	FileConfiguration config;
	File configFile;
	Plugin main;
	public WhiteList(Plugin main) {
		this.main = main;
		configFile = new File(main.getDataFolder().getAbsolutePath() + "\\whitelist.yml");
		this.reloadConfig();
	}
	
	public void setDefault(String path, Object value) {
		if (!this.config.contains(path)) {
			this.config.set(path, value);
		}
	}

	public FileConfiguration getConfig() {
		return this.config;
	}
	
	public void add(String player, long uid) {
		this.config.set("whitelist-qq." + player, String.valueOf(uid));
		this.saveConfig();
	}

	public void remove(String player) {
		if(!this.config.contains("whitelist-qq."+player)) return;
		ConfigurationSection cs = this.config.getConfigurationSection("whitelist-qq");
		ConfigurationSection newcs = this.config.createSection("whitelist-qq");
		for(String key : cs.getKeys(false)) {
			if(!key.equalsIgnoreCase(player)) {
				newcs.set(key, cs.get(key));
			}
		}
		this.config.set("whitelist-qq", newcs);
		this.saveConfig();
	}
	
	public boolean contains(String player) {
		return this.config.contains("whitelist-qq." + player);
	}
	
	public int getQQBinds(long qq) {
		if(!this.config.contains("whitelist-qq")) return 0;
		ConfigurationSection cs = this.config.getConfigurationSection("whitelist-qq");
		int count = 0;
		for(String key : cs.getKeys(false)) {
			if(cs.getString(key).equalsIgnoreCase(String.valueOf(qq))) {
				count++;
			}
		}
		return count;
	}
	
	public void reloadConfig() {
		if(configFile.exists()) {
			config = YamlConfiguration.loadConfiguration(configFile);
			if(this.config.contains("whitelist") && this.config.isList("whitelist")) {
				for(String str : this.config.getStringList("whitelist")) {
					this.getConfig().set("whitelist-qq." + str, "-1");
				}
				this.config.set("whitelist", "@Deprecated");
				this.saveConfig();
			}
		}
		else {
			config = new YamlConfiguration();
			this.saveConfig();
		}
	}

	public void saveConfig() {
		try {
			this.config.save(this.configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
