package connect4;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable
{
	private static final int COLUMNS=7;
	private static final int ROWS=6;
	private static final int CIRCLE_DIAMETER=80;
	private static final String DISC_COLOR1="#24303E";
	private static final String DISC_COLOR2="#4CAA88";

	private static String PLAYER_ONE="PLAYER ONE";
	private static String PLAYER_TWO="PLAYER TWO";


	private static boolean isPLAYERoneTURN=true;

	private Disc[][] insertDisc=new Disc[ROWS][COLUMNS];//For Structural Changes meant for the Developer


	@FXML
	public GridPane rootGridPane;

	@FXML
	public Pane insertedDiscPane;

	@FXML
	public Label playerNameLabel;

	@FXML
	public TextField playerOneTextField,playerTwoTextField;

	@FXML
	public Button setNamesButton;



	private boolean isAllowedToInsert=true;//Just a flag to stop multiple discs from being added

	public void createPlayground()
	{
		setNamesButton.setOnAction(event -> {
			PLAYER_ONE=playerOneTextField.getText();
			PLAYER_TWO=playerTwoTextField.getText();
			playerNameLabel.setText(isPLAYERoneTURN?PLAYER_ONE:PLAYER_TWO);


		});

        Shape rectangleWithHoles=createGameStructuralGrid();
		rootGridPane.add(rectangleWithHoles,0,1);

		List<Rectangle> rectangleList=createClickableColumns();
		for (Rectangle rectangle:rectangleList)
		{
			rootGridPane.add(rectangle,0,1);
		}

	}
	private Shape createGameStructuralGrid()
	{
		Shape rectangleWithHoles=new Rectangle((COLUMNS+1)*CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);
		for(int row=0;row<ROWS;row++)
		{
			for(int columns=0;columns<COLUMNS;columns++)
			{
				Circle circle=new Circle();
				circle.setRadius(CIRCLE_DIAMETER/2);
				circle.setCenterX(CIRCLE_DIAMETER/2);
				circle.setCenterY(CIRCLE_DIAMETER/2);
				circle.setSmooth(true);
				circle.setTranslateX(columns*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);
				circle.setTranslateY(row*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);
				rectangleWithHoles=Shape.subtract(rectangleWithHoles,circle);




			}
		}
		rectangleWithHoles.setFill(Color.WHITE);
		return rectangleWithHoles;
	}

	private List<Rectangle> createClickableColumns()
	{
		List<Rectangle> rectangleList=new ArrayList<>();
		for(int col=0;col<COLUMNS;col++)
		{
			Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col*(CIRCLE_DIAMETER+5)+(CIRCLE_DIAMETER / 4));
			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));
			final int column=col;
			rectangle.setOnMouseClicked(event -> {
				if(isAllowedToInsert)
				{
					isAllowedToInsert=false;//When Disc is dropped no more disc will be inserted
					insertDisc(new Disc(isPLAYERoneTURN),column);
				}
			});
			rectangleList.add(rectangle);
		}
		return rectangleList;
	}
	private void insertDisc(Disc disc,int column)
	{
		int row=ROWS-1;
		while(row>=0)
		{
			if(getDiscIfPresent(row,column)==null)
				break;

			row--;
		}

		if(row<0)
			return;//Describes that the List is full
		int currentRow=row;
      insertDisc[row][column]=disc;//For Structural Changes by the Developer
      insertedDiscPane.getChildren().add(disc);//For Visual Changes for the Player
      disc.setTranslateX(column*(CIRCLE_DIAMETER+5)+(CIRCLE_DIAMETER / 4));
		TranslateTransition translateTransition=new TranslateTransition(Duration.seconds(0.5),disc);
      translateTransition.setToY(row*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);
      translateTransition.setOnFinished(event -> {
      	isAllowedToInsert=true;//For the Next Player to Insert The Disc
      	if(gameEnded(currentRow,column))
        {
        	gameOver();
        	return;
        }
      	isPLAYERoneTURN=!isPLAYERoneTURN;
      	playerNameLabel.setText(isPLAYERoneTURN?PLAYER_ONE:PLAYER_TWO);
      });
      translateTransition.play();
	}
	private boolean gameEnded(int row,int column)
	{
		//Vertical Points.

		List<Point2D> verticalPoints=IntStream.rangeClosed(row-3,row+3)
				.mapToObj(r -> new Point2D(r,column))
				.collect(Collectors.toList());
		List<Point2D> horizontalPoints=IntStream.rangeClosed(column-3,column+3)
				.mapToObj(col -> new Point2D(row,col))
				.collect(Collectors.toList());
		Point2D startPoint1=new Point2D(row-3,column+3);
		List<Point2D> diagonal1Points=IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint1.add(i,-i))
				.collect(Collectors.toList());

		Point2D startPoint2=new Point2D(row-3,column-3);
		List<Point2D> diagonal2Points=IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint2.add(i,i))
				.collect(Collectors.toList());


		boolean isEnded=checkCombinations(verticalPoints)||checkCombinations(horizontalPoints)
				         || checkCombinations(diagonal1Points)
				         || checkCombinations(diagonal2Points);

      return isEnded;
	}

	private boolean checkCombinations(List<Point2D> points) {
		int chain = 0;
		for (Point2D point : points) {

			int rowIndexForArray = (int) point.getX();
			int columnIndexForArray = (int) point.getY();
			Disc disc = getDiscIfPresent(rowIndexForArray,columnIndexForArray);
			if (disc != null && disc.isPlayerOneMove == isPLAYERoneTURN) {
				chain++;
				if (chain == 4) {
					return true;
				}
			} else {
				chain = 0;
			}
		}
		return false;
	}
	private Disc getDiscIfPresent(int row,int column)
	{
		//To Prevent ArrayIndexOutOfBoundsException
		if(row>=ROWS || row<0 || column>=COLUMNS || column<0)
			return null;

		return insertDisc[row][column];

	}


	private void gameOver()
	{
		String winner=isPLAYERoneTURN?PLAYER_ONE:PLAYER_TWO;
		System.out.println("Winner is "+winner);
		Alert alert=new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The Winner is "+winner);
		alert.setContentText("Want to Play Again ?");

		ButtonType yesBtn=new ButtonType("Yes");
		ButtonType noBtn=new ButtonType("No,Exit");
		alert.getButtonTypes().setAll(yesBtn,noBtn);
		Platform.runLater(() ->{
			Optional<ButtonType> btnClicked=alert.showAndWait();
			if(btnClicked.isPresent() && btnClicked.get()==yesBtn)
			{
				resetGame();
			}
			else
			{
				//to exit from the game
				Platform.exit();
				System.exit(0);
			}
		});



	}

	public void resetGame()
	{
		insertedDiscPane.getChildren().clear();//To remove the inserted Disc from the Pane

		for(int row=0;row<insertDisc.length;row++)
		{
			for (int col=0;col<insertDisc[row].length;col++)
			{
				insertDisc[row][col]=null;
			}
		}
		isPLAYERoneTURN=true;
		playerNameLabel.setText(PLAYER_ONE);
		createPlayground();
	}

	private static class Disc extends Circle
	{
		private final boolean isPlayerOneMove;

		public Disc(boolean isPlayerOneMove)
		{
			this.isPlayerOneMove=isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER/2);
			setFill(isPlayerOneMove?Color.valueOf(DISC_COLOR1):Color.valueOf(DISC_COLOR2));
			setCenterX(CIRCLE_DIAMETER/2);
			setCenterY(CIRCLE_DIAMETER/2);

		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{


	}
}
