// Generated by view binder compiler. Do not edit!
package com.myapp.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.myapp.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class MainBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button aboutButton;

  @NonNull
  public final ImageButton aboutImageButton;

  @NonNull
  public final Button controlsButton;

  @NonNull
  public final ImageButton controlsImageButton;

  @NonNull
  public final Button helpButton;

  @NonNull
  public final ImageButton helpImageButton;

  @NonNull
  public final Button highScoreButton;

  @NonNull
  public final ImageButton highScoreImageButton;

  @NonNull
  public final LinearLayout mainRoot;

  @NonNull
  public final Button playButton;

  @NonNull
  public final ImageButton playImageButton;

  @NonNull
  public final Button resetDataButton;

  @NonNull
  public final ImageButton resetDataImageButton;

  @NonNull
  public final ScrollView scrollView1;

  @NonNull
  public final Button soundButton;

  @NonNull
  public final ImageButton soundImageButton;

  @NonNull
  public final TableLayout tableLayout1;

  @NonNull
  public final TableRow tableRow1;

  @NonNull
  public final TableRow tableRow2;

  @NonNull
  public final TableRow tableRow3;

  @NonNull
  public final TableRow tableRow4;

  @NonNull
  public final TableRow tableRow5;

  @NonNull
  public final TableRow tableRow6;

  @NonNull
  public final TableRow tableRow7;

  @NonNull
  public final Toolbar toolbar;

  private MainBinding(@NonNull LinearLayout rootView, @NonNull Button aboutButton,
      @NonNull ImageButton aboutImageButton, @NonNull Button controlsButton,
      @NonNull ImageButton controlsImageButton, @NonNull Button helpButton,
      @NonNull ImageButton helpImageButton, @NonNull Button highScoreButton,
      @NonNull ImageButton highScoreImageButton, @NonNull LinearLayout mainRoot,
      @NonNull Button playButton, @NonNull ImageButton playImageButton,
      @NonNull Button resetDataButton, @NonNull ImageButton resetDataImageButton,
      @NonNull ScrollView scrollView1, @NonNull Button soundButton,
      @NonNull ImageButton soundImageButton, @NonNull TableLayout tableLayout1,
      @NonNull TableRow tableRow1, @NonNull TableRow tableRow2, @NonNull TableRow tableRow3,
      @NonNull TableRow tableRow4, @NonNull TableRow tableRow5, @NonNull TableRow tableRow6,
      @NonNull TableRow tableRow7, @NonNull Toolbar toolbar) {
    this.rootView = rootView;
    this.aboutButton = aboutButton;
    this.aboutImageButton = aboutImageButton;
    this.controlsButton = controlsButton;
    this.controlsImageButton = controlsImageButton;
    this.helpButton = helpButton;
    this.helpImageButton = helpImageButton;
    this.highScoreButton = highScoreButton;
    this.highScoreImageButton = highScoreImageButton;
    this.mainRoot = mainRoot;
    this.playButton = playButton;
    this.playImageButton = playImageButton;
    this.resetDataButton = resetDataButton;
    this.resetDataImageButton = resetDataImageButton;
    this.scrollView1 = scrollView1;
    this.soundButton = soundButton;
    this.soundImageButton = soundImageButton;
    this.tableLayout1 = tableLayout1;
    this.tableRow1 = tableRow1;
    this.tableRow2 = tableRow2;
    this.tableRow3 = tableRow3;
    this.tableRow4 = tableRow4;
    this.tableRow5 = tableRow5;
    this.tableRow6 = tableRow6;
    this.tableRow7 = tableRow7;
    this.toolbar = toolbar;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static MainBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static MainBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent,
      boolean attachToParent) {
    View root = inflater.inflate(R.layout.main, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static MainBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.aboutButton;
      Button aboutButton = ViewBindings.findChildViewById(rootView, id);
      if (aboutButton == null) {
        break missingId;
      }

      id = R.id.aboutImageButton;
      ImageButton aboutImageButton = ViewBindings.findChildViewById(rootView, id);
      if (aboutImageButton == null) {
        break missingId;
      }

      id = R.id.controlsButton;
      Button controlsButton = ViewBindings.findChildViewById(rootView, id);
      if (controlsButton == null) {
        break missingId;
      }

      id = R.id.controlsImageButton;
      ImageButton controlsImageButton = ViewBindings.findChildViewById(rootView, id);
      if (controlsImageButton == null) {
        break missingId;
      }

      id = R.id.helpButton;
      Button helpButton = ViewBindings.findChildViewById(rootView, id);
      if (helpButton == null) {
        break missingId;
      }

      id = R.id.helpImageButton;
      ImageButton helpImageButton = ViewBindings.findChildViewById(rootView, id);
      if (helpImageButton == null) {
        break missingId;
      }

      id = R.id.highScoreButton;
      Button highScoreButton = ViewBindings.findChildViewById(rootView, id);
      if (highScoreButton == null) {
        break missingId;
      }

      id = R.id.highScoreImageButton;
      ImageButton highScoreImageButton = ViewBindings.findChildViewById(rootView, id);
      if (highScoreImageButton == null) {
        break missingId;
      }

      LinearLayout mainRoot = (LinearLayout) rootView;

      id = R.id.playButton;
      Button playButton = ViewBindings.findChildViewById(rootView, id);
      if (playButton == null) {
        break missingId;
      }

      id = R.id.playImageButton;
      ImageButton playImageButton = ViewBindings.findChildViewById(rootView, id);
      if (playImageButton == null) {
        break missingId;
      }

      id = R.id.resetDataButton;
      Button resetDataButton = ViewBindings.findChildViewById(rootView, id);
      if (resetDataButton == null) {
        break missingId;
      }

      id = R.id.resetDataImageButton;
      ImageButton resetDataImageButton = ViewBindings.findChildViewById(rootView, id);
      if (resetDataImageButton == null) {
        break missingId;
      }

      id = R.id.scrollView1;
      ScrollView scrollView1 = ViewBindings.findChildViewById(rootView, id);
      if (scrollView1 == null) {
        break missingId;
      }

      id = R.id.soundButton;
      Button soundButton = ViewBindings.findChildViewById(rootView, id);
      if (soundButton == null) {
        break missingId;
      }

      id = R.id.soundImageButton;
      ImageButton soundImageButton = ViewBindings.findChildViewById(rootView, id);
      if (soundImageButton == null) {
        break missingId;
      }

      id = R.id.tableLayout1;
      TableLayout tableLayout1 = ViewBindings.findChildViewById(rootView, id);
      if (tableLayout1 == null) {
        break missingId;
      }

      id = R.id.tableRow1;
      TableRow tableRow1 = ViewBindings.findChildViewById(rootView, id);
      if (tableRow1 == null) {
        break missingId;
      }

      id = R.id.tableRow2;
      TableRow tableRow2 = ViewBindings.findChildViewById(rootView, id);
      if (tableRow2 == null) {
        break missingId;
      }

      id = R.id.tableRow3;
      TableRow tableRow3 = ViewBindings.findChildViewById(rootView, id);
      if (tableRow3 == null) {
        break missingId;
      }

      id = R.id.tableRow4;
      TableRow tableRow4 = ViewBindings.findChildViewById(rootView, id);
      if (tableRow4 == null) {
        break missingId;
      }

      id = R.id.tableRow5;
      TableRow tableRow5 = ViewBindings.findChildViewById(rootView, id);
      if (tableRow5 == null) {
        break missingId;
      }

      id = R.id.tableRow6;
      TableRow tableRow6 = ViewBindings.findChildViewById(rootView, id);
      if (tableRow6 == null) {
        break missingId;
      }

      id = R.id.tableRow7;
      TableRow tableRow7 = ViewBindings.findChildViewById(rootView, id);
      if (tableRow7 == null) {
        break missingId;
      }

      id = R.id.toolbar;
      Toolbar toolbar = ViewBindings.findChildViewById(rootView, id);
      if (toolbar == null) {
        break missingId;
      }

      return new MainBinding((LinearLayout) rootView, aboutButton, aboutImageButton, controlsButton,
          controlsImageButton, helpButton, helpImageButton, highScoreButton, highScoreImageButton,
          mainRoot, playButton, playImageButton, resetDataButton, resetDataImageButton, scrollView1,
          soundButton, soundImageButton, tableLayout1, tableRow1, tableRow2, tableRow3, tableRow4,
          tableRow5, tableRow6, tableRow7, toolbar);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
