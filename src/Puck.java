import javafx.scene.shape.Circle;

public class Puck extends Circle{
	
	double horizontal = 5; 
	double vertical = 3; 
	
	Puck(double radius){
		super(radius);
	}

	public boolean isRight() {
		return horizontal > 0;
	}

	public boolean isDown() {
		return vertical > 0;
	}
	
	public void hitHorizontal() {
		horizontal = -horizontal;
	}
	
	public void hitVertical() {
		vertical = -vertical;
	}

	public void step() {
		setLayoutX(getLayoutX() + horizontal);
		setLayoutY(getLayoutY() + vertical);
	}
	
}
