package pl.ku2bu1ssWallet.managers;

public enum ShopCategory {
    KLUCZE("Klucze"),
    DODATKI("Dodatki"),
    RANGI("Rangi");

    private final String displayName;

    ShopCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ShopCategory fromString(String name) {
        for (ShopCategory category : values()) {
            if (category.name().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return null;
    }
}
