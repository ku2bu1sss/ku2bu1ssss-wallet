package pl.ku2bu1ssWallet.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import pl.ku2bu1ssWallet.managers.ShopManager;

public class ConfirmationGUIHolder implements InventoryHolder {

    private Inventory inventory;
    private final ShopManager.ShopItem shopItem;

    public ConfirmationGUIHolder(ShopManager.ShopItem shopItem) {
        this.shopItem = shopItem;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public ShopManager.ShopItem getShopItem() {
        return shopItem;
    }
}
