package pl.ku2bu1ssWallet.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import pl.ku2bu1ssWallet.managers.ShopCategory;
import pl.ku2bu1ssWallet.managers.ShopManager;

import java.util.HashMap;
import java.util.Map;

public class WalletGUIHolder implements InventoryHolder {

    private Inventory inventory;
    private ShopCategory currentCategory;
    private final Map<Integer, ShopManager.ShopItem> slotMap = new HashMap<>();

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public ShopCategory getCurrentCategory() {
        return currentCategory;
    }

    public void setCurrentCategory(ShopCategory currentCategory) {
        this.currentCategory = currentCategory;
    }

    public void setShopItem(int slot, ShopManager.ShopItem item) {
        slotMap.put(slot, item);
    }

    public ShopManager.ShopItem getShopItem(int slot) {
        return slotMap.get(slot);
    }
}
