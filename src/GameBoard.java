import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class GameBoard {
	public static final File file = new File("img/HockeyRink.png"); 
	ImageView iv;
	
	GameBoard(){
    Image image = new Image(file.toURI().toString());
    this.iv = new ImageView(image);
	iv.setVisible(true);
	
	}

	public ImageView getIv() {
		return iv;
	}

	public void setIv(ImageView iv) {
		this.iv = iv;
	}
}
