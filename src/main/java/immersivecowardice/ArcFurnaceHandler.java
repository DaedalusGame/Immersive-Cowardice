package immersivecowardice;

import blusunrize.immersiveengineering.common.blocks.metal.ArcFurnaceTileEntity;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArcFurnaceHandler {
    @SubscribeEvent
    public void OnAttachCapability(AttachCapabilitiesEvent<TileEntity> event) {
        if(!CommonConfig.INSTANCE.arcFurnaceEnable.get())
            return;
        TileEntity tile = event.getObject();
        if(tile instanceof ArcFurnaceTileEntity) {
            event.addCapability(new ResourceLocation(ImmersiveCowardice.MODID,"arc_furnace_electrode_handler"), new ArcFurnaceElectrodeHandler((ArcFurnaceTileEntity) tile));
        }
    }

    public static class ArcFurnaceElectrodeHandler implements ICapabilityProvider, IItemHandler {
        private static final BlockPos ELECTRODE_POS = new BlockPos(2, 4, 2);

        private final LazyOptional<IItemHandler> holder = LazyOptional.of(() -> this);
        private ArcFurnaceTileEntity tile;
        private ArcFurnaceTileEntity master;
        private IEInventoryHandler handler;

        public ArcFurnaceElectrodeHandler(ArcFurnaceTileEntity tile) {
            this.tile = tile;
        }

        private void setupHandler() {
            ArcFurnaceTileEntity currentMaster = tile.master();
            if(this.master != currentMaster) {
                this.handler = new IEInventoryHandler(3, currentMaster, 23, true, false);
                this.master = currentMaster;
            }
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
            if(tile.master() != null && tile.posInMultiblock.equals(ELECTRODE_POS))
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(capability, holder);
            else
                return LazyOptional.empty();
        }

        @Override
        public int getSlots() {
            return 3;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            setupHandler();
            return handler.getStackInSlot(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            setupHandler();
            ItemStack remainder = handler.insertItem(slot, stack, simulate);
            if(remainder.getCount() < stack.getCount() && !simulate) { //Something changed, update the arc furnace.
                BlockState state = master.getState();
                master.getWorld().notifyBlockUpdate(master.getPos(), state, state, 2);
            }

            return remainder;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            setupHandler();
            return handler.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            setupHandler();
            return handler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return !stack.isEmpty() && IEItems.Misc.graphiteElectrode.equals(stack.getItem()); //yeah, ok, i guess you could have used tags
        }
    }
}
