package ravenv.module.modules;

import ravenv.event.EventTarget;
import ravenv.event.types.EventType;
import ravenv.events.TickEvent;
import ravenv.module.Module;
import ravenv.util.ItemUtil;
import ravenv.util.KeyBindUtil;
import ravenv.property.properties.BooleanProperty;
import ravenv.property.properties.IntProperty;
import ravenv.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.block.Block;

public class AutoTool extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private int currentToolSlot = -1;
    private int previousSlot = -1;
    private int tickDelayCounter = 0;

    // New LiquidBounce mode state
    private boolean ignoreDurabilityLB = false;
    private float swapDelayLB = 20f;
    private int tickCounterLB = 0;
    private int previousHotbarSlotLB = -1;

    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"LEGACY", "LIQUIDBOUNCE"});
    public final IntProperty switchDelay = new IntProperty("delay", 0, 0, 5);
    public final BooleanProperty switchBack = new BooleanProperty("switch-back", true);
    public final BooleanProperty sneakOnly = new BooleanProperty("sneak-only", true);

    // LiquidBounce mode properties
    public final BooleanProperty ignoreDurability = new BooleanProperty("ignore-durability", false,
            () -> mode.getValue() == 1);
    public final IntProperty swapPreviousDelay = new IntProperty("swap-delay", 20, 1, 100,
            () -> mode.getValue() == 1);
    public final BooleanProperty requireSneaking = new BooleanProperty("require-sneaking", false,
            () -> mode.getValue() == 1);

    public AutoTool() {
        super("AutoTool", false);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (mode.getValue() == 0) {
                onTickLegacy();
            } else if (mode.getValue() == 1) {
                onTickLiquidBounce();
            }
        }
    }

    /**
     * Original RavenV AutoTool logic
     */
    private void onTickLegacy() {
        if (this.currentToolSlot != -1 && this.currentToolSlot != mc.thePlayer.inventory.currentItem) {
            this.currentToolSlot = -1;
            this.previousSlot = -1;
        }
        if (mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK
                && mc.gameSettings.keyBindAttack.isKeyDown()
                && !mc.thePlayer.isUsingItem()) {
            if (this.tickDelayCounter >= this.switchDelay.getValue()
                    && (!(Boolean) this.sneakOnly.getValue() || KeyBindUtil.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()))) {
                int slot = ItemUtil.findInventorySlot(
                        mc.thePlayer.inventory.currentItem, mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock()
                );
                if (mc.thePlayer.inventory.currentItem != slot) {
                    if (this.previousSlot == -1) {
                        this.previousSlot = mc.thePlayer.inventory.currentItem;
                    }
                    mc.thePlayer.inventory.currentItem = this.currentToolSlot = slot;
                }
            }
            this.tickDelayCounter++;
        } else {
            if (this.switchBack.getValue() && this.previousSlot != -1) {
                mc.thePlayer.inventory.currentItem = this.previousSlot;
            }
            this.currentToolSlot = -1;
            this.previousSlot = -1;
            this.tickDelayCounter = 0;
        }
    }

    /**
     * LiquidBounce-inspired AutoTool logic
     */
    private void onTickLiquidBounce() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        // Check if sneaking is required
        if (this.requireSneaking.getValue() && !mc.thePlayer.isSneaking()) {
            return;
        }

        // Get targeted block
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectType.BLOCK) {
            resetLiquidBounceState();
            return;
        }

        // Only proceed if left-clicking (attacking/mining)
        if (!mc.gameSettings.keyBindAttack.isKeyDown()) {
            resetLiquidBounceState();
            return;
        }

        Block targetBlock = mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock();

        // Find best tool for this block
        int bestSlot = findBestToolSlot(targetBlock);

        if (bestSlot != -1 && bestSlot != mc.thePlayer.inventory.currentItem) {
            switchToSlotWithDelay(bestSlot);
        }
    }

    /**
     * Finds the best tool in hotbar for mining a specific block
     */
    private int findBestToolSlot(Block targetBlock) {
        int bestSlot = -1;
        float bestSpeed = 1f;

        // Check all 9 hotbar slots
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);

            if (itemStack == null || itemStack.stackSize == 0) {
                continue;
            }

            // Durability check
            if (!this.ignoreDurability.getValue()) {
                int durability = itemStack.getMaxDamage() - itemStack.getItemDamage();
                if (durability < 2 && itemStack.getMaxDamage() > 0) {
                    continue; // Skip broken/near-broken tools
                }
            }

            // Silk touch check
            if (!canUseSilkTouch(targetBlock, itemStack)) {
                continue;
            }

            // Get mining speed
            float speed = itemStack.getStrVsBlock(targetBlock);

            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    /**
     * Checks if item's silk touch enchantment is appropriate for the block
     */
    private boolean canUseSilkTouch(Block block, ItemStack itemStack) {
        // Blocks that should be mined with silk touch
        boolean shouldHaveSilkTouch = block == Blocks.ender_chest ||
                block == Blocks.glowstone ||
                block == Blocks.sea_lantern;

        if (!shouldHaveSilkTouch) {
            return true; // Block doesn't require silk touch
        }

        // Check if item has silk touch
        int silkTouchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, itemStack);

        // Return false if block needs silk touch but item doesn't have it
        return silkTouchLevel > 0;
    }

    /**
     * Switches to a slot with configurable delay
     */
    private void switchToSlotWithDelay(int slot) {
        if (this.previousHotbarSlotLB == -1) {
            // First switch - save current slot
            this.previousHotbarSlotLB = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.inventory.currentItem = slot;
        } else if (this.tickCounterLB >= this.swapPreviousDelay.getValue()) {
            // Delay elapsed - confirm switch
            mc.thePlayer.inventory.currentItem = slot;
            this.tickCounterLB = 0;
            this.previousHotbarSlotLB = -1;
        } else {
            this.tickCounterLB++;
        }
    }

    /**
     * Resets LiquidBounce mode state
     */
    private void resetLiquidBounceState() {
        this.tickCounterLB = 0;
        this.previousHotbarSlotLB = -1;
    }

    @Override
    public void onDisabled() {
        this.currentToolSlot = -1;
        this.previousSlot = -1;
        this.tickDelayCounter = 0;
        resetLiquidBounceState();
    }

    @Override
    public String[] getSuffix() {
        return new String[]{mode.getModeString()};
    }
}