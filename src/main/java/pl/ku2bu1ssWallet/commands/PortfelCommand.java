package pl.ku2bu1ssWallet.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.ku2bu1ssWallet.gui.WalletGUI;
import pl.ku2bu1ssWallet.managers.ConfigManager;

public class PortfelCommand implements CommandExecutor {

    private final WalletGUI walletGUI;
    private final ConfigManager configManager;

    public PortfelCommand(WalletGUI walletGUI, ConfigManager configManager) {
        this.walletGUI = walletGUI;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(configManager.getMessage("must-be-player"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lordcode.use")) {
            player.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        walletGUI.open(player);

        return true;
    }
}
