package com.example.examplemod;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class CompanionFollowGoal extends Goal {
    private final CompanionEntity companion;
    private final double speed;
    private final double startDistance;
    private final double stopDistance;

    public CompanionFollowGoal(CompanionEntity companion, double speed, double startDistance, double stopDistance) {
        this.companion = companion;
        this.speed = speed;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        Player owner = companion.getOwnerPlayer();
        return companion.isFollowing()
                && owner != null
                && owner.isAlive()
                && companion.distanceToSqr(owner) > startDistance * startDistance;
    }

    @Override
    public boolean canContinueToUse() {
        Player owner = companion.getOwnerPlayer();
        return companion.isFollowing()
                && owner != null
                && owner.isAlive()
                && companion.distanceToSqr(owner) > stopDistance * stopDistance;
    }

    @Override
    public void start() {
        Player owner = companion.getOwnerPlayer();
        if (owner != null) {
            companion.getNavigation().moveTo(owner, speed);
        }
    }

    @Override
    public void tick() {
        Player owner = companion.getOwnerPlayer();
        if (owner != null && companion.tickCount % 10 == 0) {
            companion.getNavigation().moveTo(owner, speed);
        }
    }

    @Override
    public void stop() {
        companion.getNavigation().stop();
    }
}
