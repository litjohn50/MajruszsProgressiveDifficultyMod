package com.majruszs_difficulty.blocks;

import com.majruszs_difficulty.Instances;
import com.majruszs_difficulty.MajruszsDifficulty;
import com.majruszs_difficulty.config.GameStateIntegerConfig;
import com.mlib.MajruszLibrary;
import com.mlib.config.AvailabilityConfig;
import com.mlib.config.ConfigGroup;
import com.mlib.config.DoubleConfig;
import com.mlib.config.DurationConfig;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;

import static com.majruszs_difficulty.MajruszsDifficulty.FEATURES_GROUP;

/** New late game crystal ore located in The End. */
@Mod.EventBusSubscriber
public class EndShardOre extends Block {
	protected final ConfigGroup configGroup;
	protected final AvailabilityConfig availability;

	public EndShardOre() {
		super( AbstractBlock.Properties.of( Material.METAL, MaterialColor.COLOR_YELLOW )
			.harvestLevel( 4 )
			.requiresCorrectToolForDrops()
			.strength( 30.0f /* hardness */, 1200.0f /* resistance */ )
			.sound( SoundType.ANCIENT_DEBRIS )
		);

		this.configGroup = new ConfigGroup( "EndShardOre", "Configuration for new late game ore." );
		FEATURES_GROUP.addGroup( this.configGroup );

		String availabilityComment = "Should this ore be available in survival mode? (ore generation, loot tables etc.) (requires game restart!)";
		this.availability = new AvailabilityConfig( "is_enabled", availabilityComment, true, true );
		this.configGroup.addConfig( this.availability );
	}

	public boolean isEnabled() {
		return this.availability.isEnabled();
	}

	@Override
	public int getExpDrop( BlockState state, net.minecraft.world.IWorldReader world, BlockPos position, int fortuneLevel, int silkTouchLevel ) {
		return silkTouchLevel == 0 ? MathHelper.nextInt( MajruszLibrary.RANDOM, 6, 11 ) : 0;
	}

	@SubscribeEvent
	public static void onBlockDestroying( PlayerEvent.BreakSpeed event ) {
		BlockState blockState = event.getState();
		Block block = blockState.getBlock();
		if( block.equals( Instances.END_SHARD_ORE ) ) {
			PlayerEntity player = event.getPlayer();
			player.displayClientMessage( new TranslationTextComponent( "block.majruszs_difficulty.end_shard_ore.warning" ).withStyle( TextFormatting.BOLD ), true );
		}
	}

	@SubscribeEvent
	public static void onBlockDestroy( BlockEvent.BreakEvent event ) {
		BlockState blockState = event.getState();

		if( blockState.getBlock() instanceof EndShardOre )
			targetEndermansOnEntity( event.getPlayer(), 1000.0 );
	}

	/**
	 Makes all endermans in the given distance target the entity.

	 @param target          Entity to target.
	 @param maximumDistance Maximum distance from enderman to entity.
	 */
	public static void targetEndermansOnEntity( LivingEntity target, double maximumDistance ) {
		if( !( target.getCommandSenderWorld() instanceof ServerWorld ) )
			return;

		ServerWorld world = ( ServerWorld )target.getCommandSenderWorld();
		for( Entity entity : world.getEntities( null, enderman->enderman.distanceToSqr( target ) < maximumDistance ) )
			if( entity instanceof EndermanEntity ) {
				EndermanEntity enderman = ( EndermanEntity )entity;
				LivingEntity currentEndermanTarget = enderman.getTarget();
				if( currentEndermanTarget == null || !currentEndermanTarget.isAlive() )
					enderman.setTarget( target );
			}
	}

	public static class EndShardOreItem extends BlockItem {
		public EndShardOreItem() {
			super( Instances.END_SHARD_ORE, ( new Properties() ).stacksTo( 64 )
				.tab( Instances.ITEM_GROUP ) );
		}

		@Override
		@OnlyIn( Dist.CLIENT )
		public void appendHoverText( ItemStack stack, @Nullable World world, List< ITextComponent > toolTip, ITooltipFlag flag ) {
			MajruszsDifficulty.addExtraTooltipIfDisabled( toolTip, Instances.END_SHARD_ORE.isEnabled() );
		}
	}
}
