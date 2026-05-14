package pl.ku2bu1ssWallet.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.ku2bu1ssWallet.managers.ConfigManager;
import pl.ku2bu1ssWallet.managers.ShopCategory;
import pl.ku2bu1ssWallet.managers.ShopManager;
import pl.ku2bu1ssWallet.managers.WalletManager;
import pl.ku2bu1ssWallet.utils.ColorUtil;
import pl.ku2bu1ssWallet.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public class WalletGUI {

    private final ConfigManager configManager;
    private final WalletManager walletManager;
    private final ShopManager shopManager;

    public WalletGUI(ConfigManager configManager, WalletManager walletManager, ShopManager shopManager) {
        this.configManager = configManager;
        this.walletManager = walletManager;
        this.shopManager = shopManager;

    }

    public void open(Player player) {
        open(player, ShopCategory.KLUCZE);
    }

    public void open(Player player, ShopCategory filter) {

        FileConfiguration config = configManager.getConfig();

        String title = ColorUtil.colorize(config.getString("gui.wallet.title", "&#FFD700&lTWÓJ PORTFEL"));
        int size = config.getInt("gui.wallet.size", 45);

        WalletGUIHolder holder = new WalletGUIHolder();
        Inventory inventory = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inventory);
        holder.setCurrentCategory(filter);

        addDecorativeItems(inventory, config);

        addBalanceDisplay(inventory, player, config);

        addCategoryButtons(inventory, config);

        addShopItems(inventory, config, filter);

        player.openInventory(inventory);
    }

    private void addDecorativeItems(Inventory inventory, FileConfiguration config) {
        ConfigurationSection itemsSection = config.getConfigurationSection("gui.wallet.items");
        if (itemsSection == null)
            return;

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection == null)
                continue;

            try {
                Material material = Material.valueOf(itemSection.getString("material", "WHITE_STAINED_GLASS_PANE"));
                String displayName = ColorUtil.colorize(itemSection.getString("display_name", "&7"));
                List<Integer> slots = itemSection.getIntegerList("slots");

                ItemStack pane = new ItemBuilder(material)
                        .setName(displayName)
                        .build();

                for (int slot : slots) {
                    if (slot >= 0 && slot < inventory.getSize()) {
                        inventory.setItem(slot, pane);
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private void addBalanceDisplay(Inventory inventory, Player player, FileConfiguration config) {
        ConfigurationSection balanceSection = config.getConfigurationSection("gui.wallet.balance");
        if (balanceSection == null)
            return;

        int slot = balanceSection.getInt("slot", 4);
        Material material;
        try {
            material = Material.valueOf(balanceSection.getString("material", "GOLD_INGOT"));
        } catch (IllegalArgumentException e) {
            material = Material.GOLD_INGOT;
        }

        double balance = walletManager.getBalance(player);
        String balanceFormatted = ColorUtil.formatCurrency(balance, true);

        String name = ColorUtil.colorize(balanceSection.getString("display_name", "&#FFD700&lTwoje Saldo:"));
        List<String> loreTemplate = balanceSection.getStringList("lore");

        List<String> lore = new ArrayList<>();
        for (String line : loreTemplate) {
            lore.add(ColorUtil.colorize(line.replace("{balance}", balanceFormatted)));
        }

        ItemStack balanceItem = new ItemBuilder(material)
                .setName(name)
                .setLore(lore)
                .build();

        inventory.setItem(slot, balanceItem);
    }

    private void addCategoryButtons(Inventory inventory, FileConfiguration config) {
        ConfigurationSection buttonsSection = config.getConfigurationSection("gui.wallet.category_buttons");
        if (buttonsSection == null)
            return;

        for (String categoryKey : buttonsSection.getKeys(false)) {
            ConfigurationSection buttonSection = buttonsSection.getConfigurationSection(categoryKey);
            if (buttonSection == null)
                continue;

            try {
                int slot = buttonSection.getInt("slot");
                Material material = Material.valueOf(buttonSection.getString("material", "PAPER"));
                String displayName = ColorUtil.colorize(buttonSection.getString("display_name", "&fCategory"));
                List<String> lore = new ArrayList<>();
                for (String line : buttonSection.getStringList("lore")) {
                    lore.add(ColorUtil.colorize(line));
                }

                ItemStack button = new ItemBuilder(material)
                        .setName(displayName)
                        .setLore(lore)
                        .build();

                inventory.setItem(slot, button);
            } catch (Exception e) {
            }
        }
    }

    private void addShopItems(Inventory inventory, FileConfiguration config, ShopCategory filter) {
        List<ShopManager.ShopItem> items;
        if (filter != null) {
            items = shopManager.getItemsByCategory(filter);
        } else {
            items = shopManager.getItems();
        }

        WalletGUIHolder holder = (WalletGUIHolder) inventory.getHolder();

        boolean itemGlow = config.getBoolean("shop.item-glow", true);
        List<String> shopLoreTemplate = configManager.getMessageList("gui.shop-item.lore");

        List<Integer> availableSlots = config.getIntegerList("gui.wallet.available_slots");
        if (availableSlots.isEmpty()) {
            availableSlots = new ArrayList<>();
            for (int i = 10; i <= 34; i++) {
                availableSlots.add(i);
            }
        }

        int autoSlotIndex = 0;
        for (ShopManager.ShopItem shopItem : items) {
            int slot = shopItem.getSlot();

            if (slot == -1) {
                if (autoSlotIndex >= availableSlots.size())
                    break;
                slot = availableSlots.get(autoSlotIndex);
                autoSlotIndex++;
            }

            if (slot < 0 || slot >= inventory.getSize())
                continue;

            if (holder != null) {
                holder.setShopItem(slot, shopItem);
            }

            ItemStack displayItem = shopItem.toItemStack();
            ItemBuilder builder = new ItemBuilder(displayItem);

            List<String> customLore = new ArrayList<>(shopItem.getLore());
            for (String loreLine : shopLoreTemplate) {
                String formatted = loreLine
                        .replace("{price}", ColorUtil.formatCurrency(shopItem.getPrice(), true))
                        .replace("{amount}", String.valueOf(shopItem.getAmount()));
                customLore.add(formatted);
            }

            builder.setLore(customLore);

            if (itemGlow) {
                builder.setGlow();
            }

            inventory.setItem(slot, builder.build());
        }
    }

}
