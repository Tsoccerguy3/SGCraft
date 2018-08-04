//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate controller fuelling gui screen
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.ic2.zpm;

import gcewing.sg.DHDFuelContainer;
import gcewing.sg.DHDTE;
import gcewing.sg.SGCraft;
import gcewing.sg.SGScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.text.DecimalFormat;

public class ZPMInterfaceCartScreen extends SGScreen {

    static String screenTitle = "ZPM Interface Cart";
    static final int guiWidth = 256;
    static final int guiHeight = 208;
    final static DecimalFormat dFormat = new DecimalFormat("###,###,###,##0");

    ZpmInterfaceCartTE te;

    public static ZPMInterfaceCartScreen create(EntityPlayer player, World world, BlockPos pos) {
        ZpmInterfaceCartTE te = ZpmInterfaceCartTE.at(world, pos);
        if (te != null)
            return new ZPMInterfaceCartScreen(player, te);
        else
            return null;
    }

    public ZPMInterfaceCartScreen(EntityPlayer player, ZpmInterfaceCartTE te) {
        super(new ZpmContainer(player, te), guiWidth, guiHeight);
        this.te = te;
    }
    
    @Override
    protected void drawBackgroundLayer() {
        bindTexture(SGCraft.mod.resourceLocation("textures/gui/zpm_interface_cart_gui.png"), 256, 256);
        drawTexturedRect(0, 0, guiWidth, guiHeight, 0, 0);

        int cx = xSize / 2;
        setTextColor(0x004c66);
        drawCenteredString(screenTitle, cx, 8);
        drawString("ZPM", 120, 45);

        if (!this.te.getStackInSlot(0).isEmpty()) {
            drawString("Available Power: " + dFormat.format(te.availableEnergy()), 60, 100);
        }
    }
}
