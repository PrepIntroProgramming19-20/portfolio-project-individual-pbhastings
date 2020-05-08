import javax.swing.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.lang.Math;
import java.awt.Point;
import java.util.*;

public class TestPaintPanel{
    JFrame frame = new JFrame();
    PaintedPanel panel = new PaintedPanel();
    JTextField name = new JTextField("enter name");
    JLabel scoreboard = new JLabel();

    int fWidth = 500; //frame width
    int fHeight = 500; //frame height
    int width = 500; //panel width
    int height = 500; //panel height
    //double angle = Math.PI / 7; //start angle
    double angle = Math.PI / 2;
    double ballStartSpeed = 3; //speed ball resets to after each point
    double ballSpeed = 3; //ball speed
    double ballSpeedInc = 0.1; //ball speed increment per paddle collision
    //double ballSpin = 0;
    double ballSpin = 0.5;
    int playerScore = 0;
    int prevPlayerY = 0;
    List<Integer> prevPlayerYs = new ArrayList<>(List.of(0, 0, 0, 0, 0));
    List<Integer> prevEnemyYs = new ArrayList<>(List.of(0, 0, 0, 0, 0));
    String playerName;
    //enemy vars
    double enemySpeed = 4; //enemy speed
    int enemyScore = 0;
    double enemyTargetOffset = 0; //how far away from the center of the paddle the enemy will try to hit the ball
    int msSinceLastHit = 0; //milliseconds since last hit (if longer than some time, reset and change angle)

    Point mousePos;

    class TimerListener implements ActionListener{
        public void actionPerformed(ActionEvent event){
            //set width and height if they have changed
            width = panel.getWidth();
            height = panel.getHeight();
            //increase time since last hit
            msSinceLastHit += 10;
            //if ball gets past paddles, reset
            if(panel.ballX > width - panel.padelOffset){
                panel.ballX = width/2;
                panel.ballY = height/2;
                ballSpeed = ballStartSpeed;
                ballSpin = 0;
                msSinceLastHit = 0;
                playerScore++;
                scoreboard.setText(playerName + " " + playerScore + " - " + enemyScore + " Patrick");
            }else if(panel.ballX < panel.padelOffset){
                panel.ballX = width/2;
                panel.ballY = height/2;
                ballSpeed = ballStartSpeed;
                ballSpin = 0;
                msSinceLastHit = 0;
                enemyScore++;
                scoreboard.setText(playerName + " " + playerScore + " - " + enemyScore + " Patrick");
            }
            //if ball leaves frame x, set angle to pi-angle
            if(checkEnemyCollision()){
                double yDiff = panel.ballY - panel.enemyY; // some value from -ballSize to padelHeight
                //map yDiff to an angle of 2pi/3 to 4pi/3
                double newAng = (yDiff + panel.ballSize) / (panel.padelHeight + panel.ballSize); //0 to 1
                newAng = (newAng * -1) + 1; //reverse
                newAng = (newAng * 2*Math.PI/3) + 2*Math.PI/3; //scale
                angle = newAng;
                ballSpin = -((double)calcEnemySpeed()) / 100; //set ballSpin based on the padel's speed
                //generate new random offset for enemy
                enemyTargetOffset = (Math.random() * (panel.padelHeight) - (panel.padelHeight) / 2) * 0.9; //multiply by 0.9 to make good

                ballSpeed += ballSpeedInc; //increase ball speed by increment
                msSinceLastHit = 0;
            }else if(checkPlayerCollision()){
                double yDiff = panel.ballY - panel.playerY; //some value from -ballSize to padelHeight
                //map yDiff to an angle of 5pi/3 to 7pi/3
                double newAng = (yDiff + panel.ballSize) / (panel.padelHeight + panel.ballSize); //0 to 1
                newAng = (newAng * 2*Math.PI/3) + 5*Math.PI/3; //scale
                angle = newAng;
                ballSpin = ((double)calcPlayerSpeed()) / 100; //set ballSpin based on the padel's speed
                
                ballSpeed += ballSpeedInc; //increase ball speed by increment
                msSinceLastHit = 0;
            }
            //if ball leaves frame y, set angle to negative angle 
            if(panel.ballY > height - panel.ballSize){
                //change bounce angle based on spin
                /*
                double newAng = angle * -1;
                newAng += ballSpin/2;
                angle = newAng;
                */
                angle = bounceBot(angle, ballSpin);
                ballSpin /= 2;
            }else if(panel.ballY < 0){
                //change bounce angle based on spin
                /*
                double newAng = angle * -1;
                newAng -= ballSpin/2;
                angle = newAng;
                */
                angle = bounceTop(angle, ballSpin);
                ballSpin /= 2;
            }
            //translate ball in its new direction by speed units
            panel.ballX += ballSpeed * Math.cos(angle);
            panel.ballY += ballSpeed * Math.sin(angle);

            //move enemy padel
            if(Math.abs(panel.enemyY + panel.padelHeight/2 - panel.ballSize + enemyTargetOffset - panel.ballY) > enemySpeed){ //don't move if close
                if(panel.enemyY + panel.padelHeight/2 - panel.ballSize + enemyTargetOffset > panel.ballY){
                    panel.enemyY -= enemySpeed;
                }else{
                    panel.enemyY += enemySpeed;
                }
            }

            //set player's y position to the mouse y
            mousePos = frame.getMousePosition(); //use frame instead of panel so that it tracks mouse when it goes to the top window bar
            if(mousePos != null){
                panel.playerY = mousePos.y - panel.padelHeight/2 - 25; //subtract half height of panel and 25 to make position relative to panel, not frame
            }

            //update the rotation of the ball based on the ball's spin
            panel.rotation += ballSpin;
            //scoreboard.setText(playerName + " " + playerScore + " - " + enemyScore + " Patrick" + " spin: " + ballSpin + " vel: " + calcPlayerSpeed());
            
            /*if(ballSpin > 0){
                scoreboard.setText("pos");
            }else{
                scoreboard.setText("neg");
            }*/
            
            //change direction of ball as it moves based on the spin and adjust spin
            angle -= 0.03*ballSpin;
            ballSpin *= 0.99;
            
            //if ball still hasn't hit a paddle after 5 seconds, reset
            if(msSinceLastHit > 5000){
                panel.ballX = width/2;
                panel.ballY = height/2;
                ballSpin = 0;
                angle = 0;
                msSinceLastHit = 0;
            }
            
            //update the position arrays of player and enemy
            prevPlayerYs.add(0, panel.playerY);
            prevPlayerYs.remove(5);
            prevEnemyYs.add(0, (int) panel.enemyY);
            prevEnemyYs.remove(5);

            //redraw the panel
            panel.repaint();
        }
    }

    public TestPaintPanel(){
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(name);

        frame.setSize(fWidth, fHeight); //set panel dimensions
        frame.setVisible(true); //make panel visible

        name.addActionListener(new TextListener());
    }
    //run after user enters name
    public void ontinue(){
        fWidth = frame.getWidth();
        fHeight = frame.getHeight();
        //setup frame and add components
        frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel, BorderLayout.CENTER);
        frame.add(scoreboard, BorderLayout.SOUTH);
        scoreboard.setText(playerName + " 0 - 0 Patrick");
        scoreboard.setHorizontalAlignment(JLabel.CENTER);
        frame.setSize(fWidth, fHeight);
        frame.setVisible(true);
        //set initial padel positions
        panel.enemyY = panel.getHeight() / 2 - 50;
        //set initial width and height to panel width and height
        width = panel.getWidth();
        height = panel.getHeight();
        //create timer that updates every 10 milliseconds
        int period = 10;
        javax.swing.Timer timer = new javax.swing.Timer(period, new TimerListener());
        timer.start();
    }

    public boolean checkEnemyCollision(){
        if(panel.ballX > width - panel.padelOffset - panel.padelWidth && panel.ballY + panel.ballSize > panel.enemyY && panel.ballY < panel.enemyY + panel.padelHeight){
            return(true);
        }
        return(false);
    }

    public int calcPlayerSpeed(){
        int speed = prevPlayerYs.get(0) - prevPlayerYs.get(4);
        return speed;
    }
    
    public int calcEnemySpeed(){
        int speed = prevEnemyYs.get(0) - prevEnemyYs.get(4);
        return speed;
    }

    public boolean checkPlayerCollision(){
        if(panel.ballX < panel.padelOffset + panel.padelWidth && panel.ballY + panel.ballSize > panel.playerY && panel.ballY < panel.playerY + panel.padelHeight){
            return(true);
        }
        return(false);
    }
    
    //finds value ofan angle mod 2 pi
    public static double modTwoPi(double ang){
        int q = (int)(ang / (2 * Math.PI));
        ang -= q * 2 * Math.PI;
        //angle can still be less than 0 if it was initially negative
        if(ang < 0){
            ang += 2 * Math.PI;
        }
        return ang;
    }
    
    public double bounceTop(double ang, double spin){
        ang = modTwoPi(ang);
        //calculate distance to max angle and the adjustment factor
        double dif;
        double adj;
        if(spin > 0){
            dif = ang;
            adj = 1 - (1 / (1 + ballSpin / 5)); //this equation approaches 1 as ballSpin increases
        }else{
            dif = Math.PI - ang;
            adj = 1 - (1 / (1 + ballSpin / -5)); //this equation approaches 1 as ballSpin decreases
        }
        
        ang *= -1;
        if(spin > 0){
            ang += dif * adj;
        }else{
            ang -= dif * adj;
        }
        return ang;
    }
    
    public double bounceBot(double ang, double spin){
        ang = modTwoPi(ang);
        //calculate distance to max angle and the adjustment factor
        double dif;
        double adj;
        if(spin > 0){
            dif = Math.PI - ang;
            adj = 1 - (1 / (1 + ballSpin / 5)); //this equation approaches 1 as ballSpin increases
        }else{
            dif = 2 * Math.PI - ang;
            adj = 1 - (1 / (1 + ballSpin / -5)); //this equation approaches 1 as ballSpin decreases
        }
        
        ang *= -1;
        if(spin > 0){
            ang += dif * adj;
        }else{
            ang -= dif * adj;
        }
        return ang;
    }

    public class TextListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            playerName = name.getText();
            ontinue();
        }
    }

    public static void main(String[] args) throws InterruptedException{
        TestPaintPanel gui = new TestPaintPanel();
    }
}