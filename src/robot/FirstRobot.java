package robot;
import robocode.*;
import robocode.util.Utils;
import java.util.Random;

public class FirstRobot extends AdvancedRobot 
{
	/* Random generator*/
	Random gen = new Random();
	
	/* Tick counter*/
	int tick = 0;
	
	/* Keeps track of enemy. If null no enemy scanned*/
	String enemy = null; 
	/* Keeps track how many ticks we have been looking for a enemy*/
	int lookTime = 0;
	/* Specifies how much to turn the radar*/
	double radarTurnRate = 10;
	
	/*Keeps track of the number of hits*/
	int hit = 0;
	
	/* Keeps track if a ram is in process */
	boolean activeRam = false;
	/* Keeps track of how long we are trying to ram*/
	int ramTime = 0;

	
	/**
	 * This method will be called by robocode, everything you want the tank to do
	 * needs to start with this method
	 */
    public void run() 
    {
    	/**
    	 * The following calls enable the tank parts to move independently
    	 */
    	setAdjustRadarForGunTurn(true);
    	setAdjustRadarForRobotTurn(true);
    	setAdjustGunForRobotTurn(true);
    	
    	while (true)
    	{
    		randomWalk();
    		scan();
    		execute();
    		
    		tick++;
    		
    	}
    	
    }

    /*--------------------------Attack Mechanics--------------------------*/ 
    public void attackMethod(ScannedRobotEvent event)
    {
    	if ( getEnergy() > 50 && event.getEnergy() < 15 &&  ramTime < 10)
    		robotRam(event);
    	
    	else
    	{
    		ramTime = 0;
    		robotFire(event);
    	}
    }
    
    /*--------------------------Ram mechanics--------------------------*/ 
    public void robotRam(ScannedRobotEvent event)
    {
    	activeRam = true;
    	ramTime++;
    	
    	/* Calculate enemy angle (Where the enemy is)*/
    	double rotation = Utils.normalRelativeAngle(getHeadingRadians() + event.getBearingRadians());

        /* Calculate the coordinates of the robot */
        double enemyX = Math.abs((getX() + Math.sin(rotation) * event.getDistance()));
        double enemyY = Math.abs((getY() + Math.cos(rotation) * event.getDistance()));
        
        goTo(enemyX, enemyY);    	
        
        System.out.printf("---Ram Data---\nPosition: (%f,%f)\nEnemy pos: (%f,%f)\nTime: %d\n", getX(), getY(), enemyX, enemyY, lookTime);
    }
   
    
    /*--------------------------Fire mechanics--------------------------*/ 
    /**
     * This method will aim and fire a bullet if possible
     * @param event Information about the scanned event
     */
    public void robotFire(ScannedRobotEvent event)
    {
    	lookTime = 0;
    	
    	double power = adjustFirePower(event);
    	
    	boolean permFire = adjustAim(event);
    	
    	if ( permFire )
    		setFire(power);   	
    	
    	if ( getGunHeat() == 0)
    		System.out.printf("---Fire Data---\nActive fire: %s\nPower: %f\nGunHeat: %f\nDistance: %f\nHit: %d\n", permFire == true? "True": "False", power, getGunHeat(), event.getDistance(), hit);
    }
    
    /**
     * This method will aim gun at the scanned enemy. In case the gun is reasonably
     * well aimed then we return true, which indicates that we can fire.
     * @param event Information about the scanned event
     * @return true if gun if properly aim at enemy, otherwise false
     */
    public boolean adjustAim(ScannedRobotEvent event)
    {
    	double rotation = Utils.normalRelativeAngle(getHeadingRadians() - getGunHeadingRadians() + event.getBearingRadians());
    	double rotationDeg = Math.toDegrees(rotation);
    	
    	setTurnGunRightRadians(rotation);
    	
    	return Math.abs(rotationDeg) < 10 && getGunHeat() == 0; 
    }
    
    /**
     * This method will adjust the fire power to be fired. It takes in consideration 
     * the distance, previous accuracy and energy levels.
     * @param event Information about the scanned event
     * @return fire power
     */
    public double adjustFirePower(ScannedRobotEvent event)
    {
    	double dist = event.getDistance();
    	
    	double power = 0;
    	
    	/*Criteria: Distance*/    	
    	power += dist > 600 ? 0.25: dist > 500 ? 0.75: dist > 400 ? 1.25: dist > 300 ? 1.75: dist > 200 ? 2: dist > 100 ? 2.5 : 3; 
    	
    	/*Criteria: Previous accuracy*/  
    	power = normalizePower(power);
    	power += hit >= 4 ? 1.5: hit >= 2 ? 1: hit >= 1 ? 0.5: 0;

    	/*Criteria: Current energy*/ 
    	power = normalizePower(power);
    	power -= getEnergy() < 25 ? 1: getEnergy() < 25 ? 0.75: getEnergy() <50 ? 0.25: 0;
    	
    	return power;    	
    }
    
    public double normalizePower(double power)
    {
    	return power >= 3 ? 3: power <= 0.1 ? 0.1: power;
    }
    
    public void onBulletHit(BulletHitEvent event)
    {
    	hit++;
    }
    
    public void onBulletMissed(BulletMissedEvent event)
    {
    	hit = 0;
    }
    
    /*--------------------------Scan mechanics--------------------------*/   
    /**
     * This method should be called in every tick/turn. What it basically does
     * is to check if we have don't have an enemy in sight, if so then we need to do a 
     * full scan (full map scan), otherwise we check for number of turns we haven't seen 
     * our enemy and act appropriately. 
     */
    public void scan()
    {
    	lookTime++;
    	
    	System.out.println(lookTime);
    	
    	if ( enemy == null )
    		fullScan();
    	
    	else if ( lookTime > 11 )
    	{
    		fullScan();
    		enemy = null;
    		activeRam = false;
    		ramTime = 0;
    	}
    	
    	else if ( lookTime > 5 )
    		setTurnRadarRight(-radarTurnRate);
    	
    	else if ( lookTime > 2 )
    		setTurnRadarRight(radarTurnRate);
    }
    
    /**
     * Basic full rotation
     */
    public void fullScan()
    {
		setTurnRadarRight(-2*radarTurnRate);    	
    }

    /**
     * The method will be called once we scanned an enemy tank. We calculate the how much
     * of the radar we have to turn, register the enemy name. This method will also call
     * the attack method.
     */
    public void onScannedRobot(ScannedRobotEvent event)
    {
    	lookTime = 0;
    	
    	enemy = event.getName();
    	
    	double rotation = Utils.normalRelativeAngle(getHeadingRadians() - getRadarHeadingRadians() + event.getBearingRadians());
    	
    	setTurnRightRadians(rotation);
    	
    	attackMethod(event);
    }

    /*--------------------------Move mechanics--------------------------*/  
    /**
     * This method will make the tank walk randomly around the CENTER position.
     */
    public void randomWalk()
    {    	
    	if ( activeRam && lookTime < 4 )
    		return;
    	
    	else
    		activeRam = false;
    	
    	if ( tick % 10 != 0 )
    		return;
    	
    	double w = getBattleFieldWidth() / 2 ;
    	double h = getBattleFieldHeight() / 2;
    	
    	/*
    	 * Remember Random.nextInt generates positive and negative numbers
    	 */
    	double offset = gen.nextInt() % 100;
    	
    	goTo(w + offset, h + offset);    	
    }
    
    /**
     * Simple goTo. Robot moves asynchronous to the rest of its components.
     * This will mean that sometimes the robot will start to move even before the 
     * correct direction is achieved, sometimes making a curve and being a little 
     * imprecise.
     * @param ox column position
     * @param oy row position
     */
    public void goTo(double ox, double oy)
    {    	
    	double x = ox - getX();
    	double y = oy - getY();
    	
    	double rotation = Math.atan2(x, y);
    	rotation = Utils.normalRelativeAngle(rotation - getHeadingRadians());
    	
    	setTurnRightRadians(rotation);
    	double dist = Math.hypot(x, y);
    	setAhead(dist);
    	
    	/**
    	 * Comment the following line to run test against other robots
    	 */
    	//System.out.printf("----Movement data----\nRoute set to: (%f, %f)\nDistance: %f\nAngle: %f\nCurrent pos: (%f, %f)\n", ox, oy, dist, Math.toDegrees(rotation), getX(), getY());
    }

}
