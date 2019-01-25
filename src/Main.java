import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.event.ActionEvent;
import javafx.animation.KeyFrame;
import javafx.animation.PathTransition;
import javafx.util.Duration;


public class Main extends Application{
	
	private Scene mainScene;
	private Stage primaryStage;
	private Pane pane;
	
	private HockeyStick playerstick;
	private Puck puck;
	private ScoreBoard scoreboard;
	private GameBoard background;
	private Text playerscore;
	private Text opponentscore;

	private HockeyStick opponentstick;
	private Line opponentpath;
	private PathTransition opponenttransition;
	
	private Timeline puckmovement;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		
		// Setup the main stage
		pane = new Pane();
		mainScene = new Scene(pane, 720, 960);	
		pane.setMaxSize(mainScene.getWidth(), mainScene.getWidth());
		
		// Create a background & scoreboard
		background = new GameBoard();
		scoreboard = new ScoreBoard();
		pane.getChildren().add(background.getIv());

		
		// Create group
		Group g = new Group(); //hold puck and stick
		pane.getChildren().add(g);

		// Create puck (game object)
		puck = new Puck(20);
		puck.relocate(mainScene.getWidth()/2-puck.getRadius(), mainScene.getHeight()/2-puck.getRadius()/2f);
		g.getChildren().add(puck);
		
		// Create Players stick
		playerstick = new HockeyStick(120, 25);
		playerstick.relocate(
				mainScene.getWidth()/2-playerstick.getWidth()/2f,
				mainScene.getHeight()-playerstick.getHeight()-50
		);
		playerstick.setCursor(Cursor.HAND);
		playerstick.setOnMouseDragged(playerMouseDrag);
		g.getChildren().add(playerstick);

		//Create opponent stick and path
		opponentstick = new HockeyStick(200,25);
		opponentstick.relocate(
				mainScene.getX()+opponentstick.getWidth()/2f,
				mainScene.getY() + opponentstick.getHeight()+15);
		g.getChildren().add(opponentstick);
	
		opponentpath = new Line(opponentstick.getX(),50,720-opponentstick.getWidth(),50);
		opponentpath.setVisible(false);
		g.getChildren().add(opponentpath);
		
		//Set Path Transition (follow this path essentially)
		opponenttransition = new PathTransition();
		opponenttransition.setNode(opponentstick);     
		opponenttransition.setPath(opponentpath); 	//sets path node should follow
		opponenttransition.setDuration(Duration.millis(2000));		//Uses duration class to set time of animation
		opponenttransition.setCycleCount(Timeline.INDEFINITE);			//Sets amount of cycles animation will complete
		opponenttransition.autoReverseProperty().set(true);
		opponenttransition.play();

		

		// Create Title text
		Text title = new Text("Welcome to Air Hockey");
		title.setFont(Font.font("Tahoma", FontWeight.BOLD, 40));
		title.setTextAlignment(TextAlignment.CENTER);
		title.setLayoutX(150);
		title.setLayoutY(320);
		pane.getChildren().add(title);

		// Create player score
		playerscore = new Text("Player Score: " + scoreboard.getPlayerscore());
		playerscore.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		playerscore.setLayoutX(50);
		playerscore.setLayoutY(50);

		// Create opponent score
		opponentscore = new Text("Opponent Score: " + scoreboard.getOpponentscore());
		opponentscore.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		opponentscore.setLayoutX(50);
		opponentscore.setLayoutY(70);

		//Reset button
		Hyperlink reset = new Hyperlink("Reset");
		reset.setLayoutX(50);
		reset.setLayoutY(90);
		reset.setOnAction(new EventHandler<ActionEvent> (){
			@Override
			public void handle(ActionEvent e) {
				reset();
			}
		});
		
		
		// Finalize scene
		primaryStage.setTitle("Air Hockey");
		primaryStage.setScene(mainScene);
		primaryStage.setResizable(false);
		primaryStage.sizeToScene();
		primaryStage.show();   

		// Create gameloop here
		puckmovement = new Timeline();
		puckmovement.getKeyFrames().add(new KeyFrame(Duration.millis(10), new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent t) {
				step();
			}
		}));
		puckmovement.setCycleCount(Timeline.INDEFINITE);

		// New game button
		Button newgame = new Button("New Game");
		Platform.runLater(() -> {
			newgame.setLayoutX(pane.getWidth()/2-newgame.getWidth()/2);
			newgame.setLayoutY(640);
		});
		pane.getChildren().add(newgame);
		newgame.setOnAction(new EventHandler<ActionEvent> () {
			@Override
			public void handle(ActionEvent arg0) {
				title.setVisible(false);
				newgame.setVisible(false);
				puckmovement.play();
				pane.getChildren().addAll(playerscore, opponentscore,reset);

			}
		});

	}

	public static void main(String[] args) {
		launch();
	}
	
	/**
	 * This is the main games step loop. All of the game logic happens here.
	 */
	private void step() {
		puck.step();

		Bounds bounds = pane.getBoundsInLocal();
		Shape playerintersect = Shape.intersect(puck, playerstick);
		Shape opponentintersect = Shape.intersect(puck, opponentstick);
		
		//If the puck reaches right or left boundary, bounce puck
		if(puck.getLayoutX() <= (bounds.getMinX() + puck.getRadius()) || 
				puck.getLayoutX() >= (bounds.getMaxX() - puck.getRadius()) ){
			puck.hitHorizontal();
		}

		//If the puck reaches the top, give player a point
		if(puck.getLayoutY() == (bounds.getMinY() + puck.getRadius())) {
			scoreboard.setPlayerscore(scoreboard.getPlayerscore()+1);
			puckmovement.pause();
			puck.relocate(mainScene.getWidth()/2-puck.getRadius(), mainScene.getHeight()/2-puck.getRadius()/2f);
			puck.vertical = Math.abs(puck.vertical);
		}
		
		//If the puck reaches the bottom, give opponent a point
		if(puck.getLayoutY() == (bounds.getMaxY() - puck.getRadius())) {
			scoreboard.setOpponentscore(scoreboard.getOpponentscore()+1);
			puckmovement.pause();
			puck.relocate(mainScene.getWidth()/2-puck.getRadius(), mainScene.getHeight()/2-puck.getRadius()/2f);
			puck.vertical = Math.abs(puck.vertical);
		}
		
		// Puck hits playerstick
		if(playerintersect.getBoundsInLocal().getWidth() != -1) {
			puck.hitVertical();
		}

		//Puck hits opponentstick
		if(opponentintersect.getBoundsInLocal().getWidth() != -1) {
			puck.hitVertical();
		}
		
		//If player score reaches 10, reset game and display winning screen
		if(scoreboard.getPlayerscore() == 10) {
			playerwin();
			reset();
		}
		
		//If opponent score reaches 10, reset game and display loser screen
		if(scoreboard.getOpponentscore() == 10) {
			opponentwin();
			reset();
		}
		// Update score text
		playerscore.setText("Player Score: " + scoreboard.getPlayerscore());
		opponentscore.setText("Opponent Score: " + scoreboard.getOpponentscore());
	}
	
	/**
	 * Simple function to clamp a value with a min and max.
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	private double clamp( double value, double min, double max ) {
		return Math.max(min, Math.min(max, value));
	}

	
	//Paddle Animation Events
	EventHandler<MouseEvent> playerMouseDrag = new EventHandler<MouseEvent> () {
		@Override
		public void handle(MouseEvent m) {
			Rectangle r = ((Rectangle)(m.getSource()));
			double setX = (m.getScreenX()-primaryStage.getX()) - r.getWidth()/2;
			double setY = (m.getScreenY()-primaryStage.getY()) - r.getHeight();
			
			// Dont let the paddle outside of the screen!
			setX = clamp( setX, 0, mainScene.getWidth()-r.getWidth());
			setY = clamp( setY, 0, mainScene.getHeight()-r.getHeight());
			
			// THIS IS JUST FOR TESTING (DONT LET PLAYER CHANGE Y POSITION)
			setY = r.getLayoutY();
			
			// Update position
			r.relocate(setX, setY);
			
			//Play the animation
			puckmovement.play();
		}

	};

	//Reset positions
	public void reset() {
		//Set scores to 0
		scoreboard.setOpponentscore(0);
		scoreboard.setPlayerscore(0);
		
		//Display new scores
		playerscore.setText("Player Score: " + scoreboard.getPlayerscore());
		opponentscore.setText("Opponent Score: " + scoreboard.getOpponentscore());
		
		//Reset puck position and pause it
		puck.relocate(mainScene.getWidth()/2-puck.getRadius(), mainScene.getHeight()/2-puck.getRadius()/2f);
		puckmovement.pause();
	
		//Reset paddle positions
		playerstick.relocate(
				mainScene.getWidth()/2-playerstick.getWidth()/2f,
				mainScene.getHeight()-playerstick.getHeight()-50
		);
		opponentstick.relocate(
				mainScene.getX()+opponentstick.getWidth()/2f,
				mainScene.getY() + opponentstick.getHeight()+15
				);
		
	}
	
	//Player win
	public void playerwin() {
		HBox winbox = new HBox(10);
		
		Scene win = new Scene(winbox,200,100);
		
		Text winner = new Text("You win! :)");
		winner.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		winner.setTextAlignment(TextAlignment.CENTER);
		winner.setLayoutX(100);
		winner.setLayoutY(50);
		winbox.getChildren().add(winner);
		
		
		Stage secondaryStage = new Stage();
		secondaryStage.setTitle("You win!");
		secondaryStage.setScene(win);
		secondaryStage.show();
	}
	
	//Opponent win
	public void opponentwin() {
		HBox losebox = new HBox(10);
		
		Scene lose = new Scene(losebox,150,150);
		
		Text loser = new Text("You Lose! :(");
		loser.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		loser.setTextAlignment(TextAlignment.CENTER);
	
		losebox.getChildren().addAll(loser);
		
		Stage secondaryStage = new Stage();
		secondaryStage.setTitle("You Lose!");
		secondaryStage.setScene(lose);
		secondaryStage.show();
	}
	
}


