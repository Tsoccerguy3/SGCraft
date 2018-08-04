//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Controller Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.world.*;
import net.minecraft.util.math.*;

import static gcewing.sg.BaseBlockUtils.*;
import static gcewing.sg.BaseUtils.*;

public class DHDTE extends BaseTileInventory implements ISGEnergySource {

    // Debug options
    public static boolean debugLink = false;

    // Configuration options
    public static int linkRangeX = 5; // either side
    public static int linkRangeY = 1; // up or down
    public static int linkRangeZ = 6; // in front

    // Inventory slots
    public static final int firstFuelSlot = 0;
    public static final int numFuelSlots = 4;
    public static final int numSlots = numFuelSlots;

    // Persisted fields
    public boolean isLinkedToStargate;
    public BlockPos linkedPos = new BlockPos(0, 0, 0);
    public String enteredAddress = "";
    IInventory inventory = new InventoryBasic("DHD", false, numSlots);
    
    static AxisAlignedBB bounds;
    static double maxEnergyBuffer;

    double energyInBuffer;

    public static void configure(BaseConfiguration cfg) {
        linkRangeX = cfg.getInteger("dhd", "linkRangeX", linkRangeX);
        linkRangeY = cfg.getInteger("dhd", "linkRangeY", linkRangeY);
        linkRangeZ = cfg.getInteger("dhd", "linkRangeZ", linkRangeZ);
        maxEnergyBuffer = SGBaseTE.energyPerFuelItem;
    }
    
    public static DHDTE at(IBlockAccess world, BlockPos pos) {
        TileEntity te = getWorldTileEntity(world, pos);
        if (te instanceof DHDTE)
            return (DHDTE)te;
        else
            return null;
    }
    
    public static DHDTE at(IBlockAccess world, NBTTagCompound nbt) {
        BlockPos pos = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
        return DHDTE.at(world, pos);
    }
    
    public void setEnteredAddress(String address) {
        enteredAddress = address;
        markChanged();
    }
    
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return bounds.expand(getX() + 0.5, getY(), getZ() + 0.5);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 32768.0;
    }

    @Override
    protected IInventory getInventory() {
        return inventory;
    }
    
    public DHDBlock getBlock() {
        return (DHDBlock)getBlockType();
    }
    
//     public Trans3 localToGlobalTransformation() {
//         World world = getSoundWorld();
//         IBlockState state = world.getBlockState(pos);
//         return getBlock().localToGlobalTransformation(world, pos, state);
//     }
    
//     public int getRotation() {
//         return getBlock().rotationInWorld(getBlockMetadata(), this);
//     }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        isLinkedToStargate = nbt.getBoolean("isLinkedToStargate");
        energyInBuffer = nbt.getDouble("energyInBuffer");
        int x = nbt.getInteger("linkedX");
        int y = nbt.getInteger("linkedY");
        int z = nbt.getInteger("linkedZ");
        linkedPos = new BlockPos(x, y, z);
        enteredAddress = nbt.getString("enteredAddress");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("isLinkedToStargate", isLinkedToStargate);
        nbt.setDouble("energyInBuffer", energyInBuffer);
        nbt.setInteger("linkedX", linkedPos.getX());
        nbt.setInteger("linkedY", linkedPos.getY());
        nbt.setInteger("linkedZ", linkedPos.getZ());
        nbt.setString("enteredAddress", enteredAddress);
        return nbt;
    }

    SGBaseTE getLinkedStargateTE() {
        if (isLinkedToStargate) {
            TileEntity gte = getWorldTileEntity(world, linkedPos);
            if (gte instanceof SGBaseTE)
                return (SGBaseTE)gte;
        }
        return null;
    }

    void checkForLink() {
        if (debugLink)
            System.out.printf("DHDTE.checkForLink at %s: isLinkedToStargate = %s\n",
                pos, isLinkedToStargate);
        if (!isLinkedToStargate) {
            Trans3 t = localToGlobalTransformation();
            for (int i = -linkRangeX; i <= linkRangeX; i++)
                for (int j = -linkRangeY; j <= linkRangeY; j++)
                    for (int k = 1; k <= linkRangeZ; k++) {
                        Vector3 p = t.p(i, j, -k);
                        //System.out.printf("DHDTE: Looking for stargate at (%d,%d,%d)\n",
                        //  p.floorX(), p.floorY(), p.floorZ());
                        BlockPos bp = new BlockPos(p.floorX(), p.floorY(), p.floorZ());
                        if (debugLink)
                            System.out.printf("DHDTE.checkForLink: probing %s\n", bp);
                        TileEntity te = world.getTileEntity(bp);
                        if (te instanceof SGBaseTE) {
                            if (debugLink)
                                System.out.printf("DHDTE.checkForLink: Found stargate at %s\n",
                                    te.getPos());
                            if (linkToStargate((SGBaseTE)te))
                                return;
                        }
                    }
        }
    }
    
    boolean linkToStargate(SGBaseTE gte) {
        if (!isLinkedToStargate && !gte.isLinkedToController && gte.isMerged) {
            if (debugLink)
                System.out.printf(
                    "DHDTE.linkToStargate: Linking controller at %s with stargate at %s\n",
                    pos, gte.getPos());
            linkedPos = gte.getPos();
            isLinkedToStargate = true;
            markChanged();
            gte.linkedPos = pos;
            gte.isLinkedToController = true;
            gte.markChanged();
            return true;
        }
        return false;
    }
    
    public void clearLinkToStargate() {
        if (debugLink)
            System.out.printf("DHDTE: Unlinking controller at %s from stargate\n", pos);
        isLinkedToStargate = false;
        markChanged();
    }
    
    @Override
    public double availableEnergy() {
        double energy = energyInBuffer;
        for (int i = 0; i < numFuelSlots; i++) {
            ItemStack stack = fuelStackInSlot(i);
            if (stack != null)
                energy += stack.getCount() * SGBaseTE.energyPerFuelItem;
        }
        return energy;
    }
    
    @Override
    public double drawEnergyDouble(double amount) {
        double energyDrawn = 0;
        while (energyDrawn < amount) {
            if (energyInBuffer == 0) {
                if (!useFuelItem())
                    break;
            }
            double e = min(amount, energyInBuffer);
            energyDrawn += e;
            energyInBuffer -= e;
        }
        if (SGBaseTE.debugEnergyUse)
            System.out.printf("DHDTE.drawEnergyDouble: %s; supplied: %s; buffered: %s\n",
                amount, energyDrawn, energyInBuffer);
        markChanged();
        return energyDrawn;
    }

    @Override public double totalAvailableEnergy() {
        return energyInBuffer;
    }

    boolean useFuelItem() {
        for (int i = numFuelSlots - 1; i >= 0; i--) {
            ItemStack stack = fuelStackInSlot(i);
            if (stack != null) {
                decrStackSize(i, 1);
                energyInBuffer += SGBaseTE.energyPerFuelItem;
                return true;
            }
        }
        return false;
    }
    
    ItemStack fuelStackInSlot(int i) {
        ItemStack stack = getStackInSlot(firstFuelSlot + i);
        if (isValidFuelItem(stack))
            return stack;
        else
            return null;
    }
    
    public static boolean isValidFuelItem(ItemStack stack) {
        return stack != null && stack.getItem() == SGCraft.naquadah && stack.getCount() > 0;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return isValidFuelItem(stack);
    }

}
