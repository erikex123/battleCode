package razeplayer;
import battlecode.common.*;
import java.util.*;

/**
 * Created by Max_Inspiron15 on 1/10/2017.
 */
public strictfp class RobotPlayer {
    static RobotController rc;
    static Random myRand;
    @SuppressWarnings("unused")
    // Keep broadcast channels
    static int GARDENER_CHANNEL = 5;
    static int LUMBERJACK_CHANNEL = 6;
    static int ARCHON_Location = 20;

    // Keep important numbers here
    static int GARDENER_MAX = 8;
    static int LUMBERJACK_MAX = 10;

    public static void run(RobotController rc) throws GameActionException {
        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        myRand = new Random(rc.getID());
        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                runArchon();
                break;
            case GARDENER:
                runGardener();
                break;
            case SOLDIER:
                runSoldier();
                break;
            case LUMBERJACK:
                runLumberjack();
                break;
        }
    }


    static void runArchon() throws GameActionException {
        while (true) {
            try {

                Direction dir = randomDirection();
                int prevNumGard = rc.readBroadcast(GARDENER_CHANNEL);
                rc.broadcast(GARDENER_CHANNEL, 0);


//
//                if (CountHowManyRobotNearBy(rc.getLocation(), Direction.NORTH) <= 2 && prevNumGard < GARDENER_MAX && rc.onTheMap((rc.getLocation().add(Direction.NORTH,10))) && rc.canHireGardener(Direction.NORTH)){
//                    rc.hireGardener(Direction.NORTH);
//                    rc.broadcast(GARDENER_CHANNEL, prevNumGard + 1);
//                }
//                if (CountHowManyRobotNearBy(rc.getLocation(), Direction.EAST) <= 2 && prevNumGard < GARDENER_MAX && rc.onTheMap((rc.getLocation().add(Direction.EAST,10))) && rc.canHireGardener(Direction.EAST)){
//                    rc.hireGardener(Direction.EAST);
//                    rc.broadcast(GARDENER_CHANNEL, prevNumGard + 1);
//                }
//                if (CountHowManyRobotNearBy(rc.getLocation(), Direction.WEST) <= 2 && prevNumGard < GARDENER_MAX && rc.onTheMap((rc.getLocation().add(Direction.WEST,10))) && rc.canHireGardener(Direction.WEST)){
//                    rc.hireGardener(Direction.WEST);
//                    rc.broadcast(GARDENER_CHANNEL, prevNumGard + 1);
//                }
//                if (CountHowManyRobotNearBy(rc.getLocation(), Direction.SOUTH) <= 2 && prevNumGard < GARDENER_MAX && rc.onTheMap((rc.getLocation().add(Direction.SOUTH,10))) && rc.canHireGardener(Direction.SOUTH)){
//                    rc.hireGardener(Direction.SOUTH);
//                    rc.broadcast(GARDENER_CHANNEL, prevNumGard + 1);
//                }

                if (prevNumGard < GARDENER_MAX && rc.canHireGardener(dir)){
                    rc.hireGardener(dir);
                    rc.broadcast(GARDENER_CHANNEL, prevNumGard + 1);
                }


                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void runGardener() throws GameActionException {
        while (true) {
            try {
                dodge();


                int prev = rc.readBroadcast(GARDENER_CHANNEL);
                rc.broadcast(GARDENER_CHANNEL, prev+1);
                wander();


                Direction dir = randomDirection();
                int prevNumGard = rc.readBroadcast(LUMBERJACK_CHANNEL);
                if (prevNumGard <= LUMBERJACK_MAX && rc.canBuildRobot(RobotType.LUMBERJACK, dir)) {
                        rc.buildRobot(RobotType.LUMBERJACK, dir);
                        rc.broadcast(LUMBERJACK_CHANNEL, prevNumGard + 1);

                }


                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void runSoldier() throws GameActionException {
        while (true) {
            try {
                dodge();
                RobotInfo[] bots = rc.senseNearbyRobots();
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam()) {
                        Direction towards = rc.getLocation().directionTo(b.getLocation());
                        rc.fireSingleShot(towards);
                        break;
                    }
                }
                wander();
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    static void runLumberjack() throws GameActionException {
        while (true) {
            try {
                dodge();
                RobotInfo[] bots = rc.senseNearbyRobots();
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam() && rc.canStrike()) {
                        rc.strike();
                        Direction chase = rc.getLocation().directionTo(b.getLocation());
                        tryMove(chase);
                        break;
                    }
                }
                TreeInfo[] trees = rc.senseNearbyTrees();
                for (TreeInfo t : trees) {
                    if (rc.canChop(t.getLocation())) {
                        rc.chop(t.getLocation());
                        break;
                    }
                }
                if (! rc.hasAttacked()) {
                    wanderWithDirection(Direction.EAST);
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void wander() throws GameActionException {
        Direction dir = randomDirection();
        tryMove(dir);
    }


    public static Direction randomDirection() {
        return(new Direction(myRand.nextFloat()*2*(float)Math.PI));
    }

    static boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI / 2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,3);
    }


    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (!rc.hasMoved() && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        //boolean moved = rc.hasMoved();
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(!rc.hasMoved() && rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(! rc.hasMoved() && rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }

    static boolean trySidestep(BulletInfo bullet) throws GameActionException{

        Direction towards = bullet.getDir();
        MapLocation leftGoal = rc.getLocation().add(towards.rotateLeftDegrees(90), rc.getType().bodyRadius);
        MapLocation rightGoal = rc.getLocation().add(towards.rotateRightDegrees(90), rc.getType().bodyRadius);

        return(tryMove(towards.rotateRightDegrees(90)) || tryMove(towards.rotateLeftDegrees(90)));
    }

    static void dodge() throws GameActionException {
        BulletInfo[] bullets = rc.senseNearbyBullets();
        for (BulletInfo bi : bullets) {
            if (willCollideWithMe(bi)) {
                trySidestep(bi);
            }
        }

    }

    static void movementGardener(){
        RobotInfo[] nearBy = rc.senseNearbyRobots();

        for (RobotInfo R : nearBy){
            if (R.getType() == RobotType.ARCHON){

            }
        }

    }

    /**
     *  check to see how many robot are in a particular direction with respect to the inspecter
     *
     * @param location location of the inspector
     * @param DDD the particular direction
     * @return  int
     */

    static int CountHowManyRobotNearBy(MapLocation location, Direction DDD){

        RobotInfo[] R = rc.senseNearbyRobots(location, 25, rc.getTeam());

        int count = 0 ;
        for (RobotInfo senseingRobot: R){
            if( senseingRobot.getType() == RobotType.GARDENER){
                Direction dir = location.directionTo(senseingRobot.getLocation());
                if (DDD == dir){
                    count += 1;
                }
            }
        }

        return count;

    }

    static void wanderWithDirection(Direction dir) throws GameActionException{

        double rand = Math.random();

        if (0< rand  && rand <= 0.5 ){
             tryMove(dir);
        }
        if (0.5 < rand && rand <= 0.66666){
            tryMove(dir.rotateLeftDegrees(90));
        }
        if (0.66666 < rand && rand <= 0.866666){
            tryMove(dir.rotateLeftDegrees(180));
        }
        if (0.866666 < rand && rand <= 1){
            tryMove(dir.rotateLeftDegrees(270));
        }

    }


}