
import acm.graphics.*;
import acm.program.*;
import acm.util.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {
	
	
	public Breakout()
	{
		
	}

	public Breakout(int ballCount,int width, int height)
	{
		
	}
	
	// Width and height of application window in pixels 

	public static final int APPLICATION_WIDTH = 600;
	public static final int APPLICATION_HEIGHT = 620;

	// Dimensions of the game board
	private static final int WIDTH = 400;
	private static final int HEIGHT = 600;
	private static final int FRAME_TOP = (APPLICATION_HEIGHT - HEIGHT) / 2;
	private static final int FRAME_BOTTOM = FRAME_TOP + HEIGHT;
	private static final int FRAME_LEFT = (APPLICATION_WIDTH - WIDTH) / 2;
	private static final int FRAME_RIGHT = FRAME_LEFT + WIDTH;

	// paddle dimension
	private static final int PADDLE_WIDTH = 60;
	private static final int HALF_PADDLE_WIDTH = PADDLE_WIDTH / 2;
	private static final int PADDLE_HEIGHT = PADDLE_WIDTH / 6;
	private static final int PADDLE_Y_OFFSET = 40; // Offset of the paddle up
													// from the bottom
	private static final int PADDLE_Y = FRAME_TOP + HEIGHT - PADDLE_Y_OFFSET; // y
																				// coordinate
																				// of
																				// paddle

	// Brick constants 
	private static final int BRICK_HEIGHT = 9; // height of bricks
	private static final int NBRICKS_PER_ROW = 10; // number of bricks per row
	private static final int NBRICK_ROWS = 10; // number of rows
	private static final int BRICK_SEP = 4; // Separation between bricks
	private static final int BRICK_Y_OFFSET = FRAME_TOP
			+ ((BRICK_HEIGHT + BRICK_SEP) * 6); // Offset of the top brick row
												// from the top

	private static final int BRICK_WIDTH = (WIDTH - (NBRICKS_PER_ROW - 1)
			* BRICK_SEP)
			/ NBRICKS_PER_ROW; // width of bricks

	private static final Color[] BRICK_COLOR = { Color.red, Color.orange,
			Color.yellow, Color.green, Color.cyan }; // colors of bricks

	// Ball constants 
	private static final int BALL_DIAMETER = 18; // multiples of 2
	private static final int BALL_RADIUS = BALL_DIAMETER / 2;
	private static final Color BALL_COLOR = Color.BLACK;
	private static final double BALL_DELAY = 10;
	private static final int NBALLS = 3; // number of turns

	// Instance variables 
	private static final double minVel = 2; // min Velocity on x or y
	private static final double maxVel = 5; // min Velocity on x or y
	private static double xVel = minVel; // ball x velocity
	private static double yVel = minVel; // ball y velocity
	private static double xVelBefore = xVel; // to keep trak of direction of
												// ball before a bounce
	private static double yVelBefore = yVel; // to keep trak of direction of
												// ball before a bounce
	private static int bricksLeft; // number of remaining bricks
	private static int paddleContacts; // number times the ball has hit the
										// paddle
	private static String currentCollider; // name of colliding object
	private static boolean JustBoncedInPaddle; // to prevent the ball bouncing
												// twice on paddle
	private static int ballsLeft = NBALLS; // number of remaining turns
	private static double ballTop; // y coordinate of top of ball
	private static double ballBottom; // y coordinate of top of ball
	private static double ballLeft; // x coordinate of leftmost of ball
	private static double ballRight; // x coordinate of rightmost of ball

	// instance objects 
	private static GRect brick;
	private static GRect paddle;
	private static GOval ball;
	private static GObject collider;
	private static GLabel messageLabel;
	private GOval[] ballIndicator = { new GOval(1, 1), new GOval(1, 1),
			new GOval(1, 1) };

	private RandomGenerator rgen = RandomGenerator.getInstance();


	public void run() {

		while (true) {
			Initialize();
			play();

			if (ballsLeft != 0)
				displayMessage("YOU WIN!");

			else
				displayMessage("GAME OVER");
		}
	}

	private void Initialize() {

		ballsLeft = NBALLS;
		paddleContacts = 0;
		currentCollider = null;
		JustBoncedInPaddle = false;

		setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT); // resize window
		removeAll();
		addFrame();
		addBricks();
		addPaddle();
		addBallIndicators();
		addMouseListeners();
		// addDebugLabels(); //debuging only
	}

	private void play() {

		double colliderWidth;

		while (ballsLeft > 0 && bricksLeft > 0) {

			displayMessage("Click to Play");
			getBallAngleAndDir();
			addBall();
			removeBallsIndicator();
			--ballsLeft;
			// updateBallsLabel(); //debug only

			while (bricksLeft > 0) {

				// updateBallVelLabel(); //debug only
				moveBall();

				if (checkForWallCollision() == false) {
					break; // ball hit bottom so put a new ball
				}

				// check for collition on 8 sections of ball
				for (int i = 1; i <= 8; i++) {

					collider = getCollidingObject(i);

					if (collider == null) {
						currentCollider = null;
						// updateColliderLabel(); // debug only
					}

					else {

						colliderWidth = collider.getWidth();

						if (colliderWidth >= WIDTH) {
							currentCollider = "wall";
							// updateColliderLabel(); // debug only
						}

						else {

							// if collider is brick...
							if (colliderWidth == BRICK_WIDTH) {
								currentCollider = "brick";
								remove(collider);
								--bricksLeft;
								// updateBricksLeftLabel(); // debug only
								bounceBall(i);

								// updateColliderLabel(); // debug only
								// waitForClick(); //debug only
							}

							// if collider is paddle...
							else if (JustBoncedInPaddle == false) {
								currentCollider = "paddle";
								bounceBall(i);

								++paddleContacts;

								if (paddleContacts % 7 == 0
										&& (Math.abs(yVel) * 1.1) < maxVel) {
									yVel += yVel * 0.1; // increase y Velocity
														// by 10%
								}
								// updateColliderLabel(); //debug only
								// waitForClick(); //debug only
							}
						}
					}
				}

				// waitForClick(); //debug only
				// updateColliderLabel(); //debug only
				// updateElementCountLabel(); //debug only

				xVelBefore = xVel;
				yVelBefore = yVel;
				pause(BALL_DELAY);
			}
		}
		return;
	}

	private void addFrame() {
		GRoundRect frame1 = new GRoundRect(FRAME_LEFT, FRAME_TOP, WIDTH, HEIGHT);
		add(frame1);
	}

	private void addBricks() {

		int brickX = (FRAME_LEFT + (BRICK_SEP / 2)); // get Initial X position
		int brickY = (BRICK_Y_OFFSET); // get initial Y position
		int initialX = brickX;
		Color brickColor = BRICK_COLOR[0];

		bricksLeft = NBRICK_ROWS * NBRICKS_PER_ROW;

		for (int j = 1; j <= NBRICK_ROWS; j++) {

			// add row of bricks
			for (int i = 0; i < NBRICKS_PER_ROW; i++) {
				brick = new GRect(brickX, brickY, BRICK_WIDTH, BRICK_HEIGHT);
				brick.setColor(brickColor);
				brick.setFilled(true);
				add(brick);
				brickX += BRICK_WIDTH + BRICK_SEP;
			}
			// change brick color every two rows
			if (j % 2 == 0 && j < NBRICK_ROWS) {
				brickColor = BRICK_COLOR[j / 2];
			}

			brickY += BRICK_HEIGHT + BRICK_SEP;
			brickX = initialX;
		}
	}

	private void addPaddle() {

		paddle = new GRect(FRAME_LEFT + (WIDTH / 2) - (PADDLE_WIDTH / 2),
				PADDLE_Y, PADDLE_WIDTH, PADDLE_HEIGHT);
		paddle.setColor(Color.gray);
		paddle.setFilled(true);
		add(paddle);
	}

	private void addBall() {

		ball = new GOval(FRAME_LEFT + (WIDTH / 2) - BALL_RADIUS, FRAME_TOP
				+ (HEIGHT / 2) - BALL_RADIUS, BALL_DIAMETER, BALL_DIAMETER);

		ball.setColor(BALL_COLOR);
		ball.setFilled(true);
		add(ball);
	}

	private void moveBall() {
		ball.move(xVel, yVel);
	}

	private boolean checkForWallCollision() {

		/** ToDo: need make sure to check middle of ball */
		ballTop = ball.getY() - 1;
		ballBottom = ballTop + BALL_DIAMETER + 3;
		ballLeft = ball.getX() - 1;
		ballRight = ballLeft + BALL_DIAMETER + 3;

		if (ballBottom > FRAME_BOTTOM) {
			remove(ball);
			return false;
		}

		else if (ballTop < FRAME_TOP) {
			bounceBall(1);

			// ballDelayOn = false; //debug only
		}

		if (ballLeft < FRAME_LEFT) {
			bounceBall(3);

		}

		else if (ballRight > FRAME_RIGHT) {
			bounceBall(3);

		}
		return true;
	}

	private GObject getCollidingObject(int ballSection) {

		switch (ballSection) {

		// ball North
		case 1:
			return getElementAt(ballLeft + BALL_RADIUS + 2, ballTop);

			// ball South
		case 2:
			return getElementAt(ballLeft + BALL_RADIUS + 2, ballBottom);

			// ball East
		case 3:
			return getElementAt(ballRight, ballTop + BALL_RADIUS + 2);

			// ball West
		case 4:
			return getElementAt(ballLeft, ballTop + BALL_RADIUS + 2);

			// ball North-East
		case 5:
			return getElementAt(ballRight - (BALL_RADIUS / 3), ballTop
					+ (BALL_RADIUS / 3));

			// ball South-East
		case 6:
			return getElementAt(ballRight - (BALL_RADIUS / 3), ballBottom
					- (BALL_RADIUS / 3));

			// ball North-West
		case 7:
			return getElementAt(ballLeft + (BALL_RADIUS / 3), ballTop
					+ (BALL_RADIUS / 3));

			// ball South-West
		case 8:
			return getElementAt(ballLeft + (BALL_RADIUS / 3), ballBottom
					- (BALL_RADIUS / 3));
		}

		return null;
	}

	private void getBallAngleAndDir() {

		xVel = rgen.nextDouble(minVel, maxVel);
		if (rgen.nextBoolean(0.5)) {
			xVel = -xVel;
		}
	}

	// bounce ball on X, Y, or both direction
	private void bounceBall(int ballSection) {

		if (ballSection == 1 || ballSection == 2) {
			if (yVel == yVelBefore)
				yVel = -yVel;
		}

		else if (ballSection == 3 || ballSection == 4) {
			if (xVel == xVelBefore)
				xVel = -xVel;
		}

		else {
			if (yVel == yVelBefore)
				yVel = -yVel;
			if (xVel == xVelBefore)
				xVel = -xVel;
		}

		if (currentCollider == "paddle")
			JustBoncedInPaddle = true;

		else
			JustBoncedInPaddle = false;
	}

	private void displayMessage(String message) {
		messageLabel = new GLabel(message);
		messageLabel.setFont(new Font("Verdana", Font.ITALIC, 38));
		messageLabel.setLocation(
				FRAME_LEFT + (WIDTH / 2) - (messageLabel.getWidth() / 2),
				FRAME_TOP + (HEIGHT / 2));
		add(messageLabel);
		waitForClick();
		remove(messageLabel);
	}

	private void addBallIndicators() {
		for (int i = 0; i < 3; i++) {

			addBall();
			ballIndicator[i] = ball;
			remove(ball);
			ballIndicator[i].setLocation(FRAME_RIGHT + BALL_RADIUS, FRAME_TOP
					+ (i * 3 * BALL_RADIUS));
			add(ballIndicator[i]);
		}
	}

	private void removeBallsIndicator() {
		remove(ballIndicator[ballsLeft - 1]);
	}

	public void mouseMoved(MouseEvent e) {

		int mouseX = e.getX();

		if ((mouseX > (FRAME_LEFT + HALF_PADDLE_WIDTH))
				&& (mouseX < (FRAME_RIGHT - HALF_PADDLE_WIDTH))
				&& currentCollider != "paddle") {
			paddle.setLocation(mouseX - HALF_PADDLE_WIDTH, PADDLE_Y);
		}
		// for debugging
	}
}