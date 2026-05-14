package pl.ku2bu1ssWallet.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pl.ku2bu1ssWallet.utils.ColorUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration shop;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        saveResource("messages.yml");
        saveResource("shop.yml");

        config = plugin.getConfig();
        messages = loadConfig("messages.yml");
        shop = loadConfig("shop.yml");
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        messages = loadConfig("messages.yml");
        shop = loadConfig("shop.yml");
    }

    private void saveResource(String resourceName) {
        File file = new File(plugin.getDataFolder(), resourceName);
        if (!file.exists()) {
            plugin.saveResource(resourceName, false);
        }
    }

    private FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        return YamlConfiguration.loadConfiguration(file);
    }

    public void saveShop() {
        try {
            File file = new File(plugin.getDataFolder(), "shop.yml");
            shop.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save shop.yml: " + e.getMessage());
        }
    }

    public String getMessage(String path) {
        String message = messages.getString(path);
        if (message == null) {
            return ColorUtil.colorize("&c[Missing message: " + path + "]");
        }

        String prefix = messages.getString("prefix", "");
        message = message.replace("{prefix}", prefix);

        return ColorUtil.colorize(message);
    }

    public String getMessage(String path, String... placeholders) {
        String message = getMessage(path);

        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                String placeholder = "{" + placeholders[i] + "}";
                String value = placeholders[i + 1];
                message = message.replace(placeholder, value);
            }
        }

        return message;
    }

    public List<String> getMessageList(String path) {
        List<String> messages = this.messages.getStringList(path);
        return messages.stream()
                .map(ColorUtil::colorize)
                .collect(Collectors.toList());
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getShop() {
        return shop;
    }
}
