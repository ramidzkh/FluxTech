package com.fusionflux.fluxtech.blocks;

import com.fusionflux.fluxtech.entity.BlockCollisionLimiter;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Gel extends Block {
    public static final BooleanProperty NORTH;
    public static final BooleanProperty EAST;
    public static final BooleanProperty SOUTH;
    public static final BooleanProperty WEST;
    public static final Map<Direction, BooleanProperty> propertyMap;
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    //protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16D, 1.0D, 16);

    static {
        NORTH = Properties.NORTH;
        EAST = Properties.EAST;
        SOUTH = Properties.SOUTH;
        WEST = Properties.WEST;
        propertyMap = new HashMap<>();
        propertyMap.put(Direction.NORTH, Properties.NORTH);
        propertyMap.put(Direction.SOUTH, Properties.SOUTH);
        propertyMap.put(Direction.EAST, Properties.EAST);
        propertyMap.put(Direction.WEST, Properties.WEST);
    }

    private final BlockCollisionLimiter limiter = new BlockCollisionLimiter();


    public Gel(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(NORTH, false).with(EAST, false).with(SOUTH, false).with(WEST, false));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        this.addCollisionEffects(world, entity);
    }

    private void addCollisionEffects(World world, Entity entity) {
        if (entity.isOnGround()) {
            if (!entity.isSneaking()) {
                if (limiter.check(world, entity)) {
                    if (entity.getVelocity().x < 5 && entity.getVelocity().x > -2 && entity.getVelocity().y < 2 && entity.getVelocity().y > -2) {
                        entity.setVelocity(entity.getVelocity().multiply(1.7, 1.0D, 1.7));
                    } else if (entity.getVelocity().x > 2 && entity.getVelocity().x < -2 && entity.getVelocity().y > 2 && entity.getVelocity().y < -2) {
                        entity.setVelocity(entity.getVelocity().multiply(1.01, 1.0D, 1.01));
                    }
                }
            }
        }
    }

    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.PUSH_ONLY;
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH);
    }

    protected boolean canConnect(WorldAccess world, BlockPos neighborPos) {
        return world.getBlockState(neighborPos).getBlock() == FluxTechBlocks.PROPULSION_GEL;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();

        return getDefaultState()
                .with(NORTH, canConnect(world, pos.north()))
                .with(SOUTH, canConnect(world, pos.south()))
                .with(EAST, canConnect(world, pos.east()))
                .with(WEST, canConnect(world, pos.west()));
    }

    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);
        return this.canRunOnTop(world, blockPos, blockState);
    }

    private boolean canRunOnTop(BlockView world, BlockPos pos, BlockState floor) {
        return floor.isSideSolidFullSquare(world, pos, Direction.UP) && !floor.isOf(FluxTechBlocks.PROPULSION_GEL);
    }

    public BlockState getStateForNeighborUpdate(
            BlockState state,
            Direction facing,
            BlockState neighborState,
            WorldAccess world,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        return state.with(propertyMap.get(facing), canConnect(world, neighborPos));
    }

}
