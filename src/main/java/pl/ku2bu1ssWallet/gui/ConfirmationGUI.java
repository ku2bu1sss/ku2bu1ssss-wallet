package pl.ku2bu1ssWallet.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.ku2bu1ssWallet.managers.ConfigManager;
import pl.ku2bu1ssWallet.managers.ShopManager;
import pl.ku2bu1ssWallet.managers.WalletManager;
import pl.ku2bu1ssWallet.utils.ColorUtil;
import pl.ku2bu1ssWallet.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public class ConfirmationGUI {

    private final ConfigManager configManager;
    private final WalletManager walletManager;
    private final ShopManager.ShopItem shopItem;

    public ConfirmationGUI(ConfigManager configManager, WalletManager walletManager, ShopManager.ShopItem shopItem) {
        this.configManager = configManager;
        this.walletManager = walletManager;
        this.shopItem = shopItem;
    }

    public void open(Player player) {
        FileConfiguration config = configManager.getConfig();

        String title = ColorUtil
                .colorize(config.getString("gui.confirmation-gui.title", "&#FF4444&lPotwierdzenie zakupu"));
        int size = config.getInt("gui.confirmation-gui.size", 27);

        ConfirmationGUIHolder holder = new ConfirmationGUIHolder(shopItem);
        Inventory inventory = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inventory);

        addItemDisplay(inventory, player, config);

        addConfirmButton(inventory, config);

        addCancelButton(inventory, config);

        fillEmptySlots(inventory);

        player.openInventory(inventory);
    }

    private void addItemDisplay(Inventory inventory, Player player, FileConfiguration config) {
        int itemSlot = config.getInt("gui.confirmation-gui.item-slot", 13);

        double balance = walletManager.getBalance(player);
        String balanceFormatted = ColorUtil.formatCurrency(balance, true);
        String priceFormatted = ColorUtil.formatCurrency(shopItem.getPrice(), true);

        List<String> loreTemplate = configManager.getMessageList("gui.confirmation.item-display-lore");

        ItemStack displayItem = shopItem.toItemStack();
        ItemBuilder builder = new ItemBuilder(displayItem);

        List<String> customLore = new ArrayList<>(shopItem.getLore());

        for (String loreLine : loreTemplate) {
            String formatted = loreLine
                    .replace("{price}", priceFormatted)
                    .replace("{balance}", balanceFormatted);
            customLore.add(formatted);
        }

        builder.setLore(customLore);
        builder.setGlow();

        inventory.setItem(itemSlot, builder.build());
    }

    private void addConfirmButton(Inventory inventory, FileConfiguration config) {
        int confirmSlot = config.getInt("gui.confirmation-gui.confirm-slot", 11);
        String materialName = config.getString("gui.confirmation-gui.confirm-item", "LIME_STAINED_GLASS_PANE");

        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            material = Material.LIME_STAINED_GLASS_PANE;
        }

        String name = configManager.getMessage("gui.confirmation.confirm-button.name");
        List<String> loreTemplate = configManager.getMessageList("gui.confirmation.confirm-button.lore");

        List<String> lore = new ArrayList<>();
        String priceFormatted = ColorUtil.formatCurrency(shopItem.getPrice(), true);
        for (String line : loreTemplate) {
            lore.add(line.replace("{price}", priceFormatted));
        }

        ItemStack confirmButton = new ItemBuilder(material)
                .setName(name)
                .setLore(lore)
                .build();

        inventory.setItem(confirmSlot, confirmButton);
    }

    private void addCancelButton(Inventory inventory, FileConfiguration config) {
        int cancelSlot = config.getInt("gui.confirmation-gui.cancel-slot", 15);
        String materialName = config.getString("gui.confirmation-gui.cancel-item", "RED_STAINED_GLASS_PANE");

        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            material = Material.RED_STAINED_GLASS_PANE;
        }

        String name = configManager.getMessage("gui.confirmation.cancel-button.name");
        List<String> lore = configManager.getMessageList("gui.confirmation.cancel-button.lore");

        ItemStack cancelButton = new ItemBuilder(material)
                .setName(name)
                .setLore(lore)
                .build();

        inventory.setItem(cancelSlot, cancelButton);
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setName("&r")
                .build();

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }
}
