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

import com.io7m.digal.core.DialControlLabelled;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.Stop;

import java.util.List;

@ExtendWith(ApplicationExtension.class)
public final class DialControlLabelledTest
{
  private Stage stageCurrent;

  /**
   * Labelled dials load without errors.
   *
   * @param robot The FX robot
   * @param info  The test info
   *
   * @throws Exception On errors
   */

  @Test
  public void testDial(
    final FxRobot robot,
    final TestInfo info)
    throws Exception
  {
    Platform.runLater(() -> {
      this.stageCurrent.setTitle(
        "%s: %s".formatted(info.getTestClass().get(), info.getDisplayName())
      );
    });

    /*
     * Capture an image of the scene and save it.
     */

    final var root =
      robot.lookup("#root")
        .query();

    DialImageComparisons.compareSampleImageWithScene(
      "testDialsLabelled.png",
      root.getScene(),
      1.5
    );
  }

  @Start
  public void start(
    final Stage stage)
    throws Exception
  {
    this.stageCurrent = stage;

    final var pane = new FlowPane();
    pane.setId("root");
    pane.setPrefSize(640, 480);
    pane.setPadding(new Insets(8));

    final var dials = List.of(
      new DialControlLabelled("GAIN"),
      new DialControlLabelled("LPF"),
      new DialControlLabelled("HPF"),
      new DialControlLabelled("PITCH 0"),
      new DialControlLabelled("PITCH 1"),
      new DialControlLabelled("DLY TIME"),
      new DialControlLabelled("DLY MIX")
    );

    dials.forEach(dial -> {
      dial.setPrefWidth(80.0);
      dial.dial().dialRadialGaugeSize().setValue(6.0);
    });

    pane.getChildren().addAll(dials);

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
