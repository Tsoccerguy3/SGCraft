//------------------------------------------------------------------------------------------------
//
//   SG Craft - Main Class
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import gcewing.sg.ic2.zpm.ZPMItem;
import gcewing.sg.ic2.zpm.ZpmContainer;
import gcewing.sg.ic2.zpm.ZpmInterfaceCart;
import gcewing.sg.ic2.zpm.ZpmInterfaceCartTE;
import gcewing.sg.oc.OCIntegration;
import gcewing.sg.rf.RFIntegration;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static java.util.Objects.requireNonNull;
import static net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import static net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

// import dan200.computercraft.api.*; //[CC]

@Mod(modid = Info.modID, name = Info.modName, version = Info.versionNumber,
    acceptableRemoteVersions = Info.versionBounds, dependencies = "after:opencomputers;after:ic2;after:computercraft")

public class SGCraft extends BaseMod<SGCraftClient> {

    public static final Material machineMaterial = new Material(MapColor.IRON);

    public static SGCraft mod;

    public static SGChannel channel;
    public static BaseTEChunkManager chunkManager;
    
    public static SGBaseBlock sgBaseBlock;
    public static SGRingBlock sgRingBlock;
    public static DHDBlock sgControllerBlock;
    //public static SGPortalBlock sgPortalBlock;
    public static Block naquadahBlock, naquadahOre;
    
    public static Item naquadah, naquadahIngot, sgCoreCrystal, sgControllerCrystal, sgChevronUpgrade,
        sgIrisUpgrade, sgIrisBlade;
    
    public static Block ic2PowerUnit;
    public static Item ic2Capacitor;
    public static Block rfPowerUnit;
    
    public static boolean addOresToExistingWorlds;
    public static NaquadahOreWorldGen naquadahOreGenerator;
//     public static int tokraVillagerID;
    
    public static BaseSubsystem ic2Integration; //[IC2]
    public static IIntegration ccIntegration; //[CC]
    public static OCIntegration ocIntegration; //[OC]
    public static RFIntegration rfIntegration; //[RF]
//     public static MystcraftIntegration mystcraftIntegration; //[MYST]

    public static Block zpm_interface_cart;
    public static Item zpm, zpm_interface_cart_item;

    public static CreativeTabs creativeTabs;

    public SGCraft() {
        mod = this;

    }

    @Mod.EventHandler
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        this.creativeTab = new CreativeTabs("sgcraft:sgcraft") {
            @Override
            public ItemStack getTabIconItem() {
                return new ItemStack(Item.getItemFromBlock(sgBaseBlock));
            }
        };
        FMLCommonHandler.instance().bus().register(this);
        rfIntegration = (RFIntegration) integrateWithMod("redstoneflux", "gcewing.sg.rf.RFIntegration"); //[RF]
        ic2Integration = integrateWithMod("ic2", "gcewing.sg.ic2.IC2Integration"); //[IC2]
        ccIntegration = (IIntegration) integrateWithMod("computercraft", "gcewing.sg.cc.CCIntegration"); //[CC]
        ocIntegration = (OCIntegration)integrateWithMod("opencomputers", "gcewing.sg.oc.OCIntegration"); //[OC]
//         mystcraftIntegration = (MystcraftIntegration)integrateWithMod("Mystcraft", "gcewing.sg.MystcraftIntegration"); //[MYST]

        GameRegistry.registerTileEntity(ZpmInterfaceCartTE.class, new ResourceLocation(this.modID));
        super.preInit(e);
    }
    
    @Mod.EventHandler
    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        System.out.printf("SGCraft.init\n");
        configure();
        channel = new SGChannel(Info.modID);
        chunkManager = new BaseTEChunkManager(this);
    }

    @Mod.EventHandler
    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }

    @Override   
    protected SGCraftClient initClient() {
        return new SGCraftClient(this);
    }

    @Override
    void configure() {
        DHDTE.configure(config);
        NaquadahOreWorldGen.configure(config);
        SGBaseBlock.configure(config);
        SGBaseTE.configure(config);
        FeatureGeneration.configure(config);
        addOresToExistingWorlds = config.getBoolean("options", "addOresToExistingWorlds", false);
    }       

    @Override
    protected void registerOther() {
        MinecraftForge.TERRAIN_GEN_BUS.register(this);
    }

    @Override
    protected void registerBlocks() {
        sgRingBlock = newBlock("stargateRing", SGRingBlock.class, SGRingItem.class);
        sgBaseBlock = newBlock("stargateBase", SGBaseBlock.class);
        sgControllerBlock = newBlock("stargateController", DHDBlock.class);
        //sgPortalBlock = newBlock("stargatePortal", SGPortalBlock.class);
        naquadahBlock = newBlock("naquadahBlock", NaquadahBlock.class);
        naquadahOre = newBlock("naquadahOre", NaquadahOreBlock.class);
        if (isModLoaded("ic2")) {
            zpm_interface_cart = newBlock("zpm_interface_cart", ZpmInterfaceCart.class);
        }
    }
    
    @Override
    protected void registerItems() {
        naquadah = newItem("naquadah"); //, "Naquadah");
        naquadahIngot = newItem("naquadahIngot"); //, "Naquadah Alloy Ingot");
        sgCoreCrystal = newItem("sgCoreCrystal"); //, "Stargate Core Crystal");
        sgControllerCrystal = newItem("sgControllerCrystal"); //, "Stargate Controller Crystal");
        sgChevronUpgrade = addItem(new SGChevronUpgradeItem(), "sgChevronUpgrade");
        sgIrisUpgrade = addItem(new SGIrisUpgradeItem(), "sgIrisUpgrade");
        sgIrisBlade = newItem("sgIrisBlade");
        if (isModLoaded("ic2") || !isModLoaded("thermalexpansion")) {
            ic2Capacitor = newItem("ic2Capacitor");
        }
        if (isModLoaded("ic2")) {
            zpm = addItem(new ZPMItem(), "zpm");
        }
    }

    @SideOnly(Side.CLIENT)
    public static void playSound(SoundSource source, SoundEvent sound) {
        playSound(source, sound, SoundCategory.AMBIENT);
    }

    @SideOnly(Side.CLIENT)
    public static void playSound(SoundSource source, SoundEvent sound, SoundCategory category) {
        SoundHandler soundHandler = getSoundHandler();
        soundHandler.playSound(new Sound(source, sound, category));
    }

    @SideOnly(Side.CLIENT)
    private static SoundHandler getSoundHandler() {
        return Minecraft.getMinecraft().getSoundHandler();
    }
    
    public static boolean isValidStargateUpgrade(Item item) {
        return item == sgChevronUpgrade || item == sgIrisUpgrade;
    }
    
    @Override
    protected void registerOres() {
        addOre("oreNaquadah", naquadahOre);
        addOre("naquadah", naquadah);
        addOre("ingotNaquadahAlloy", naquadahIngot);
    }

    @Override
    protected void registerRecipes() {
        ItemStack chiselledSandstone = new ItemStack(Blocks.SANDSTONE, 1, 1);
        ItemStack smoothSandstone = new ItemStack(Blocks.SANDSTONE, 1, 2);
        ItemStack sgChevronBlock = new ItemStack(sgRingBlock, 1, 1);
        ItemStack blueDye = new ItemStack(Items.DYE, 1, 4);
        ItemStack orangeDye = new ItemStack(Items.DYE, 1, 14);

        if (config.getBoolean("options", "allowCraftingNaquadah", false))
            newShapelessRecipe("naquada",naquadah, 1, Ingredient.fromItems(Items.COAL, Items.SLIME_BALL, Items.BLAZE_POWDER));
        newRecipe("sgringblock", sgRingBlock, 1, "CCC", "NNN", "SSS",
            'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone);
        newRecipe("sgcheveronblock", sgChevronBlock, "CgC", "NpN", "SrS",
            'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone,
            'g', Items.GLOWSTONE_DUST, 'r', Items.REDSTONE, 'p', Items.ENDER_PEARL);
        newRecipe("sgbaseblock", sgBaseBlock, 1, "CrC", "NeN", "ScS",
            'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone,
            'r', Items.REDSTONE, 'e', Items.ENDER_EYE, 'c', sgCoreCrystal);
        newRecipe("sgcontrollerblock", sgControllerBlock, 1, "bbb", "OpO", "OcO",
            'b', Blocks.STONE_BUTTON, 'O', Blocks.OBSIDIAN, 'p', Items.ENDER_PEARL,
            'c', sgControllerCrystal);
        newShapelessRecipe("naquadahingot",naquadahIngot, 1, Ingredient.fromItem(Items.IRON_INGOT),
                Ingredient.fromItem(naquadah));
        newRecipe("naquadahblock", naquadahBlock, 1, "NNN", "NNN", "NNN", 'N', "ingotNaquadahAlloy");
        newRecipe("sgchevronupgrade", sgChevronUpgrade, 1, "g g", "pNp", "r r",
            'N', "ingotNaquadahAlloy",
            'g', Items.GLOWSTONE_DUST, 'r', Items.REDSTONE, 'p', Items.ENDER_PEARL);
        newRecipe("naquadahingot_from_block", naquadahIngot, 9, "B", 'B', naquadahBlock);
        newRecipe("sgirisblade", sgIrisBlade, 1, " ii", "ic ", "i  ",
            'i', Items.IRON_INGOT, 'c', new ItemStack(Items.COAL, 1, 1));
        newRecipe("sgirisupgrade", sgIrisUpgrade, 1, "bbb", "brb", "bbb",
            'b', sgIrisBlade, 'r', Items.REDSTONE);
        if (config.getBoolean("options", "allowCraftingCrystals", false)) {
            newRecipe("sgcorecrystal", sgCoreCrystal, 1, "bbr", "rdb", "brb",
                'b', blueDye, 'r', Items.REDSTONE, 'd', Items.DIAMOND);
            newRecipe("sgcontrollercrystal", sgControllerCrystal, 1, "roo", "odr", "oor",
                'o', orangeDye, 'r', Items.REDSTONE, 'd', Items.DIAMOND);
        }
        if (!isModLoaded("ic2"))
            addGenericCapacitorRecipe();
    }
    
    protected void addGenericCapacitorRecipe() {
        newRecipe("ic2capacitor", ic2Capacitor, 1, "iii", "ppp", "iii",
            'i', "ingotIron", 'p', "paper");
    }

    @Override
    protected void registerContainers() {
        addContainer(SGGui.SGBase, SGBaseContainer.class);
        addContainer(SGGui.DHDFuel, DHDFuelContainer.class);
        addContainer(SGGui.PowerUnit, PowerContainer.class);
        addContainer(SGGui.ZPMInterfaceCart, ZpmContainer.class);
    }
   
    @Override
    protected void registerWorldGenerators() {
        if (config.getBoolean("options", "enableNaquadahOre", true)) {
            System.out.printf("SGCraft: Registering NaquadahOreWorldGen\n");
            naquadahOreGenerator = new NaquadahOreWorldGen();
            GameRegistry.registerWorldGenerator(naquadahOreGenerator, 0);
        }
        MapGenStructureIO.registerStructureComponent(FeatureUnderDesertPyramid.class, "SGCraft:FeatureUnderDesertPyramid");
    }
    
    @Override //[VILL]
    protected void registerVillagers() {
        VillagerProfession tokraProfession = new VillagerProfession("sgcraft:tokra", "sgcraft:textures/skins/tokra.png","sgcraft:textures/skins/tokra.png");
        // Update: Needs new skin for Zombie mode?
        VillagerCareer tokraCareer = new VillagerCareer(tokraProfession, "sgcraft:tokra");
        tokraCareer.addTrade(1, new SGTradeHandler());
        ForgeRegistries.VILLAGER_PROFESSIONS.register(tokraProfession);
    }

    @Override
    protected void registerEntities() {
        addEntity(EntityStargateIris.class, "stargate_iris", SGEntity.Iris, 1000000, false);
    }
    
    @Override
    protected void registerSounds() {
        SGBaseTE.registerSounds(this);
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent.Load e) {
        Chunk chunk = e.getChunk();
        SGChunkData.onChunkLoad(e);
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save e) {
        Chunk chunk = e.getChunk();
        SGChunkData.onChunkSave(e);
    }
    
    @SubscribeEvent
    public void onInitMapGen(InitMapGenEvent e) {
        FeatureGeneration.onInitMapGen(e);
    }
    
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        switch (e.phase) {
            case START: {
                for (BaseSubsystem om : subsystems)
                    if (om instanceof IIntegration)
                        ((IIntegration)om).onServerTick();
                break;
            }
        }
    }
    
    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload e) {
        Chunk chunk = e.getChunk();
        if (!chunk.getWorld().isRemote) {
            for (Object obj : chunk.getTileEntityMap().values()) {
                if (obj instanceof SGBaseTE) {
                    SGBaseTE te = (SGBaseTE)obj;
                    te.disconnect();
                }
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onModelRegistry(ModelRegistryEvent event) {
        // Register Complex Block Models
        // Note: Complex Item Models register within their creation class because their registration order isn't important.
        registerModel(Item.getItemFromBlock(SGCraft.zpm_interface_cart));
    }

    @SideOnly(Side.CLIENT)
    private void registerModel(Item item) {
        this.registerInventoryModel(item, requireNonNull(item.getRegistryName()));
    }

    @SideOnly(Side.CLIENT)
    private void registerInventoryModel(Item item, ResourceLocation blockName) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(blockName, "inventory"));
    }

}
