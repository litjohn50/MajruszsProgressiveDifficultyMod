package com.majruszs_difficulty.generation.structure_pieces;

import com.majruszs_difficulty.Instances;
import com.majruszs_difficulty.MajruszsDifficulty;
import com.majruszs_difficulty.entities.SkyKeeperEntity;
import com.mlib.MajruszLibrary;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.spawner.AbstractSpawner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.majruszs_difficulty.generation.structure_pieces.FlyingEndIslandPiece.FORGE_CHEST_RESOURCE_LOCATION;

/** All possible Flying End Island pieces. */
public class FlyingEndShipPiece extends TemplateStructurePiece {
	public static final List< ResourceLocation > SHIP_RESOURCE_LOCATIONS = new ArrayList<>();
	public static final List< Block > RANDOM_BLOCKS = new ArrayList<>();
	public static final ResourceLocation SHIP_CHEST_RESOURCE_LOCATION = MajruszsDifficulty.getLocation( "chests/end_island_ship" );
	public static final ResourceLocation SHIP_EXTRA_CHEST_RESOURCE_LOCATION = MajruszsDifficulty.getLocation( "chests/end_island_ship_extra" );
	private final Rotation rotation;

	static {
		SHIP_RESOURCE_LOCATIONS.add( MajruszsDifficulty.getLocation( "end_island_ship_military" ) );
		SHIP_RESOURCE_LOCATIONS.add( MajruszsDifficulty.getLocation( "end_island_ship_trader" ) );

		for( int i = 0; i < 40; i++ )
			RANDOM_BLOCKS.add( Blocks.IRON_BLOCK );
		for( int i = 0; i < 40; i++ )
			RANDOM_BLOCKS.add( Blocks.GOLD_BLOCK );
		for( int i = 0; i < 10; i++ )
			RANDOM_BLOCKS.add( Blocks.EMERALD_BLOCK );
		for( int i = 0; i < 5; i++ )
			RANDOM_BLOCKS.add( Blocks.DIAMOND_BLOCK );
		for( int i = 0; i < 5; i++ )
			RANDOM_BLOCKS.add( Blocks.ANCIENT_DEBRIS );
	}

	public FlyingEndShipPiece( TemplateManager templateManager, BlockPos position, Rotation rotation ) {
		super( Instances.FLYING_END_SHIP_PIECE, 0 );
		this.templatePosition = position;
		this.rotation = rotation;
		this.setupPiece( templateManager );
	}

	public FlyingEndShipPiece( TemplateManager templateManager, CompoundNBT compoundNBT ) {
		super( Instances.FLYING_END_SHIP_PIECE, compoundNBT );
		this.rotation = Rotation.valueOf( compoundNBT.getString( "Rot" ) );
		this.setupPiece( templateManager );
	}

	@Override
	protected void readAdditional( CompoundNBT compoundNBT ) {
		super.readAdditional( compoundNBT );

		compoundNBT.putString( "Rot", this.rotation.name() );
	}

	@Override
	protected void handleDataMarker( String function, BlockPos position, IServerWorld world, Random random, MutableBoundingBox boundingBox ) {
		if( function.startsWith( "chest" ) ) {
			TileEntity tileEntity = world.getTileEntity( position.down() );

			if( tileEntity instanceof ChestTileEntity ) {
				ChestTileEntity chest = ( ChestTileEntity )tileEntity;
				if( function.startsWith( "chest_forge" ) ) {
					chest.setLootTable( FORGE_CHEST_RESOURCE_LOCATION, random.nextLong() );
				} else if( function.startsWith( "chest_ship" ) ) {
					chest.setLootTable( SHIP_CHEST_RESOURCE_LOCATION, random.nextLong() );
				} else if( function.startsWith( "chest_extra" ) ) {
					if( com.mlib.Random.tryChance( 0.25 ) )
						chest.setLootTable( SHIP_EXTRA_CHEST_RESOURCE_LOCATION, random.nextLong() );
					else
						world.setBlockState( position.down(), Blocks.AIR.getDefaultState(), 2 );
				}
			}
			world.setBlockState( position, Blocks.AIR.getDefaultState(), 2 );

		} else if( function.startsWith( "end_keeper" ) ) {
			world.setBlockState( position, Blocks.AIR.getDefaultState(), 2 );
			SkyKeeperEntity monster = SkyKeeperEntity.type.create( world.getWorld() );
			if( monster != null ) {
				monster.enablePersistence();
				monster.setPosition( position.getX(), position.getY(), position.getZ() );
				world.addEntity( monster );
			}
		} else if( function.startsWith( "spawner" ) ) {
			world.setBlockState( position, Blocks.AIR.getDefaultState(), 2 );
			world.setBlockState( position.down(), Blocks.SPAWNER.getDefaultState(), 2 );
			TileEntity tileEntity = world.getTileEntity( position.down() );

			if( tileEntity instanceof MobSpawnerTileEntity ) {
				MobSpawnerTileEntity mobSpawnerTileEntity = ( MobSpawnerTileEntity )tileEntity;
				AbstractSpawner abstractSpawner = mobSpawnerTileEntity.getSpawnerBaseLogic();
				abstractSpawner.setEntityType( EntityType.ENDERMITE );
			}
		} else if( function.startsWith( "random_block" ) ) {
			Block randomBlock = RANDOM_BLOCKS.get( MajruszLibrary.RANDOM.nextInt( RANDOM_BLOCKS.size() ) );
			world.setBlockState( position, randomBlock.getDefaultState(), 2 );
		}
	}

	/** Begins assembling your structure and where the pieces needs to go. */
	public static void start( TemplateManager templateManager, BlockPos position, Rotation rotation, List< StructurePiece > pieces, Random random ) {
		BlockPos rotationOffSet = new BlockPos( 0, 0, 0 ).rotate( rotation );
		BlockPos blockpos = rotationOffSet.add( position.getX(), position.getY(), position.getZ() );

		pieces.add( new FlyingEndShipPiece( templateManager, blockpos, rotation ) );
	}

	private void setupPiece( TemplateManager templateManager ) {
		Template template = templateManager.getTemplateDefaulted( getRandomResourceLocation() );
		PlacementSettings placementsettings = ( new PlacementSettings() ).setRotation( this.rotation )
			.setMirror( Mirror.NONE );

		this.setup( template, this.templatePosition, placementsettings );
	}

	/** Returns random resource location to building. */
	private ResourceLocation getRandomResourceLocation() {
		return SHIP_RESOURCE_LOCATIONS.get( MajruszLibrary.RANDOM.nextInt( SHIP_RESOURCE_LOCATIONS.size() ) );
	}
}