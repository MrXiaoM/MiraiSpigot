package top.mrxiaom.qqwhitelist;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.Lists;

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
	
	public void add(String player) {
		if(!this.config.contains("whitelist")) this.config.set("whitelist", Lists.<String>newArrayList());
		List<String> oldList = this.config.getStringList("whitelist");
		oldList.add(player);
		this.config.set("whitelist", oldList);
	}

	public void remove(String player) {
		if(!this.config.contains("whitelist")) this.config.set("whitelist", Lists.<String>newArrayList());
		List<String> oldList = this.config.getStringList("whitelist");
		oldList.remove(player);
		this.config.set("whitelist", oldList);
	}
	
	public boolean contains(String player) {
		if(!this.config.contains("whitelist")) this.config.set("whitelist", Lists.<String>newArrayList());
		return this.config.getStringList("whitelist").contains(player);
	}
	
	public void reloadConfig() {
		if(configFile.exists()) {
			config = YamlConfiguration.loadConfiguration(configFile);
			if(!this.config.contains("whitelist")) this.config.set("whitelist", Lists.<String>newArrayList());
		}
		else {
			config = new YamlConfiguration();
			config.set("whitelist", Lists.<String>newArrayList());
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
