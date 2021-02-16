package robot;
import robocode.*;
import robocode.util.Utils;

public class FirstRobot extends AdvancedRobot {

	boolean focus = false;
	double extraPower = 0;
	int counter = 0;
	
    public void run() 
    {
    	setAdjustRadarForGunTurn(true);
    	setAdjustRadarForRobotTurn(true);
    	setAdjustGunForRobotTurn(true);
    	
    	while(true) 
    	{    		    	
    		if ( !focus )
    			setTurnRadarRight(360);
    		
    		focus = false;
    		
    		execute();
    	}
    }
    
    public void fireMechanics(double angle, double distance)
    {
    	double gunAngle = Utils.normalRelativeAngle(getHeadingRadians() - getGunHeadingRadians() + angle);
    	
    	setTurnGunRightRadians(gunAngle);
    	
    	System.out.println(gunAngle);
    	
    	if ( Math.toDegrees(angle) < 10 )
    	{
    		double power = 1.5;
    		
    		if ( distance < 100 )
    			power = 3;
    		
    		else if ( distance < 200 )
    			power = 2;
    		
    		else if ( distance < 300 )
    			power = 1;
    		
    		else
    			power = 0.5;
    		
    		fire ( power );
    	}
    }
    
    public void onBulletHit(BulletHitEvent event)
    {
    	System.out.println(counter);
    	
    	if ( counter >= 1 )
    		extraPower = extraPower >= 2.5 ? 3: extraPower + 0.5;
    	
    	else
    		counter++;
    }
    
    public void onBulletMissed(BulletMissedEvent event)
    {
    	extraPower = extraPower <= 0.3 ? 0.1: extraPower - 0.2;
    	
    	counter = 0;
    }
    
    public void onScannedRobot(ScannedRobotEvent e) {
		// Lock on to our target (this time for sure)
		setTurnRadarRight(getHeading() - getRadarHeading() + e.getBearing());
		focus = true;	
		
		fireMechanics(e.getBearingRadians(), e.getDistance());
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
    	
    	double rotation = Math.atan2(x-getX(), y-getY());
    	rotation = Utils.normalRelativeAngle(getHeadingRadians() - rotation);
    	
    	setTurnRightRadians(rotation);
    	setAhead(Math.cos(rotation) * Math.hypot(x, y));
    }
}
