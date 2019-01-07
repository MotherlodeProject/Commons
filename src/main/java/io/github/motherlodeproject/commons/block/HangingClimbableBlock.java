package io.github.motherlodeproject.commons.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.VerticalEntityPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.World;

public class HangingClimbableBlock extends Block {
	public static final VoxelShape SHAPE = Block.createCubeShape(5, 0, 5, 11, 16, 11);

	public HangingClimbableBlock(Block.Settings settings) {
		super(settings);
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public VoxelShape getBoundingShape(BlockState state, BlockView world, BlockPos pos) {
		return SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, VerticalEntityPosition entityPosition) {
		return VoxelShapes.empty();
	}

	public boolean isSuitablePos(BlockView world, BlockPos pos) {
		BlockPos posUp = pos.up();
		BlockState stateUp = world.getBlockState(posUp);
		return Block.isFaceFullCube(stateUp.getCollisionShape(world, posUp), Direction.DOWN) || stateUp.getBlock().equals(this);
	}

	@Override
	public boolean canPlaceAt(BlockState state, ViewableWorld world, BlockPos pos) {
		return isSuitablePos(world, pos);
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState blockState_1, Direction direction_1, BlockState blockState_2, IWorld iWorld_1, BlockPos blockPos_1, BlockPos blockPos_2) {
		if (!blockState_1.canPlaceAt(iWorld_1, blockPos_1)) {
			iWorld_1.getBlockTickScheduler().schedule(blockPos_1, this, 1);
		}
		return super.getStateForNeighborUpdate(blockState_1, direction_1, blockState_2, iWorld_1, blockPos_1, blockPos_2);
	}

	@Override
	public boolean activate(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, Direction face, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getStackInHand(hand);
		if (stack.getItem().equals(Item.getItemFromBlock(this))) {
			for (int i = 1; i < pos.getY(); i++) {
				BlockPos newPos = pos.down(i);
				if (world.isAir(newPos) && world.getBlockState(newPos).getBlock() != this) {
					if (stack.useOnBlock(new ItemUsageContext(player, stack, newPos, Direction.DOWN, 0.5F, 0.5F, 0.5F)) == ActionResult.SUCCESS) {
						if (player.isCreative()) {
							stack.addAmount(1);
						}
						return true;
					}
				}
			}
		}
		return super.activate(state, world, pos, player, hand, face, hitX, hitY, hitZ);
	}

	public void climb(boolean isClimbing, World world, BlockPos pos, BlockState state, Entity entity) {
		float speed = 0.2F;
		if (isClimbing) {
			entity.velocityY = speed;
			entity.velocityX = MathHelper.clamp(entity.velocityX, -0.15D, 0.15D);
			entity.velocityZ = MathHelper.clamp(entity.velocityZ, -0.15D, 0.15D);
		}
		entity.fallDistance = 0.0F;
		if (entity.pitch > 80) {
			entity.velocityY = Math.max(entity.velocityY, -1D);
		} else {
			entity.velocityY = Math.max(entity.velocityY, -0.15D);
		}
		if (entity.isSneaking()) {
			entity.velocityY = Math.max(entity.velocityY, 0.08D);
		} else {
			entity.playSoundAtEntity(this.soundGroup.getStepSound(), this.soundGroup.pitch, this.soundGroup.volume);
		}
	}
}
