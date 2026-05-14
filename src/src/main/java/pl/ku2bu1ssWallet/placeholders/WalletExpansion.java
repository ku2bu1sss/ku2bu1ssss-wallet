package pl.ku2bu1ssWallet.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.ku2bu1ssWallet.Ku2bu1ssWallet;
import pl.ku2bu1ssWallet.managers.WalletManager;
import pl.ku2bu1ssWallet.utils.ColorUtil;

public class WalletExpansion extends PlaceholderExpansion {

    private final Ku2bu1ssWallet plugin;
    private final WalletManager walletManager;

    public WalletExpansion(Ku2bu1ssWallet plugin, WalletManager walletManager) {
        this.plugin = plugin;
        this.walletManager = walletManager;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "kubus";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "Kubus";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equalsIgnoreCase("wallet_cash")) {
            double balance = walletManager.getBalance(player.getUniqueId());
            return ColorUtil.formatCurrency(balance, false);
        }

        if (identifier.equalsIgnoreCase("wallet_cashzgroszami")) {
            double balance = walletManager.getBalance(player.getUniqueId());
            return ColorUtil.formatCurrency(balance, true);
        }

        return null;
    }
}
