package gcewing.sg.almura;

import gcewing.sg.ISGEnergySource;
import gcewing.sg.ic2.zpm.ZpmInterfaceCartTE;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AlmuraAddon {

    // Note: The following code has been added for support with specific added methods within SGBaseTE.
    final static boolean debugAddon = true;

    public static boolean worldRequiresZPM(String origin, String destination) {
        if (origin.equalsIgnoreCase("dakara") || origin.equalsIgnoreCase("nether") || origin.equalsIgnoreCase("the_end") || origin.equalsIgnoreCase("asgard") || origin.equalsIgnoreCase("orilla")) {
            if (destination.equalsIgnoreCase("dakara") || destination.equalsIgnoreCase("nether") || destination.equalsIgnoreCase("the_end") || destination.equalsIgnoreCase("asgard") || destination.equalsIgnoreCase("orilla")) {
                if (debugAddon) {
                    System.out.println("SGCraft:AlmuraAddon - Origin: " + origin + " -> Destination: " + destination + " Zpm Required: false");
                }
                return false;
            }
        }
        if (debugAddon) {
            System.out.println("SGCraft:AlmuraAddon - Origin: " + origin + " -> Destination: " + destination + " Zpm Required: true");
        }
        return true;
    }

    public static int worldZpmMultiplier(String world) {
        int multiplier = 0;
        if (world.equalsIgnoreCase("cemaria") || world.equalsIgnoreCase("cemaria-nether") || world.equalsIgnoreCase("cemaria-end")) {
            multiplier = 1000;
        } else if (world.equalsIgnoreCase("keystone") || world.equalsIgnoreCase("keystone-nether") || world.equalsIgnoreCase("keystone-end")) {
            multiplier = 1000;
        } else if (world.equalsIgnoreCase("atlantis") || world.equalsIgnoreCase("atlantis-nether") || world.equalsIgnoreCase("atlantis-end")) {
            multiplier = 1000;
        }

        if (debugAddon) {
            System.out.println("SGCraft:AlmuraAddon - Destination: " + world + " Zpm Multiplier: " + multiplier);
        }

        return multiplier;
    }

    public static double zpmPowerAvailable(World world, BlockPos pos, int radius, boolean debug) {
        double zpmPower = 0.0;
        for (final BlockPos.MutableBlockPos nearPos : BlockPos.getAllInBoxMutable(
            pos.add(-radius, -radius, -radius),
            pos.add(radius, radius, radius)
        )) {
            TileEntity nte = world.getTileEntity(nearPos);
            if (nte != null) {
                if (nte instanceof ZpmInterfaceCartTE) {
                    if (debug)
                        System.out.printf("SGBaseTE.zpmInterfaceCartNear: %s at %s\n", nte, nearPos);
                    zpmPower = ((ISGEnergySource) nte).totalAvailableEnergy();
                    if (debug)
                        System.out.println("ZPMPowerAvailable: " + zpmPower);
                    break;
                }
            }
        }
        if (debugAddon) {
            System.out.println("SGCraft:AlmuraAddon - Power Available:" + zpmPower);
        }
        return zpmPower;
    }
    // Almura END
}
