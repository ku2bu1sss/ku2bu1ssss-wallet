package pl.ku2bu1ssWallet.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.ku2bu1ssWallet.utils.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public class ShopManager {

    private final ConfigManager configManager;
    private final List<ShopItem> items;

    public ShopManager(ConfigManager configManager) {
        this.configManager = configManager;
        this.items = new ArrayList<>();
        loadItems();
    }

    public void loadItems() {
        items.clear();

        FileConfiguration shop = configManager.getShop();
        ConfigurationSection itemsSection = shop.getConfigurationSection("items");

        if (itemsSection == null) {
            return;
        }

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection == null)
                continue;

            try {
                Material material = Material.valueOf(itemSection.getString("material"));
                int amount = itemSection.getInt("amount");
                double price = itemSection.getDouble("price");
                String displayName = itemSection.getString("display-name");
                List<String> lore = itemSection.getStringList("lore");

                ShopCategory category = ShopCategory.DODATKI;
                String categoryStr = itemSection.getString("category");
                if (categoryStr != null) {
                    ShopCategory parsed = ShopCategory.fromString(categoryStr);
                    if (parsed != null) {
                        category = parsed;
                    }
                }

                int slot = itemSection.getInt("slot", -1);

                ShopItem item = new ShopItem(material, amount, price, displayName, lore, category, slot);
                items.add(item);
            } catch (Exception e) {
            }
        }
    }

    private void saveItems() {
        FileConfiguration shop = configManager.getShop();
        shop.set("items", null);

        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            String path = "items." + i;

            shop.set(path + ".material", item.getMaterial().name());
            shop.set(path + ".amount", item.getAmount());
            shop.set(path + ".price", item.getPrice());
            shop.set(path + ".display-name", item.getDisplayName());
            shop.set(path + ".lore", item.getLore());
            shop.set(path + ".category", item.getCategory().name());
            shop.set(path + ".slot", item.getSlot());
        }

        configManager.saveShop();
    }

    public void addItem(ShopItem item) {
        items.add(item);
        saveItems();
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            saveItems();
        }
    }

    public List<ShopItem> getItems() {
        return new ArrayList<>(items);
    }

    public List<ShopItem> getItemsByCategory(ShopCategory category) {
        List<ShopItem> filtered = new ArrayList<>();
        for (ShopItem item : items) {
            if (item.getCategory() == category) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    public ShopItem getItemBySlot(int slot, ShopCategory category) {
        for (ShopItem item : items) {
            if (item.getSlot() == slot && item.getCategory() == category) {
                return item;
            }
        }
        return null;
    }

    public int getMaxItems() {
        return configManager.getConfig().getInt("shop.max-items", 28);
    }

    public boolean isFull() {
        return items.size() >= getMaxItems();
    }

    public static class ShopItem {
        private final Material material;
        private final int amount;
        private final double price;
        private final String displayName;
        private final List<String> lore;
        private final ShopCategory category;
        private final int slot;

        public ShopItem(Material material, int amount, double price, String displayName, List<String> lore,
                ShopCategory category, int slot) {
            this.material = material;
            this.amount = amount;
            this.price = price;
            this.displayName = displayName;
            this.lore = lore != null ? lore : new ArrayList<>();
            this.category = category;
            this.slot = slot;
        }

        public ShopItem(ItemStack itemStack, double price, ShopCategory category, int slot) {
            this.material = itemStack.getType();
            this.amount = itemStack.getAmount();
            this.price = price;
            this.category = category;
            this.slot = slot;

            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                this.displayName = ColorUtil.stripColors(meta.getDisplayName());
            } else {
                this.displayName = null;
            }

            if (meta != null && meta.hasLore()) {
                this.lore = new ArrayList<>();
                for (String line : meta.getLore()) {
                    this.lore.add(ColorUtil.stripColors(line));
                }
            } else {
                this.lore = new ArrayList<>();
            }
        }

        public Material getMaterial() {
            return material;
        }

        public int getAmount() {
            return amount;
        }

        public double getPrice() {
            return price;
        }

        public String getDisplayName() {
            return displayName;
        }

        public List<String> getLore() {
            return new ArrayList<>(lore);
        }

        public ShopCategory getCategory() {
            return category;
        }

        public int getSlot() {
            return slot;
        }

        public ItemStack toItemStack() {
            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                if (displayName != null && !displayName.isEmpty()) {
                    meta.setDisplayName(ColorUtil.colorize(displayName));
                }

                if (!lore.isEmpty()) {
                    List<String> coloredLore = new ArrayList<>();
                    for (String line : lore) {
                        coloredLore.add(ColorUtil.colorize(line));
                    }
                    meta.setLore(coloredLore);
                }

                item.setItemMeta(meta);
            }

            return item;
        }
    }
}
