/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package com.io7m.digal.tests;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.io7m.digal.core.DialControl;
import com.io7m.digal.core.DialValueConverterType;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.Stop;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.github.romankh3.image.comparison.model.ImageComparisonState.MATCH;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
public final class DialControlTest
{
  private Stage stageCurrent;

  /**
   * Test that adjusting the dial changes the value.
   *
   * @param robot The FX robot
   * @param info  The test info
   */

  @Test
  public void testDial(
    final FxRobot robot,
    final TestInfo info)
  {
    Platform.runLater(() -> {
      this.stageCurrent.setTitle(
        "%s: %s".formatted(info.getTestClass().get(), info.getDisplayName())
      );
    });

    final DialControl dial =
      robot.lookup("#dial0")
        .query();

    robot.targetWindow(dial)
      .clickOn(dial, MouseButton.PRIMARY);

    final var target =
      robot.point(dial)
        .atOffset(0.0, -32.0);

    robot.drag(dial, MouseButton.PRIMARY);
    robot.dropTo(target);

    FxAssert.verifyThat(dial, node -> {
      final var x = node.tickCount().get();
      return x == 12;
    });

    FxAssert.verifyThat(dial, node -> {
      final var x = node.convertedValue().get();
      return x == 2.0;
    });

    FxAssert.verifyThat(dial, node -> {
      final var x = node.rawValue().get();
      return x == 0.16500000000000006;
    });
  }

  /**
   * Test that custom CSS changes the appearance.
   *
   * @param robot The FX robot
   * @param info  The test info
   */

  @Test
  public void testCSS(
    final FxRobot robot,
    final TestInfo info)
    throws Exception
  {
    Platform.runLater(() -> {
      this.stageCurrent.setTitle(
        "%s: %s".formatted(info.getTestClass().get(), info.getDisplayName())
      );
    });

    final DialControl dial =
      robot.lookup("#dial0")
        .query();

    robot.targetWindow(dial)
        .clickOn(dial, MouseButton.PRIMARY);

    dial.getStylesheets()
      .add(DialControlTest.class.getResource("/com/io7m/digal/tests/style.css")
             .toString());

    dial.applyCss();
    dial.setRawValue(0.3);

    robot.sleep(1L, TimeUnit.SECONDS);

    /*
     * Capture an image of the scene and save it.
     */

    final var scene = (Scene) dial.getScene();
    final var imageReceived =
      new WritableImage(
        (int) scene.getWidth(),
        (int) scene.getHeight()
      );

    final var latch = new CountDownLatch(1);
    Platform.runLater(() -> {
      scene.snapshot(param -> {
        latch.countDown();
        return null;
      }, imageReceived);
    });
    latch.await(5L, TimeUnit.SECONDS);

    final var imageReceivedOutput =
      new BufferedImage(
        (int) imageReceived.getWidth(),
        (int) imageReceived.getHeight(),
        BufferedImage.TYPE_INT_ARGB
      );

    final var graphics = imageReceivedOutput.createGraphics();
    final var reader = imageReceived.getPixelReader();
    for (int y = 0; y < (int) imageReceived.getHeight(); ++y) {
      for (int x = 0; x < (int) imageReceived.getWidth(); ++x) {
        final var argb = reader.getArgb(x, y);
        final var a = (argb >> 24) & 0xff;
        final var r = (argb >> 16) & 0xff;
        final var g = (argb >> 8) & 0xff;
        final var b = (argb & 0xff);
        graphics.setPaint(new Color(r, g, b, a));
        graphics.fillRect(x, y, 1, 1);
      }
    }
    graphics.dispose();

    ImageIO.write(
      imageReceivedOutput,
      "PNG",
      new File("testCSS.png")
    );

    final var imageExpected =
      loadSampleImage();

    final var imageComparison =
      new ImageComparison(imageExpected, imageReceivedOutput);
    final var imageComparisonResult =
      imageComparison.compareImages();

    assertEquals(MATCH, imageComparisonResult.getImageComparisonState());
  }

  private static BufferedImage loadSampleImage()
    throws IOException
  {
    final var stream =
      DialControlTest.class.getResource(
        "/com/io7m/digal/tests/dial.png");

    return ImageIO.read(stream);
  }

  @Start
  public void start(
    final Stage stage)
    throws Exception
  {
    this.stageCurrent = stage;

    final var pane = new StackPane();
    pane.setPrefSize(640, 480);
    pane.setPadding(new Insets(8));

    final var dial0 = new DialControl();
    final var dialSize = 128.0;
    dial0.setPrefSize(dialSize, dialSize);
    dial0.setMinSize(dialSize, dialSize);
    dial0.setMaxSize(dialSize, dialSize);

    dial0.setId("dial0");
    dial0.setTickCount(12);
    dial0.setValueConverter(
      new DialValueConverterType()
      {
        @Override
        public double convertToDial(
          final double x)
        {
          return x / 12.0;
        }

        @Override
        public double convertFromDial(
          final double x)
        {
          return (double) Math.round(x * 12.0);
        }
      });

    pane.getChildren().addAll(dial0);

    final var scene = new Scene(pane);
    stage.setTitle("Dial Control");
    stage.setScene(scene);
    stage.show();
  }

  @Stop
  public void stop()
    throws Exception
  {

  }
}
