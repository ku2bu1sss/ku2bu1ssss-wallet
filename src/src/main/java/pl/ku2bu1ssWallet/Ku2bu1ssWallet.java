package pl.ku2bu1ssWallet;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pl.ku2bu1ssWallet.commands.KubusWalletCommand;
import pl.ku2bu1ssWallet.commands.PortfelCommand;
import pl.ku2bu1ssWallet.gui.GUIListener;
import pl.ku2bu1ssWallet.gui.WalletGUI;
import pl.ku2bu1ssWallet.managers.ConfigManager;
import pl.ku2bu1ssWallet.managers.ShopManager;
import pl.ku2bu1ssWallet.managers.WalletManager;
import pl.ku2bu1ssWallet.placeholders.WalletExpansion;

public final class Ku2bu1ssWallet extends JavaPlugin {

    private ConfigManager configManager;
    private WalletManager walletManager;
    private ShopManager shopManager;

    private WalletGUI walletGUI;

    @Override
    public void onEnable() {
        getLogger().info("  Ku2bu1ss Wallet - Loading...  ");

        getLogger().info("Initializing managers...");
        configManager = new ConfigManager(this);
        walletManager = new WalletManager(this);
        shopManager = new ShopManager(configManager);

        walletGUI = new WalletGUI(configManager, walletManager, shopManager);

        getLogger().info("Registering commands...");
        getCommand("portfel").setExecutor(new PortfelCommand(walletGUI, configManager));

        KubusWalletCommand walletCommand = new KubusWalletCommand(walletManager, shopManager, configManager);
        getCommand("kubuswallet").setExecutor(walletCommand);
        getCommand("kubuswallet").setTabCompleter(walletCommand);

        getLogger().info("Registering listeners...");
        getServer().getPluginManager().registerEvents(
                new GUIListener(configManager, walletManager, walletGUI), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Registering PlaceholderAPI expansion...");
            new WalletExpansion(this, walletManager).register();
            getLogger().info("PlaceholderAPI expansion registered successfully!");
        } else {
            getLogger().warning("PlaceholderAPI not found! Placeholders will not work.");
        }

        getLogger().info("  Ku2bu1ss Wallet - Loaded!     ");
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving wallet data...");
        if (walletManager != null) {
            walletManager.saveWallets();
        }

        getLogger().info("  Ku2bu1ss Wallet - Unloaded!   ");
    }
}
