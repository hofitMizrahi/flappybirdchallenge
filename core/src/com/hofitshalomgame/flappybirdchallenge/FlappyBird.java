package com.hofitshalomgame.flappybirdchallenge;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

	private SpriteBatch batch;
	private Texture background;
	private Texture gameOver;
	private int score = 0;
	private int scoringTube = 0;

	private Preferences prefs;
	private int highScore = 0;
	private float scoreWidth;
//	private GlyphLayout glyphLayout;

	// to write Gdx text need to use font
	private BitmapFont font;
//	BitmapFont font12;

	// music
	private Music coinSound;

	private Texture[] birds;
	private int flapState = 0;
	private float birdY = 0;
	private float velocity = 1;
	// create shape for collision detection
	private Circle birdCircle;
	//ShapeRenderer shapeRenderer;


	private int gameState = 0;
	private float gravity = 2;

	private Texture topTube;
	private Texture bottomTube;
	private float gap = 500;
	private float maxTubeOffset;
	private Random randomGenerator;
	private float tubeVelocity = 10;
	private int numberOfTubes = 4;
	private float[] tubeX = new float[numberOfTubes];
	private float[] tubeOffset = new float[numberOfTubes];
	private float distanceBetweenTubes;

	private Rectangle[] topTubeRectangles;
	private Rectangle[] bottomTubeRectangles;
	private ShapeRenderer tubeShapeRenderer;


	@Override
	public void create() {

//		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("number.ttf"));
//		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
//		parameter.size = 12;
//		parameter.borderColor = Color.WHITE;
//		parameter.borderWidth = 3;
//		font12 = (BitmapFont) generator.generateFont(parameter); // font size 12 pixels
//		generator.dispose();
		BitmapFont font = new BitmapFont(Gdx.files.internal("1.ttf"), false);


		prefs = Gdx.app.getPreferences("My Preferences");
		highScore = prefs.getInteger("score", 0);
		Gdx.app.log("highscore", String.valueOf(highScore));

		batch = new SpriteBatch();
		background = new Texture("bg.png");
		birds = new Texture[2];
		birds[0] = new Texture("bird.png");
		birds[1] = new Texture("bird2.png");

		//shapeRenderer = new ShapeRenderer();
		birdCircle = new Circle();

		// init the font of the score
		// set size and color
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(10);


		glyphLayout = new GlyphLayout();
		String item = String.valueOf(score);
		glyphLayout.setText(font, item);
		scoreWidth = glyphLayout.width;

		topTube = new Texture("toptube.png");
		bottomTube = new Texture("bottomtube.png");

		topTubeRectangles = new Rectangle[numberOfTubes];
		bottomTubeRectangles = new Rectangle[numberOfTubes];
		tubeShapeRenderer = new ShapeRenderer();

		maxTubeOffset = Gdx.graphics.getHeight() / 2 - gap / 2 - 100;
		randomGenerator = new Random();
		distanceBetweenTubes = Gdx.graphics.getWidth() * 2 / 4;

		startGame();

		gameOver = new Texture("gameover.png");

		coinSound = Gdx.audio.newMusic(Gdx.files.internal("coinsounds.mp3"));
		coinSound.setVolume(0.2f);
	}

	private void startGame() {

		birdY = Gdx.graphics.getHeight() / 2 - birds[0].getHeight() / 2;

		for (int i = 0; i < numberOfTubes; i++) {

			tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - 200);
			tubeX[i] = Gdx.graphics.getWidth() / 2 - topTube.getWidth() / 2 + Gdx.graphics.getWidth() + i * distanceBetweenTubes;

			// shape to interact with the tube
			topTubeRectangles[i] = new Rectangle();
			bottomTubeRectangles[i] = new Rectangle();
		}
	}

	@Override
	public void render() {

		// display the background
		batch.begin();
		batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		switch (gameState){

			// bird don't move until the first touch
			case 0:

				if (Gdx.input.justTouched()) {
					gameState = 1;
				}
				break;

			// when the game is ACTIVE
			case 1:

				//scoring treatment
				if (tubeX[scoringTube] < (Gdx.graphics.getWidth() / 2 - birds[flapState].getWidth() / 2) - 400) {

					coinSound.play();
					score++;
					glyphLayout.setText(font, String.valueOf(score));
					scoreWidth = glyphLayout.width;

					if (scoringTube < numberOfTubes - 1) {

						scoringTube++;

					} else {

						scoringTube = 0;
					}
				}

				// when the user touch on the screen
				//call every time the screen bin touched
				if (Gdx.input.justTouched()) {

					velocity = -30;
				}


				for (int i = 0; i < numberOfTubes; i++) {

					if (tubeX[i] < -topTube.getWidth()) {

						tubeX[i] += numberOfTubes * distanceBetweenTubes;
						tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - 300);

					} else {

						tubeX[i] = tubeX[i] - tubeVelocity;
					}

					// set the size for each tube (x and y)
					batch.draw(topTube, tubeX[i], Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i]);
					batch.draw(bottomTube, tubeX[i], Gdx.graphics.getHeight() / 2 - gap / 2 - bottomTube.getHeight() + tubeOffset[i]);

					topTubeRectangles[i] = new Rectangle(tubeX[i], Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i], topTube.getWidth(), topTube.getHeight());
					bottomTubeRectangles[i] = new Rectangle(tubeX[i], Gdx.graphics.getHeight() / 2 - gap / 2 - bottomTube.getHeight() + tubeOffset[i], bottomTube.getWidth(), bottomTube.getHeight());

				}

				// the bird don't out of the screen (X, 0)
				if (birdY > 0) {

					velocity += gravity;
					birdY -= velocity;

				} else {

					//if the bird on the bottom of the screen
					// game over!!
					gameState = 2;
				}

				break;

			// game over state
			case 2:

				batch.draw(gameOver, Gdx.graphics.getWidth() / 2 - gameOver.getWidth() / 2, Gdx.graphics.getHeight() / 2 - gameOver.getHeight() / 2);

				if(highScore < score){

					prefs.putInteger("score", score);
					prefs.flush();

					highScore = score;
					Gdx.app.log("highscore", String.valueOf(highScore));
				}

				//TODO advertising

				// bird don't move until the first touch
				if (Gdx.input.justTouched()) {

					gameState = 1;
					startGame();
					score = 0;
					scoringTube = 0;
					velocity = 0;
				}
				break;
		}

		// state for the bird flying
		if (flapState == 0) {

			flapState = 1;

		} else {

			flapState = 0;
		}

		//circle that overlaps were the bird is
		// display the bird
		batch.draw(birds[flapState], (Gdx.graphics.getWidth() / 2 - birds[flapState].getWidth() / 2) - 400 , birdY);


		//set the location and the radius of the circle
		birdCircle.set(Gdx.graphics.getWidth() / 2  - 400 , birdY + 25,  birds[flapState].getWidth() / 2);

		// create the font view on the batch
		font12.draw(batch, String.valueOf(score), Gdx.graphics.getWidth() / 2 - scoreWidth / 2, Gdx.graphics.getHeight() - 110);

		//shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		//shapeRenderer.setColor(Color.RED);
		//shapeRenderer.circle(birdCircle.x, birdCircle.y ,birdCircle.radius);
		batch.end();

		for (int i = 0; i < numberOfTubes; i++) {

			//shapeRenderer.rect(tubeX[i], Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i], topTube.getWidth(), topTube.getHeight());
			//shapeRenderer.rect(tubeX[i], Gdx.graphics.getHeight() / 2 - gap / 2 - bottomTube.getHeight() + tubeOffset[i], bottomTube.getWidth(), bottomTube.getHeight());

			// check if the circle are clash on the Rectangles
			if (Intersector.overlaps(birdCircle, topTubeRectangles[i]) || Intersector.overlaps(birdCircle, bottomTubeRectangles[i])) {

				// game over!!
				gameState = 2;
			}
		}

		//shapeRenderer.end();
	}
}
