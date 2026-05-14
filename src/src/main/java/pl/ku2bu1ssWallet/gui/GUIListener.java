package pl.ku2bu1ssWallet.gui;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.ku2bu1ssWallet.managers.ConfigManager;
import pl.ku2bu1ssWallet.managers.ShopCategory;
import pl.ku2bu1ssWallet.managers.ShopManager;
import pl.ku2bu1ssWallet.managers.WalletManager;
import pl.ku2bu1ssWallet.utils.ColorUtil;

public class GUIListener implements Listener {

    private final ConfigManager configManager;
    private final WalletManager walletManager;
    private final WalletGUI walletGUI;

    public GUIListener(ConfigManager configManager, WalletManager walletManager,
            WalletGUI walletGUI) {
        this.configManager = configManager;
        this.walletManager = walletManager;
        this.walletGUI = walletGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        if (inventory == null) {
            return;
        }

        if (inventory.getHolder() instanceof WalletGUIHolder) {
            event.setCancelled(true);
            if (event.getClickedInventory() == inventory) {
                handleWalletGUIClick(player, event.getSlot(), inventory);
            }
            return;
        }

        if (inventory.getHolder() instanceof ConfirmationGUIHolder) {
            event.setCancelled(true);
            if (event.getClickedInventory() == inventory) {
                handleConfirmationGUIClick(player, event.getSlot(), (ConfirmationGUIHolder) inventory.getHolder());
            }
        }
    }

    private void handleWalletGUIClick(Player player, int slot, Inventory inventory) {
        FileConfiguration config = configManager.getConfig();

        ConfigurationSection categoryButtons = config.getConfigurationSection("gui.wallet.category_buttons");
        if (categoryButtons != null) {
            int kluczeSlot = categoryButtons.getInt("klucze.slot", 39);
            if (slot == kluczeSlot) {
                walletGUI.open(player, ShopCategory.KLUCZE);
                return;
            }

            int dodatkiSlot = categoryButtons.getInt("dodatki.slot", 40);
            if (slot == dodatkiSlot) {
                walletGUI.open(player, ShopCategory.DODATKI);
                return;
            }

            int rangiSlot = categoryButtons.getInt("rangi.slot", 41);
            if (slot == rangiSlot) {
                walletGUI.open(player, ShopCategory.RANGI);
                return;
            }
        }

        int balanceSlot = config.getInt("gui.wallet.balance.slot", 4);
        if (slot == balanceSlot) {
            return;
        }

        ItemStack clickedItem = inventory.getItem(slot);
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        WalletGUIHolder holder = (WalletGUIHolder) inventory.getHolder();

        ShopManager.ShopItem shopItem = (holder != null) ? holder.getShopItem(slot) : null;
        if (shopItem == null) {
            return;
        }

        ConfirmationGUI confirmationGUI = new ConfirmationGUI(configManager, walletManager, shopItem);
        confirmationGUI.open(player);
    }

    private void handleConfirmationGUIClick(Player player, int slot, ConfirmationGUIHolder holder) {
        FileConfiguration config = configManager.getConfig();
        int confirmSlot = config.getInt("gui.confirmation-gui.confirm-slot", 11);
        int cancelSlot = config.getInt("gui.confirmation-gui.cancel-slot", 15);

        if (slot == confirmSlot) {
            handlePurchase(player, holder.getShopItem());
        } else if (slot == cancelSlot) {
            player.closeInventory();
            player.sendMessage(configManager.getMessage("purchase.cancelled"));
        }
    }

    private void handlePurchase(Player player, ShopManager.ShopItem shopItem) {
        double price = shopItem.getPrice();
        double balance = walletManager.getBalance(player);

        if (!walletManager.hasEnough(player, price)) {
            double missing = price - balance;
            String missingFormatted = ColorUtil.formatCurrency(missing, true);
            player.sendMessage(configManager.getMessage("purchase.insufficient-funds",
                    "missing", missingFormatted));
            player.closeInventory();
            return;
        }

        ItemStack itemToGive = shopItem.toItemStack();
        if (!hasInventorySpace(player, itemToGive)) {
            player.sendMessage(configManager.getMessage("purchase.inventory-full"));
            player.closeInventory();
            return;
        }

        walletManager.removeBalance(player, price);
        player.getInventory().addItem(itemToGive);

        String itemName = shopItem.getDisplayName() != null ? shopItem.getDisplayName() : shopItem.getMaterial().name();
        player.sendMessage(configManager.getMessage("purchase.success",
                "amount", String.valueOf(shopItem.getAmount()),
                "item", itemName,
                "price", ColorUtil.formatCurrency(price, true)));

        player.closeInventory();
    }

    private boolean hasInventorySpace(Player player, ItemStack item) {
        Inventory inv = player.getInventory();
        int amount = item.getAmount();

        for (ItemStack invItem : inv.getStorageContents()) {
            if (invItem != null && invItem.isSimilar(item)) {
                int space = invItem.getMaxStackSize() - invItem.getAmount();
                amount -= space;
                if (amount <= 0) {
                    return true;
                }
            }
        }

        for (ItemStack invItem : inv.getStorageContents()) {
            if (invItem == null || invItem.getType() == Material.AIR) {
                return true;
            }
        }

        return false;
    }
}
