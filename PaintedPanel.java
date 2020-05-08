import javax.swing.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Color;

public class PaintedPanel extends JPanel{
    double ballX = 250;
    double ballY = 250;
    double ballCenterX = 255;
    double ballCenterY = 255;
    
    double enemyY = 0;
    int playerY = 0;
    int ballSize = 10; //diameter of ball
    double rotation = 0; //rotation of ball
    int dotSize = 6; //diameter of dot on ball that shows rotation (preferably even)
    double dotDistance = 3; //distance of dot from center of ball
    
    int padelWidth = 10; //width of padel
    int padelHeight = 100; //height of padel
    int padelOffset = 20; //how far padels are from the edge of the panel
    
    @Override
    public void paintComponent(Graphics g){
        ballCenterX = ballX + ballSize/2;
        ballCenterY = ballY + ballSize/2;
        g.setColor(Color.black);
        g.fillRect(0, 0, this.getWidth(), this.getHeight()); //reset background to white
        g.setColor(new Color(207, 114, 203));
        g.fillOval((int) ballX, (int) ballY, ballSize, ballSize); //draw circle at x,y
        g.setColor(Color.yellow);
        g.fillOval((int) (ballCenterX + dotDistance * Math.cos(rotation) - dotSize/2), (int) (ballCenterY - dotDistance * Math.sin(rotation) - dotSize/2), dotSize, dotSize); //draws dot to show rotation of ball
        //draw enemy padel
        g.setColor(Color.white);
        g.fillRect(this.getWidth() - padelOffset, (int) enemyY, padelWidth, padelHeight);
        //draw player padel
        g.fillRect(padelOffset, playerY, padelWidth, padelHeight);
    }
}