//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate controller fuelling gui container
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.ic2.zpm;

import gcewing.sg.BaseContainer;
import gcewing.sg.DHDFuelScreen;
import gcewing.sg.DHDTE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ZpmContainer extends BaseContainer {

    static final int numFuelSlotColumns = 2;
    static final int zpmSlotsX = 120;
    static final int zpmSlotsY = 54;
    static final int playerSlotsX = 48;
    static final int playerSlotsY = 124;

    ZpmInterfaceCartTE te;
    
    public static ZpmContainer create(EntityPlayer player, World world, BlockPos pos) {
        ZpmInterfaceCartTE te =ZpmInterfaceCartTE.at(world, pos);
        if (te != null)
            return new ZpmContainer(player, te);
        else
            return null;
    }
    
    public ZpmContainer(EntityPlayer player, ZpmInterfaceCartTE te) {
        super(ZPMInterfaceCartScreen.guiWidth, ZPMInterfaceCartScreen.guiHeight);
        this.te = te;
        addZpmSlots();
        addPlayerSlots(player, playerSlotsX, playerSlotsY);
    }
    
    void addZpmSlots() {
        int b = ZpmInterfaceCartTE.firstZpmSlot;
        int n = ZpmInterfaceCartTE.numZpmSlots;
        for (int i = 0; i < n; i++) {
            int row = i / numFuelSlotColumns;
            int col = i % numFuelSlotColumns;
            int x = zpmSlotsX + col * 18;
            int y = zpmSlotsY + row * 18;
            System.out.println("ZPM Slotzs: " + x + " / " + y);
            addSlotToContainer(new ZpmSlot(te, b + i, x, y));
        }
    }

    @Override
    protected SlotRange transferSlotRange(int srcSlotIndex, ItemStack stack) {
        SlotRange range = new SlotRange();
        range.firstSlot = DHDTE.firstFuelSlot;
        range.numSlots = DHDTE.numFuelSlots;
        return range;
    }
}

//------------------------------------------------------------------------------------------------

class ZpmSlot extends Slot {

    public ZpmSlot(IInventory inv, int i, int x, int y) {
        super(inv, i, x, y);
    }
    
    public boolean isItemValid(ItemStack stack) {
        return ZpmInterfaceCartTE.isValidFuelItem(stack);
    }

}

