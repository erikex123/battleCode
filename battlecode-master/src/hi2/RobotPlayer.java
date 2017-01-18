package hi2;

import battlecode.common.*;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

/**
 * Created by Max_Inspiron15 on 1/10/2017.
 */
public strictfp class RobotPlayer {
    static RobotController rc;
    static Direction[] dirList = new Direction[4];
    static Direction goingDir;
    static Random rand;

    static final int ArchonPositionX = 99;
    static final int ArchonPositionY = 999;
    static final int ScoutEnemyBasePositionX = 100;
    static final int ScoutEnemyBasePositionY = 101;



    static int count = 0;



    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;
        initDirList();
        rand = new Random(rc.getID());
        goingDir = randomDir();

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
            case SCOUT:
                runScout();
        }
    }

    // commonly used methods
    public static Direction randomDir() {
        return dirList[rand.nextInt(4)];
    }

    public static void initDirList() {
        for (int i = 0; i < 4; i++) {
            float radians = (float) (-Math.PI + 2 * Math.PI * ((float) i) / 4);
            dirList[i] = new Direction(radians);
            System.out.println("made new direction " + dirList[i]);
        }
    }

    //wander with direction

    public static void wander() throws GameActionException {
        try {
            Direction dir = randomDir();
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void directionalTravel(Direction dir) {
        try {
            if (rc.canMove(dir)) {
                rc.move(dir);
            } else {
                wander();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // robots classes
    public static void runArchon() {

        while (true) {
            try {

                //TODO count gardeners
                //try to build gardeners
                //can you build a gardener?


                goingDir = randomDir();

                if (rc.getTeamBullets() <= 120 ){
                    buildAndCheckRobotByGardener(RobotType.GARDENER,goingDir);
                }
                else {

                    if (Math.random() < .1 && rc.canHireGardener(goingDir)) {
                        rc.hireGardener(goingDir);
                        //tryToBuild(RobotType.GARDENER, RobotType.GARDENER.bulletCost);
                    } else {
                        wander();
                    }
                    //System.out.println("bytecode usage is "+Clock.getBytecodeNum());
                    Clock.yield();
               }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
//    public static void runArchon() {
//
//        while (true) {
//            try {
//
//                //TODO count gardeners
//                //try to build gardeners
//                //can you build a gardener?
//
//
//                goingDir = randomDir();
//
//                if (rc.getTeamBullets() < 50 && rc.canHireGardener(goingDir)){
//                    rc.hireGardener(goingDir);
//                }
//                else {
//
//                    if (Math.random() < .1 && rc.canHireGardener(goingDir)) {
//                        rc.hireGardener(goingDir);
//                        //tryToBuild(RobotType.GARDENER, RobotType.GARDENER.bulletCost);
//                    } else {
//                        wander();
//                    }
//                    //System.out.println("bytecode usage is "+Clock.getBytecodeNum());
//                    Clock.yield();
//                }
//            }catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }



    static boolean checkIfSurroundinghaveGardener(MapLocation location){
        RobotInfo[] bot = rc.senseNearbyRobots();
        for (RobotInfo b : bot){

            float distanceBetween = (b.getLocation()).distanceTo(rc.getLocation());
            if (b.getType() == RobotType.GARDENER && (b.getTeam() == rc.getTeam() ) && distanceBetween <= 8){
                return true;
            }
        }

        return false;
    }

    static void tryPlantTrees(Direction dir) throws  GameActionException{
             if (rc.canPlantTree(dir)) {
                 rc.plantTree(dir);
             }
    }



    public static int GardenerCount  = 0;
    public static void runGardener() throws GameActionException {



        Direction dir = Direction.NORTH;

        int NumOfTrees = 0 ;

         boolean stableState = false;
         boolean buildTreesCompleted = false ;

         Direction facingDir = dir;

         stableState = !checkIfSurroundinghaveGardener(rc.getLocation());





        while (true) {
            try {

                if (stableState == false){
                    goingDir = randomDir();
                    if (rc.canMove(goingDir)) {
                        rc.move(goingDir);
                    }
                    stableState = !checkIfSurroundinghaveGardener(rc.getLocation());
                }else {

                    while (NumOfTrees < 5) {
                        if (rc.getTeamBullets() > 80 && rc.canBuildRobot(RobotType.SCOUT, dir)){
                            rc.buildRobot(RobotType.SCOUT, dir);
                        }
                        else if (rc.canPlantTree(dir)) {
                            rc.plantTree(dir);


                            NumOfTrees++;
                            if (NumOfTrees == 4){
                                facingDir = dir;
                                buildTreesCompleted = true;
                                break;
                            }
                        }else if (rc.getTeamBullets() >= 100){
                            buildAndCheckRobotByGardener(RobotType.LUMBERJACK, dir);
                        }

                        tryToWater();
                        tryToShake();

                        dir = dir.rotateLeftDegrees(60);


                    }

                    if (buildTreesCompleted == true){

                        for (int i = 0 ; i < 6 ; i ++) {
                            if (Math.random() > 0.5){
                                buildAndCheckRobotByGardener(RobotType.LUMBERJACK, facingDir);
                            } else{
                                buildAndCheckRobotByGardener(RobotType.SOLDIER, facingDir);
                            }




                            facingDir = facingDir.rotateLeftDegrees(60);

                        }
                        tryToWater();
                        tryToShake();
                    }





                }







                tryToWater();
                tryToShake();



                Clock.yield();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void buildAndCheckRobotByGardener(RobotType R , Direction dir) throws GameActionException{
      if (rc.canBuildRobot(R,dir)){
          rc.buildRobot(R,dir);
      }
    }


    static MapLocation giveMapLocationOfArchon(final int X, final int Y) throws GameActionException {
       float positionX = rc.readBroadcast(X);
       float positionY = rc.readBroadcast(Y);

       MapLocation archonLocation = new MapLocation(positionX, positionY);

       return archonLocation;

    }

    public static void runSoldier() {

    }

    public static void runScout() {
        boolean archonCount  = true ;
        while (true && archonCount) {
            try {

                dodge();

                MapLocation[] ml = rc.getInitialArchonLocations(rc.getTeam().opponent());


                Direction enemyBase = rc.getLocation().directionTo(ml[0]);

                RobotInfo[] bots = rc.senseNearbyRobots();

//                BulletInfo[] bullets=rc.senseNearbyBullets();


                if (rc.readBroadcast(ScoutEnemyBasePositionX) == 0 && rc.readBroadcast(ScoutEnemyBasePositionY) == 0){
                    wanderWithDirection(enemyBase);
                }else{
                    if (ThereIsEnemyBotNearBy()) {
                        for (RobotInfo b : bots) {
                            Direction dir = rc.getLocation().directionTo(b.getLocation());
                            if (b.getTeam() != rc.getTeam()) {
                                if (rc.canFireSingleShot()) {
                                    rc.fireSingleShot(dir);
                                }
                                if (rc.canMove(dir)&&dir==enemyBase) {
                                    rc.move(dir);
                                }else{
                                    rc.move(dir.rotateLeftDegrees(10));
                                }

                            }
                            if (b.getType() == RobotType.ARCHON && b.getTeam() != rc.getTeam()) {
                                rc.broadcast(ScoutEnemyBasePositionX, (int) (rc.getLocation().x));
                                rc.broadcast(ScoutEnemyBasePositionY, (int) (rc.getLocation().y));
                            }
                        }
                    }else{

                            Direction dirToArchon = rc.getLocation().directionTo(giveMapLocationOfArchon(ScoutEnemyBasePositionX,ScoutEnemyBasePositionY));
                            wanderWithDirection(dirToArchon);

                    }
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void runLumberjack() throws GameActionException {



        while (true ) {

            try {
                dodge();

                MapLocation[] ml = rc.getInitialArchonLocations(rc.getTeam().opponent());

//                if (ml.length == 2 ){
//                    Direction Archon1toAchon2 = ml[0].directionTo(ml[1]);
//                }
//                else if (ml.length == 3){
//                    Direction Archon1toAchon2 = ml[0].directionTo(ml[1]);
//                    Direction Archon2toAchon3 = ml[1].directionTo(ml[2]);
//                }

                Direction enemyBase = rc.getLocation().directionTo(ml[0]);

                RobotInfo[] bots = rc.senseNearbyRobots();


                if (ThereIsEnemyBotNearBy()) {
                    for (RobotInfo b : bots) {

                        float dist = rc.getLocation().distanceTo(b.getLocation());
                        if (b.getTeam() != rc.getTeam() && rc.canStrike() && dist <= GameConstants.LUMBERJACK_STRIKE_RADIUS + rc.getType().bodyRadius) {
                            rc.strike();

                            Direction dir = rc.getLocation().directionTo(b.getLocation());
                            if (rc.canMove(dir)) {
                                rc.move(dir);
                            }
                            break;
                        }
                        if (b.getType() == RobotType.ARCHON && b.getTeam() != rc.getTeam()) {
                            rc.strike();
                            Direction dir = rc.getLocation().directionTo(b.getLocation());
                            rc.broadcast(ArchonPositionX, (int) (rc.getLocation().x));
                            rc.broadcast(ArchonPositionY, (int) (rc.getLocation().y));
                            if (rc.canMove(dir)) {
                                rc.move(dir);
                            }
                        }
                    }
                }

                else if (!ThereIsEnemyBotNearBy()) {
                    if (rc.readBroadcast(ArchonPositionX) == 0 && rc.readBroadcast(ArchonPositionY) == 0){
                        wanderWithDirection(enemyBase);
                    } else {
                        Direction dirToArchon = rc.getLocation().directionTo(giveMapLocationOfArchon(ArchonPositionX,ArchonPositionY));
                        wanderWithDirection(dirToArchon);
                    }

                    TreeInfo[] tree = rc.senseNearbyTrees();
                    for (TreeInfo t : tree) {

                        if (rc.canChop(t.getID()) && t.getTeam() != rc.getTeam()) {
                            rc.chop(t.getID());
                            break;
                        }
                    }
                }



//                if (!rc.hasAttacked()) {
//                    wanderWithDirection(enemyBase);
//                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }


    public static boolean ThereIsEnemyBotNearBy() throws GameActionException {
        RobotInfo[] bots = rc.senseNearbyRobots();

        for (RobotInfo b : bots) {
            if (b.getTeam() != rc.getTeam()) {
                return true;
            }
        }

        return false;
    }

    public static void tryToWater() throws GameActionException {
        if (rc.canWater()) {
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
            for (int i = 0; i < nearbyTrees.length; i++)
                if (nearbyTrees[i].getHealth() < GameConstants.BULLET_TREE_MAX_HEALTH - GameConstants.WATER_HEALTH_REGEN_RATE) {
                    if (rc.canWater(nearbyTrees[i].getID())) {
                        rc.water(nearbyTrees[i].getID());
                        break;
                    }
                }
        }
    }

    public static void tryToShake() throws GameActionException {
        if (rc.canShake()) {
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
            for (int i = 0; i < nearbyTrees.length; i++) {
                rc.shake(nearbyTrees[i].getID());
            }
        }
    }

    public static void tryToBuild(RobotType type, int moneyNeeded) throws GameActionException {
        //try to build gardeners
        //can you build a gardener?
        if (rc.getTeamBullets() > moneyNeeded) {//have enough bullets. assuming we haven't built already.
            for (int i = 0; i < 4; i++) {
                if (rc.canBuildRobot(type, dirList[i])) {
                    rc.buildRobot(type, dirList[i]);
                    break;
                }
            }
        }
    }


    public static void tryToPlant(Direction dir) throws GameActionException {
        //try to build gardeners
        //can you build a gardener?
//        if (rc.getTeamBullets() > GameConstants.BULLET_TREE_COST) {//have enough bullets. assuming we haven't built already.
//            for (int i = 0; i < 4; i++) {
//                //only plant trees on a sub-grid
//                MapLocation p = rc.getLocation().add(dirList[i], GameConstants.GENERAL_SPAWN_OFFSET + GameConstants.BULLET_TREE_RADIUS + rc.getType().bodyRadius);
//                if (modGood(p.x, 6, 0.2f) && modGood(p.y, 6, 0.2f)) {
//                    if (rc.canPlantTree(dirList[i])) {
//                        rc.plantTree(dirList[i]);
//                        break;
//                    }
//
//            }
//        }

        if (rc.getTeamBullets() > GameConstants.BULLET_TREE_COST) {


                     if (rc.canPlantTree(dir)){
                         rc.plantTree(dir);
                     }


        }

        }











    static boolean willCollideWithMe(BulletInfo bullet) throws GameActionException {
        MapLocation mylocation = rc.getLocation();

        //get relevant bullet information
        Direction propagationDirecion = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        //CAlculate bullet

        Direction directionToRobot = bulletLocation.directionTo(mylocation);
        float distToRobot = bulletLocation.distanceTo(mylocation);
        float theta = propagationDirecion.radiansBetween(directionToRobot);

        if (Math.abs(theta) >= Math.PI / 2) {
            return false;
        }

        float perpendicularDist = (float) Math.abs(distToRobot * Math.tan(theta));

        return (perpendicularDist <= rc.getType().bodyRadius);


    }

    //try slide step


    public static boolean modGood(float number, float spacing, float fraction) {
        return (number % spacing) < spacing * fraction;
    }

    static void wanderWithDirection(Direction dir) throws GameActionException {

        double rand = Math.random();

        if (0 < rand && rand <= 0.5) {
            tryMove(dir);
        }
        if (0.5 < rand && rand <= 0.66666) {
            tryMove(dir.rotateLeftDegrees(90));
        }
        if (0.66666 < rand && rand <= 0.866666) {
            tryMove(dir.rotateLeftDegrees(180));
        }
        if (0.866666 < rand && rand <= 1) {
            tryMove(dir.rotateLeftDegrees(270));
        }

    }

    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir, 20, 3);
    }

    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (!rc.hasMoved() && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        //boolean moved = rc.hasMoved();
        int currentCheck = 1;

        while (currentCheck <= checksPerSide) {
            // Try the offset of the left side
            if (!rc.hasMoved() && rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset * currentCheck));
                return true;
            }
            // Try the offset on the right side
            if (!rc.hasMoved() && rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset * currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }


    static boolean trySidestep(BulletInfo bullet) throws GameActionException {

        Direction towards = bullet.getDir();
        MapLocation leftGoal = rc.getLocation().add(towards.rotateLeftDegrees(90), rc.getType().bodyRadius);
        MapLocation rightGoal = rc.getLocation().add(towards.rotateRightDegrees(90), rc.getType().bodyRadius);

        return (tryMove(towards.rotateRightDegrees(90)) || tryMove(towards.rotateLeftDegrees(90)));
    }

    static void dodge() throws GameActionException {
        BulletInfo[] bullets = rc.senseNearbyBullets();
        for (BulletInfo bi : bullets) {
            if (willCollideWithMe(bi)) {
                trySidestep(bi);
            }
        }


    }
}