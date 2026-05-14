package pl.ku2bu1ssWallet.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WalletManager {

    private final JavaPlugin plugin;
    private final Map<UUID, Double> balances;
    private File walletsFile;
    private FileConfiguration walletsConfig;

    public WalletManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.balances = new HashMap<>();
        loadWallets();
    }

    private void loadWallets() {
        walletsFile = new File(plugin.getDataFolder(), "wallets.yml");

        if (!walletsFile.exists()) {
            try {
                walletsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create wallets.yml: " + e.getMessage());
            }
        }

        walletsConfig = YamlConfiguration.loadConfiguration(walletsFile);

        for (String key : walletsConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                double balance = walletsConfig.getDouble(key);
                balances.put(uuid, balance);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in wallets.yml: " + key);
            }
        }

        plugin.getLogger().info("Loaded " + balances.size() + " wallets from database");
    }

    public void saveWallets() {
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            walletsConfig.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            walletsConfig.save(walletsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save wallets.yml: " + e.getMessage());
        }
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, getStartingBalance());
    }

    public double getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, Math.max(0, amount));
        saveWallets();
    }

    public void setBalance(Player player, double amount) {
        setBalance(player.getUniqueId(), amount);
    }

    public void addBalance(UUID uuid, double amount) {
        double currentBalance = getBalance(uuid);
        setBalance(uuid, currentBalance + amount);
    }

    public void addBalance(Player player, double amount) {
        addBalance(player.getUniqueId(), amount);
    }

    public boolean removeBalance(UUID uuid, double amount) {
        double currentBalance = getBalance(uuid);
        if (currentBalance >= amount) {
            setBalance(uuid, currentBalance - amount);
            return true;
        }
        return false;
    }

    public boolean removeBalance(Player player, double amount) {
        return removeBalance(player.getUniqueId(), amount);
    }

    public boolean hasEnough(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public boolean hasEnough(Player player, double amount) {
        return hasEnough(player.getUniqueId(), amount);
    }

    private double getStartingBalance() {
        return plugin.getConfig().getDouble("wallet.starting-balance", 0.0);
    }
}
