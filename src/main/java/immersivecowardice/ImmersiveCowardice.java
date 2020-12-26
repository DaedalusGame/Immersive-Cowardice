package immersivecowardice;

import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.mixin.accessors.FurnaceTEAccess;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ImmersiveCowardice.MODID)
public class ImmersiveCowardice {
    public static final String MODID = "immersivecowardice";
    private static final Logger LOGGER = LogManager.getLogger();

    public ImmersiveCowardice() {
        CommonConfig.register();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ArcFurnaceHandler());
    }

    private void setup(final FMLCommonSetupEvent event) {
        if(CommonConfig.INSTANCE.furnaceHeaterEnable.get())
            ExternalHeaterHandler.registerHeatableAdapter(AbstractFurnaceTileEntity.class, new AnyFurnaceAdapter());
    }

    public static class AnyFurnaceAdapter extends ExternalHeaterHandler.HeatableAdapter<AbstractFurnaceTileEntity> {

        @Override
        public int doHeatTick(AbstractFurnaceTileEntity tile, int energyAvailable, boolean redstone) {
            int energyConsumed = 0;

            if(redstone) { //TODO: work exactly like the IE furnace handler instead of this jankass shit
                World world = tile.getWorld();
                BlockPos pos = tile.getPos();
                BlockState tileState = world.getBlockState(pos);
                IIntArray furnaceData = ((FurnaceTEAccess)tile).getFurnaceData();
                int burnTime = furnaceData.get(0);
                int fePerUnit = CommonConfig.INSTANCE.furnaceHeaterCost.get();
                if(burnTime < CommonConfig.INSTANCE.furnaceHeaterMaxBurnTime.get()) {
                    int addUnits = MathHelper.clamp(CommonConfig.INSTANCE.furnaceHeaterMaxBurnTime.get() - burnTime, 0, Math.min(CommonConfig.INSTANCE.furnaceHeaterSpeed.get(), energyAvailable / fePerUnit));
                    if(addUnits > 0) { //Add those units
                        energyConsumed = addUnits * fePerUnit;
                        furnaceData.set(0, burnTime + addUnits);
                        if(!tileState.get(AbstractFurnaceBlock.LIT))
                            world.setBlockState(pos,tileState.with(AbstractFurnaceBlock.LIT, true));
                    }
                }
            }

            return energyConsumed;
        }
    }
}
