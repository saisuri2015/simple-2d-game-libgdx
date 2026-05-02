package com.example.claw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {
    private static final float WORLD_WIDTH = 64f;
    private static final float WORLD_HEIGHT = 36f;
    private static final float GRAVITY = -60f;
    private static final float PLAYER_SPEED = 15f;
    private static final float JUMP_SPEED = 28f;

    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();

    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

    private final Rectangle player = new Rectangle(3, 5, 2.2f, 3.6f);
    private final Vector2 playerVelocity = new Vector2();
    private boolean grounded;

    private final Array<Rectangle> platforms = new Array<>();
    private final Array<Rectangle> coins = new Array<>();
    private final Array<PatrolEnemy> enemies = new Array<>();

    private int score;
    private boolean gameOver;
    private boolean win;

    public GameScreen() {
        generateLevel();
        camera.position.set(player.x + 8f, WORLD_HEIGHT / 2f, 0f);
    }

    private void generateLevel() {
        platforms.add(new Rectangle(0f, 0f, 260f, 3f));
        platforms.add(new Rectangle(10f, 7f, 8f, 1.2f));
        platforms.add(new Rectangle(22f, 10f, 9f, 1.2f));
        platforms.add(new Rectangle(38f, 6f, 10f, 1.2f));
        platforms.add(new Rectangle(52f, 12f, 9f, 1.2f));
        platforms.add(new Rectangle(70f, 16f, 10f, 1.2f));
        platforms.add(new Rectangle(89f, 9f, 10f, 1.2f));
        platforms.add(new Rectangle(108f, 14f, 12f, 1.2f));
        platforms.add(new Rectangle(130f, 18f, 8f, 1.2f));
        platforms.add(new Rectangle(146f, 11f, 11f, 1.2f));
        platforms.add(new Rectangle(172f, 15f, 10f, 1.2f));
        platforms.add(new Rectangle(192f, 19f, 12f, 1.2f));

        for (int i = 0; i < 18; i++) {
            float x = 12f + i * 10.5f;
            float y = 5f + MathUtils.random(2f, 14f);
            coins.add(new Rectangle(x, y, 1f, 1f));
        }

        enemies.add(new PatrolEnemy(28f, 3f, 26f, 36f));
        enemies.add(new PatrolEnemy(76f, 17.2f, 70f, 80f));
        enemies.add(new PatrolEnemy(156f, 12.2f, 146f, 157f));
        enemies.add(new PatrolEnemy(196f, 20.2f, 192f, 203f));
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && (gameOver || win)) {
            reset();
        }

        if (gameOver || win) {
            return;
        }

        float moveX = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX -= PLAYER_SPEED;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX += PLAYER_SPEED;
        }
        playerVelocity.x = moveX;

        if (grounded && (Gdx.input.isKeyJustPressed(Input.Keys.W)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))) {
            playerVelocity.y = JUMP_SPEED;
            grounded = false;
        }

        playerVelocity.y += GRAVITY * delta;
        moveAndCollide(delta);

        for (PatrolEnemy enemy : enemies) {
            enemy.update(delta);
            if (enemy.bounds.overlaps(player)) {
                gameOver = true;
            }
        }

        for (int i = coins.size - 1; i >= 0; i--) {
            if (coins.get(i).overlaps(player)) {
                coins.removeIndex(i);
                score += 10;
            }
        }

        if (player.y < -8f) {
            gameOver = true;
        }
        if (player.x > 225f) {
            win = true;
        }

        camera.position.x = MathUtils.clamp(player.x + 15f, WORLD_WIDTH / 2f, 225f);
        camera.update();
    }

    private void moveAndCollide(float delta) {
        player.x += playerVelocity.x * delta;

        player.y += playerVelocity.y * delta;
        grounded = false;
        for (Rectangle platform : platforms) {
            if (player.overlaps(platform)) {
                if (playerVelocity.y <= 0 && player.y + player.height - playerVelocity.y * delta >= platform.y + platform.height) {
                    player.y = platform.y + platform.height;
                    playerVelocity.y = 0f;
                    grounded = true;
                }
            }
        }
    }

    private void draw() {
        ScreenUtils.clear(0.08f, 0.1f, 0.18f, 1f);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(new Color(0.18f, 0.20f, 0.26f, 1f));
        shapeRenderer.rect(camera.position.x - WORLD_WIDTH, 0, WORLD_WIDTH * 2, 3f);

        shapeRenderer.setColor(new Color(0.45f, 0.32f, 0.20f, 1f));
        for (Rectangle platform : platforms) {
            shapeRenderer.rect(platform.x, platform.y, platform.width, platform.height);
        }

        shapeRenderer.setColor(new Color(0.95f, 0.82f, 0.22f, 1f));
        for (Rectangle coin : coins) {
            shapeRenderer.rect(coin.x, coin.y, coin.width, coin.height);
        }

        shapeRenderer.setColor(new Color(0.85f, 0.20f, 0.22f, 1f));
        for (PatrolEnemy enemy : enemies) {
            shapeRenderer.rect(enemy.bounds.x, enemy.bounds.y, enemy.bounds.width, enemy.bounds.height);
        }

        shapeRenderer.setColor(new Color(0.26f, 0.65f, 0.95f, 1f));
        shapeRenderer.rect(player.x, player.y, player.width, player.height);

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, "Treasure: " + score, camera.position.x - WORLD_WIDTH / 2f + 1f, WORLD_HEIGHT - 1f);
        font.draw(batch, "Reach the ship at x=225", camera.position.x - WORLD_WIDTH / 2f + 1f, WORLD_HEIGHT - 3f);
        if (gameOver) {
            font.draw(batch, "You were defeated! Press R to restart", camera.position.x - 12f, WORLD_HEIGHT / 2f + 3f);
        } else if (win) {
            font.draw(batch, "You escaped with treasure! Press R to replay", camera.position.x - 14f, WORLD_HEIGHT / 2f + 3f);
        }
        batch.end();
    }

    private void reset() {
        platforms.clear();
        coins.clear();
        enemies.clear();
        score = 0;
        gameOver = false;
        win = false;
        player.set(3, 5, 2.2f, 3.6f);
        playerVelocity.setZero();
        grounded = false;
        generateLevel();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }

    private static class PatrolEnemy {
        private final Rectangle bounds;
        private final float minX;
        private final float maxX;
        private float speed = 4f;

        PatrolEnemy(float x, float y, float minX, float maxX) {
            this.bounds = new Rectangle(x, y, 2f, 2.8f);
            this.minX = minX;
            this.maxX = maxX;
        }

        void update(float delta) {
            bounds.x += speed * delta;
            if (bounds.x <= minX || bounds.x + bounds.width >= maxX) {
                speed = -speed;
            }
        }
    }
}
