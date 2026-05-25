package objetos;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public abstract class Objeto {

    protected int x, y;
    protected int largura, altura;
    protected Rectangle hitbox;

    public Objeto(int x, int y, int largura, int altura) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.hitbox = new Rectangle(x, y, largura, altura);
    }

    public int getX()       { 
    	return x; 
    }
    public int getY()       {
    	return y; 
    }
    public int getLargura() { 
    	return largura; 
    }
    public int getAltura()  { 
    	return altura; 
    }
    public Rectangle getHitbox() { 
    	return hitbox; 
    }

    public abstract void draw(Graphics2D g2);
}