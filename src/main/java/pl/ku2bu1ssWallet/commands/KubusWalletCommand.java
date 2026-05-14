package pl.ku2bu1ssWallet.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.ku2bu1ssWallet.managers.ConfigManager;
import pl.ku2bu1ssWallet.managers.ShopCategory;
import pl.ku2bu1ssWallet.managers.ShopManager;
import pl.ku2bu1ssWallet.managers.WalletManager;
import pl.ku2bu1ssWallet.utils.ColorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class KubusWalletCommand implements CommandExecutor, TabCompleter {

    private final WalletManager walletManager;
    private final ShopManager shopManager;
    private final ConfigManager configManager;

    public KubusWalletCommand(WalletManager walletManager, ShopManager shopManager, ConfigManager configManager) {
        this.walletManager = walletManager;
        this.shopManager = shopManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lordcode.admin")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "give":
                return handleGive(sender, args);
            case "take":
                return handleTake(sender, args);
            case "set":
                return handleSet(sender, args);
            case "add":
                return handleAdd(sender, args);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(configManager.getMessage("prefix") +
                    ColorUtil.colorize("&#FFAA00Użycie: /kubuswallet give <gracz> <kwota>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(configManager.getMessage("player-not-found", "player", args[1]));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                sender.sendMessage(configManager.getMessage("invalid-amount"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getMessage("invalid-amount"));
            return true;
        }

        walletManager.addBalance(target, amount);

        String amountFormatted = ColorUtil.formatCurrency(amount, true);
        sender.sendMessage(configManager.getMessage("wallet.give.success-sender",
                "player", target.getName(), "amount", amountFormatted));
        target.sendMessage(configManager.getMessage("wallet.give.success-receiver",
                "amount", amountFormatted));

        return true;
    }

    private boolean handleTake(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(configManager.getMessage("prefix") +
                    ColorUtil.colorize("&#FFAA00Użycie: /kubuswallet take <gracz> <kwota>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(configManager.getMessage("player-not-found", "player", args[1]));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                sender.sendMessage(configManager.getMessage("invalid-amount"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getMessage("invalid-amount"));
            return true;
        }

        if (!walletManager.hasEnough(target, amount)) {
            sender.sendMessage(configManager.getMessage("wallet.take.insufficient-funds",
                    "player", target.getName()));
            return true;
        }

        walletManager.removeBalance(target, amount);

        String amountFormatted = ColorUtil.formatCurrency(amount, true);
        sender.sendMessage(configManager.getMessage("wallet.take.success-sender",
                "player", target.getName(), "amount", amountFormatted));
        target.sendMessage(configManager.getMessage("wallet.take.success-receiver",
                "amount", amountFormatted));

        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(configManager.getMessage("prefix") +
                    ColorUtil.colorize("&#FFAA00Użycie: /kubuswallet set <gracz> <kwota>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(configManager.getMessage("player-not-found", "player", args[1]));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount < 0) {
                sender.sendMessage(configManager.getMessage("invalid-amount"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getMessage("invalid-amount"));
            return true;
        }

        walletManager.setBalance(target, amount);

        String amountFormatted = ColorUtil.formatCurrency(amount, true);
        sender.sendMessage(configManager.getMessage("wallet.set.success-sender",
                "player", target.getName(), "amount", amountFormatted));
        target.sendMessage(configManager.getMessage("wallet.set.success-receiver",
                "amount", amountFormatted));

        return true;
    }

    private boolean handleAdd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(configManager.getMessage("must-be-player"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 4) {
            sender.sendMessage(configManager.getMessage("prefix") +
                    ColorUtil.colorize("&#FFAA00Użycie: /kubuswallet add <kategoria> <ilość> <cena> [slot]"));
            sender.sendMessage(configManager.getMessage("prefix") +
                    ColorUtil.colorize("&#FFD700Kategorie: &aKLUCZE&f, &bDODATKI&f, &eRANGI"));
            return true;
        }

        ShopCategory category = ShopCategory.fromString(args[1]);
        if (category == null) {
            sender.sendMessage(configManager.getMessage("prefix") +
                    ColorUtil.colorize("&#FF4444Nieprawidłowa kategoria! Dostępne: KLUCZE, DODATKI, RANGI"));
            return true;
        }

        if (shopManager.isFull()) {
            sender.sendMessage(configManager.getMessage("wallet.add.shop-full",
                    "max", String.valueOf(shopManager.getMaxItems())));
            return true;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            sender.sendMessage(configManager.getMessage("wallet.add.no-item-in-hand"));
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount <= 0 || amount > 64) {
                sender.sendMessage(configManager.getMessage("prefix") +
                        ColorUtil.colorize("&#FF4444Ilość musi być między 1 a 64!"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getMessage("prefix") +
                    ColorUtil.colorize("&#FF4444Nieprawidłowa ilość!"));
            return true;
        }

        double price;
        try {
            price = Double.parseDouble(args[3]);
            if (price <= 0) {
                sender.sendMessage(configManager.getMessage("invalid-amount"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getMessage("invalid-amount"));
            return true;
        }

        int slot = -1;
        if (args.length >= 5) {
            try {
                slot = Integer.parseInt(args[4]);
                if (slot < 0 || slot >= 54) {
                    sender.sendMessage(configManager.getMessage("prefix") +
                            ColorUtil.colorize("&#FF4444Slot musi być między 0 a 53!"));
                    return true;
                }

                if (shopManager.getItemBySlot(slot, category) != null) {
                    sender.sendMessage(configManager.getMessage("prefix") +
                            ColorUtil.colorize("&#FF4444Ten slot jest już zajęty w tej kategorii!"));
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(configManager.getMessage("prefix") +
                        ColorUtil.colorize("&#FF4444Nieprawidłowy slot!"));
                return true;
            }
        }

        ItemStack shopItemStack = handItem.clone();
        shopItemStack.setAmount(amount);

        ShopManager.ShopItem shopItem = new ShopManager.ShopItem(shopItemStack, price, category, slot);
        shopManager.addItem(shopItem);

        String itemName = shopItem.getDisplayName() != null ? shopItem.getDisplayName() : shopItem.getMaterial().name();
        String slotInfo = slot == -1 ? " (auto-slot)" : " (slot: " + slot + ")";
        sender.sendMessage(configManager.getMessage("wallet.add.success",
                "item", itemName, "price", ColorUtil.formatCurrency(price, true)) +
                ColorUtil.colorize("&#FFD700 Kategoria: &f" + category.getDisplayName() + slotInfo));

        return true;
    }

    private void sendHelp(CommandSender sender) {
        List<String> header = configManager.getMessageList("help.header");
        if (header != null && !header.isEmpty()) {
            for (String line : header) {
                sender.sendMessage(line);
            }
        }

        for (String line : configManager.getMessageList("help.commands")) {
            sender.sendMessage(line);
        }

        List<String> footer = configManager.getMessageList("help.footer");
        if (footer != null && !footer.isEmpty()) {
            for (String line : footer) {
                sender.sendMessage(line);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("give", "take", "set", "add"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take") ||
                    args[0].equalsIgnoreCase("set")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            }
            else if (args[0].equalsIgnoreCase("add")) {
                completions.addAll(Arrays.asList("KLUCZE", "DODATKI", "RANGI"));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add")) {
                completions.add("<ilość>");
            } else {
                completions.add("<kwota>");
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("add")) {
            completions.add("<cena>");
        } else if (args.length == 5 && args[0].equalsIgnoreCase("add")) {
            completions.add("<slot>");
            completions.add("[opcjonalny]");
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
