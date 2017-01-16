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




    static int count = 0;


    static List<String> idArr = new ArrayList<String>();

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

    public static void donate() throws GameActionException{
        if (rc.getTeamBullets()>1000) rc.donate(10);
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

                if (rc.getTeamBullets() >100 && rc.canHireGardener(goingDir)){
                    rc.hireGardener(goingDir);
                }
                else {
                    if (Math.random() < .8 && rc.canHireGardener(goingDir)) {
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

    static boolean checkIfSurroundinghaveGardener(MapLocation location){
        RobotInfo[] bot = rc.senseNearbyRobots();
        for (RobotInfo b : bot){

            float distanceBetween = (b.getLocation()).distanceTo(rc.getLocation());
            if (b.getType() == RobotType.GARDENER && (b.getTeam() == rc.getTeam() ) && distanceBetween <= 7){
                return true;
            }
        }

        return false;
    }


    public static void runGardener() throws GameActionException {
        donate();
        count++;
        int scout_count=0;

        Direction dir = Direction.NORTH;
        int NumOfTrees = 0 ;

        while (true) {
            try {

                if (checkIfSurroundinghaveGardener(rc.getLocation())){
                    if (rc.canMove(goingDir)) {
                        rc.move(goingDir);
                    } else {
                        goingDir = randomDir();
                    }
                }else {
                    while (NumOfTrees < 3) {
                        while (rc.getTeamBullets() > 80 && rc.canBuildRobot(RobotType.SCOUT, dir)&&scout_count<3){
                            rc.buildRobot(RobotType.SCOUT, dir);
                            scout_count++;
                        }
                        if (rc.canPlantTree(dir)) {
                            rc.plantTree(dir);
                            NumOfTrees++;
                        }else if (rc.getTeamBullets() >= 100){
                            buildAndCheckRobotByGardener(RobotType.SOLDIER,dir);
                            buildAndCheckRobotByGardener(RobotType.LUMBERJACK, dir);
                        }
                        if (rc.getTeamBullets()<500) {
                            tryToWater();
                            tryToShake();
                        }
                        dir = dir.rotateLeftDegrees(60);


                    }
                }

                if (rc.getTeamBullets()<500) {
                    tryToWater();
                    tryToShake();
                }

                /**
                 * here controls the type of robot that will be built
                 */
                for (int i = 0 ; i < 5; i ++){
//                    if (rc.readBroadcast(ArchonPositionX)!=0 && rc.readBroadcast(ArchonPositionY)!=0){
//                        buildAndCheckRobotByGardener(RobotType.SOLDIER,dir);
//                        buildAndCheckRobotByGardener(RobotType.LUMBERJACK, dir);
//                        buildAndCheckRobotByGardener(RobotType.SOLDIER,dir);
//                    }
                    buildAndCheckRobotByGardener(RobotType.SOLDIER,dir);
                    dir = dir.rotateLeftDegrees(60);
                    buildAndCheckRobotByGardener(RobotType.LUMBERJACK,dir);
                }
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

//    public static boolean NoTreesNearBy(){
//        TreeInfo[] trees = rc.senseNearbyTrees();
//        for (TreeInfo t: trees){
//            if ()
//        }
//
//    }

    public static boolean checkFriendlyFire(Direction dir){
        MapLocation own = rc.getLocation();
        RobotInfo[] bots = rc.senseNearbyRobots();
        Direction newdir=dir.rotateRightDegrees(180);
        for (RobotInfo b : bots){
            if (b.getTeam()==rc.getTeam()){
                if (b.getLocation().directionTo(own).equals(newdir)){
                    System.out.println("My position: "+newdir.toString() + " Friend position: "+b.getLocation().directionTo(own).toString());
                    return false;
                }
            }
        }
        return true;
    }

    public static void runSoldier() throws GameActionException {

        donate();
        boolean archonCount  = true ;
        while (true && archonCount) {
            try {

                dodge();

                MapLocation[] ml = rc.getInitialArchonLocations(rc.getTeam().opponent());


                Direction enemyBase = rc.getLocation().directionTo(ml[0]);

                RobotInfo[] bots = rc.senseNearbyRobots();


//                BulletInfo[] bullets=rc.senseNearbyBullets();

                if (ThereIsEnemyBotNearBy()){
                    for(RobotInfo b: bots){
                        Direction dir = rc.getLocation().directionTo(b.getLocation());
                        ArchonBroadcast(b);
                        if (b.getTeam()!=rc.getTeam()){
                            ArchonBroadcast(b);
                            if (rc.canFirePentadShot()&&rc.getTeamBullets()>=1000&&checkFriendlyFire(dir)){
                                rc.firePentadShot(dir);
                            }
                            else if (rc.canFireTriadShot()&&rc.getTeamBullets()>=700&&checkFriendlyFire(dir)){
                                rc.fireTriadShot(dir);
                            }
                            else if (rc.canFireSingleShot()&& rc.getTeamBullets()>0){
                                rc.fireSingleShot(dir);
                            }
                            else{
                                if (rc.readBroadcast(ArchonPositionX)!=0 && rc.readBroadcast(ArchonPositionY)!=0){
                                    Direction dirToArchon = rc.getLocation().directionTo(giveMapLocationOfArchon(ArchonPositionX,ArchonPositionY));
                                    wanderWithDirection(dirToArchon);
                                }else{
                                    wanderWithDirection(enemyBase);
                                }
                            }
                        }
                    }
                }
                else{
                    if (rc.readBroadcast(ArchonPositionX)!=0 && rc.readBroadcast(ArchonPositionY)!=0){
                        Direction dirToArchon = rc.getLocation().directionTo(giveMapLocationOfArchon(ArchonPositionX,ArchonPositionY));
                        wanderWithDirection(dirToArchon);
                    }else{
                        wanderWithDirection(enemyBase);
                    }
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void ArchonBroadcast(RobotInfo b) throws GameActionException {
            if (b.getType() == RobotType.ARCHON && b.getTeam() != rc.getTeam()) {
                rc.broadcast(ArchonPositionX, (int) (b.getLocation().x));
                rc.broadcast(ArchonPositionY, (int) (b.getLocation().y));
            }

    }



    public static void runScout() throws GameActionException{
        donate();
        boolean archonCount  = true ;
        while (true && archonCount) {
            try {

                dodge();

                MapLocation[] ml = rc.getInitialArchonLocations(rc.getTeam().opponent());


                Direction enemyBase = rc.getLocation().directionTo(ml[0]);

                RobotInfo[] bots = rc.senseNearbyRobots();

//                BulletInfo[] bullets=rc.senseNearbyBullets();


                if (rc.readBroadcast(ArchonPositionX) == 0 && rc.readBroadcast(ArchonPositionY) == 0){
                    directionalTravel(enemyBase);
                    for (RobotInfo b: bots){
                        ArchonBroadcast(b);
                    }
                }else{
                    if (ThereIsEnemyBotNearBy()) {
                        for (RobotInfo b : bots) {
                            ArchonBroadcast(b);
                            Direction dir = rc.getLocation().directionTo(b.getLocation());
                            if (b.getTeam() != rc.getTeam()) {

                                if (rc.canFireSingleShot()) {
                                    rc.fireSingleShot(dir);
                                }
                                if (b.getType()==RobotType.LUMBERJACK){
                                    wander();
                                }
                                if (rc.canMove(dir)&&dir==enemyBase) {
                                    rc.move(dir);
                                }else{
                                    rc.move(dir.rotateLeftDegrees(10));
                                }

                            }
                        }
                    }else{
                            Direction dirToArchon = rc.getLocation().directionTo(giveMapLocationOfArchon(ArchonPositionX,ArchonPositionY));
                            directionalTravel(dirToArchon);

                    }
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void runLumberjack() throws GameActionException {
        donate();
        boolean archonCount  = true ;
        while (true && archonCount) {
            try {
                dodge();

                MapLocation[] ml = rc.getInitialArchonLocations(rc.getTeam().opponent());


                Direction enemyBase = rc.getLocation().directionTo(ml[0]);

                RobotInfo[] bots = rc.senseNearbyRobots();


                if (ThereIsEnemyBotNearBy()) {
                    for (RobotInfo b : bots) {
                        ArchonBroadcast(b);
                        float dist = rc.getLocation().distanceTo(b.getLocation());
                        if (b.getTeam() != rc.getTeam() && rc.canStrike() && dist <= GameConstants.LUMBERJACK_STRIKE_RADIUS + rc.getType().bodyRadius) {
                            rc.strike();

                            Direction dir = rc.getLocation().directionTo(b.getLocation());
                            if (rc.canMove(dir)) {
                                rc.move(dir);
                            }
                            break;
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