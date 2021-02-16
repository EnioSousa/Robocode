package man;
import robocode.*;

import java.lang.Math;
import robocode.util.Utils;
import java.util.Random;

public class BadRobot extends AdvancedRobot {
	Random gen = new Random();
	int miss = 0, hit = 0;
	double rate = 0.5;
	
	@Override
	public void setAdjustRadarForGunTurn(boolean independent) {
		// TODO Auto-generated method stub
		super.setAdjustRadarForGunTurn(independent);
	}
    public void run() {
    	setAdjustRadarForGunTurn(true);
    	while(true) {
    		//randomGoTo(gen);
    		turnRadarRightRadians(360);
        }
    }
    
    public void randomGoTo(Random gen)
    {
    	goTo(Math.abs(gen.nextInt() % getBattleFieldWidth()), Math.abs(gen.nextInt() % getBattleFieldHeight())); 	
    }
    
	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		double rotate = Utils.normalRelativeAngle(getHeadingRadians()) - getGunHeadingRadians() + e.getBearingRadians();
		
		System.out.println(rate + " " + miss + " " + hit);
		
		setTurnGunRightRadians(rotate);	
		
		if ( Math.abs(getGunTurnRemaining()) < 10 )
		{
			System.out.println(rotate + " " + getGunTurnRemaining());
			
			fireBullet(e);
		}
	}
	
	public void fireBullet(ScannedRobotEvent e)
	{
		double dist = e.getDistance();
		
		if ( dist < 100 )
			fire(rate + 1);
		
		else if ( dist < 200 )
			fire(rate + 0.5 );
			
		else if ( dist > 300 )
			fire(rate - 0.5);
		
		else if ( dist > 400 )
			fire(rate - 1);
		
		else fire(rate);
	}

	public void onBulletHit(BulletHitEvent event)
	{
		if ( hit >= 2 )
			rate += 0.5;
		
		else
			hit++;
	}
	
	public void onBulletMissed(BulletMissedEvent event)
	{
		if ( miss >= 3 )
		{
			miss = 0;
			rate -= 0.25;
		}
		
		else 
			miss++;
	}
	
	public void onHitWall(HitWallEvent event)
	{
		goTo(getBattleFieldWidth()/2, getBattleFieldHeight()/2);
	}
	
    public void onHitByBullet(HitByBulletEvent event)
    {
    	System.out.println("asda");
    	double colision = event.getHeading();
    	
    	goPerpendicular(Utils.normalRelativeAngle(colision + 90));
    }
    
    public void goPerpendicular(double angle)
    {
    	double x = 50 * Math.cos(angle);
    	double y = 50 * Math.sin(angle);
    	System.out.println(x + " " + y);
    	
    	goTo(x, y);
    }
    
    /**
     * Simple goTo, does not take the best route
     * @param nx column position
     * @param ny row position
     */
    public void goTo(double x, double y)
    {
    	x -= getX();
    	y -= getY();
    	
    	double a1 = Math.atan2(x,y);
    	double a2 = getHeadingRadians();
    	double rotation = Utils.normalRelativeAngle(a1-a2);
    	double rotationDeg = Math.toDegrees(rotation);
    	
    	double dist = -1 * Math.hypot(x, y);
    	
    	if ( rotationDeg > 90 )
    		rotation -= Math.PI;
    	
    	else if ( rotationDeg < -90 )
    		rotation += Math.PI;
    	
    	else
    		dist *= -1;
    	
    	turnRightRadians(rotation);
    	ahead(dist);
    }
}













