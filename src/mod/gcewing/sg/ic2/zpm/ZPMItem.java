package gcewing.sg.ic2.zpm;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;

public class ZPMItem extends Item {
  static final String ENERGY = "AvailableEnergy";
  static final String LOADED = "loadedIntoCart";
  final static DecimalFormat dFormat = new DecimalFormat("###,###,###,##0");

  public ZPMItem() {}
  // Nothing here... yet.

  @SideOnly(Side.CLIENT)
  @Override
  public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
    super.addInformation(stack, player, tooltip, advanced);

    final NBTTagCompound compound = stack.getTagCompound();
    if (compound != null) {
      if (compound.hasKey(ZPMItem.LOADED, 99 /* number */)) {
        if (!compound.getBoolean(ZPMItem.LOADED)) {
          tooltip.add("Power: " + dFormat.format(compound.getDouble(ZPMItem.ENERGY)));
        }
      }
      return;
    }
  }
}
