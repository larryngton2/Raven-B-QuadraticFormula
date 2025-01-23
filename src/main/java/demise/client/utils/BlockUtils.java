package demise.client.utils;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;

public class BlockUtils {
    public static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean isSamePos(BlockPos blockPos, BlockPos blockPos2) {
        return blockPos == blockPos2 || (blockPos.getX() == blockPos2.getX() && blockPos.getY() == blockPos2.getY() && blockPos.getZ() == blockPos2.getZ());
    }

    public static boolean notFull(Block block) {
        return block instanceof BlockFenceGate || block instanceof BlockLadder || block instanceof BlockFlowerPot || block instanceof BlockBasePressurePlate || isFluid(block) || block instanceof BlockFence || block instanceof BlockAnvil || block instanceof BlockEnchantmentTable || block instanceof BlockChest;
    }

    public static boolean isFluid(Block block) {
        return block.getMaterial() == Material.lava || block.getMaterial() == Material.water;
    }

    public static boolean isInteractable(Block block) {
        return block instanceof BlockFurnace || block instanceof BlockTrapDoor || block instanceof BlockDoor || block instanceof BlockContainer || block instanceof BlockJukebox || block instanceof BlockFenceGate || block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockEnchantmentTable || block instanceof BlockBrewingStand || block instanceof BlockBed || block instanceof BlockDropper || block instanceof BlockDispenser || block instanceof BlockHopper || block instanceof BlockAnvil || block instanceof BlockNote || block instanceof BlockWorkbench;
    }

    public static boolean isInteractable(MovingObjectPosition mv) {
        if (mv == null || mv.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || mv.getBlockPos() == null) {
            return false;
        }
        IBlockState iblockstate = mc.theWorld.getBlockState(mv.getBlockPos());
        if (!mc.thePlayer.isSneaking() || mc.thePlayer.getHeldItem() == null || mc.thePlayer.getHeldItem().getItem().doesSneakBypassUse(mc.theWorld, mv.getBlockPos(), mc.thePlayer)) {
            Vec3 hitVec = getHitVec(mv.hitVec, mv.getBlockPos());
            return iblockstate.getBlock().onBlockActivated(mc.theWorld, mv.getBlockPos(), iblockstate, mc.thePlayer, mv.sideHit, (float) hitVec.xCoord, (float) hitVec.yCoord, (float) hitVec.zCoord);
        }
        return false;
    }

    public static Vec3 getHitVec(Vec3 hitVec, BlockPos blockPos) {
        float x = (float)(hitVec.xCoord - blockPos.getX());
        float y = (float)(hitVec.yCoord - blockPos.getY());
        float z = (float)(hitVec.zCoord - blockPos.getZ());
        return new Vec3(x, y, z);
    }

    public static float getBlockHardness(final Block block, final ItemStack itemStack, boolean ignoreSlow, boolean ignoreGround) {
        final float getBlockHardness = block.getBlockHardness(mc.theWorld, null);
        if (getBlockHardness < 0.0f) {
            return 0.0f;
        }
        return (block.getMaterial().isToolNotRequired() || (itemStack != null && itemStack.canHarvestBlock(block))) ? (getToolDigEfficiency(itemStack, block, ignoreSlow, ignoreGround) / getBlockHardness / 30.0f) : (getToolDigEfficiency(itemStack, block, ignoreSlow, ignoreGround) / getBlockHardness / 100.0f);
    }

    public static float getToolDigEfficiency(ItemStack itemStack, Block block, boolean ignoreSlow, boolean ignoreGround) {
        float n = (itemStack == null) ? 1.0f : itemStack.getItem().getStrVsBlock(itemStack, block);
        if (n > 1.0f) {
            final int getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
            if (getEnchantmentLevel > 0 && itemStack != null) {
                n += getEnchantmentLevel * getEnchantmentLevel + 1;
            }
        }
        if (mc.thePlayer.isPotionActive(Potion.digSpeed)) {
            n *= 1.0f + (mc.thePlayer.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2f;
        }
        if (!ignoreSlow) {
            if (mc.thePlayer.isPotionActive(Potion.digSlowdown)) {
                float n2;
                switch (mc.thePlayer.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
                    case 0: {
                        n2 = 0.3f;
                        break;
                    }
                    case 1: {
                        n2 = 0.09f;
                        break;
                    }
                    case 2: {
                        n2 = 0.0027f;
                        break;
                    }
                    default: {
                        n2 = 8.1E-4f;
                        break;
                    }
                }
                n *= n2;
            }
            if (mc.thePlayer.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(mc.thePlayer)) {
                n /= 5.0f;
            }
            if (!mc.thePlayer.onGround && !ignoreGround) {
                n /= 5.0f;
            }
        }
        return n;
    }

    public static Block getBlock(BlockPos blockPos) {
        return getBlockState(blockPos).getBlock();
    }

    public static Block getBlock(double x, double y, double z) {
        return getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static IBlockState getBlockState(BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos);
    }

    public static boolean check(final BlockPos blockPos, final Block block) {
        return getBlock(blockPos) == block;
    }

    public static boolean replaceable(BlockPos blockPos) {
        if (!Utils.Player.nullCheck()) {
            return true;
        }
        return getBlock(blockPos).isReplaceable(mc.theWorld, blockPos);
    }

    public static boolean isBlockUnder() {
        if (mc.thePlayer.posY >= 0.0) {
            for (int offset = 0; offset < (int) mc.thePlayer.posY + 2; offset += 2) {
                AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0.0, (double) (-offset), 0.0);
                if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                    return true;
                }
            }

        }
        return false;
    }

    public static boolean isBlockUnder(int distance) {
        for(int y = (int)mc.thePlayer.posY; y >= (int)mc.thePlayer.posY - distance; --y) {
            if (!(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, y, mc.thePlayer.posZ)).getBlock() instanceof BlockAir)) {
                return true;
            }
        }

        return false;
    }

    public static boolean canBePlaced(ItemBlock itemBlock) {
        Block block = itemBlock.getBlock();
        if (block == null) {
            return false;
        }
        return !BlockUtils.isInteractable(block) && !(block instanceof BlockSapling) && !(block instanceof BlockDaylightDetector) && !(block instanceof BlockBeacon) && !(block instanceof BlockBanner) && !(block instanceof BlockEndPortalFrame) && !(block instanceof BlockEndPortal) && !(block instanceof BlockLever) && !(block instanceof BlockButton) && !(block instanceof BlockSkull) && !(block instanceof BlockLiquid) && !(block instanceof BlockCactus) && !(block instanceof BlockDoublePlant) && !(block instanceof BlockLilyPad) && !(block instanceof BlockCarpet) && !(block instanceof BlockTripWire) && !(block instanceof BlockTripWireHook) && !(block instanceof BlockTallGrass) && !(block instanceof BlockFlower) && !(block instanceof BlockFlowerPot) && !(block instanceof BlockSign) && !(block instanceof BlockLadder) && !(block instanceof BlockTorch) && !(block instanceof BlockRedstoneTorch) && !(block instanceof BlockFence) && !(block instanceof BlockPane) && !(block instanceof BlockStainedGlassPane) && !(block instanceof BlockGravel) && !(block instanceof BlockClay) && !(block instanceof BlockSand) && !(block instanceof BlockSoulSand) && !(block instanceof BlockRail);
    }
}