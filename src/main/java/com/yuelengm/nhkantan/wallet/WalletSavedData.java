package com.yuelengm.nhkantan.wallet;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

/**
 * Balances of all players keyed by UUID, stored in the global map storage of the world so they do not depend on the
 * player being online. Balances are whole units of market currency and may be negative (debt).
 */
public class WalletSavedData extends WorldSavedData {

    public static final String DATA_NAME = "nhkantan_wallets";

    private static final String NBT_BALANCES = "Balances";

    private final Map<UUID, Long> balances = new HashMap<>();

    /** Constructor required by the map storage, which instantiates saved data reflectively. */
    public WalletSavedData(String name) {
        super(name);
    }

    public boolean has(UUID player) {
        return balances.containsKey(player);
    }

    public long get(UUID player) {
        Long balance = balances.get(player);
        return balance == null ? 0L : balance;
    }

    public void set(UUID player, long balance) {
        balances.put(player, balance);
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        balances.clear();
        NBTTagCompound stored = tag.getCompoundTag(NBT_BALANCES);
        for (String key : stored.func_150296_c()) {
            try {
                balances.put(UUID.fromString(key), stored.getLong(key));
            } catch (IllegalArgumentException ignored) {
                // Not a UUID; skip the corrupt entry
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagCompound stored = new NBTTagCompound();
        for (Map.Entry<UUID, Long> entry : balances.entrySet()) {
            stored.setLong(
                entry.getKey()
                    .toString(),
                entry.getValue());
        }
        tag.setTag(NBT_BALANCES, stored);
    }
}
