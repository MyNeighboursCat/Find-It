// Generated by view binder compiler. Do not edit!
package com.myapp.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.myapp.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class GamePausedSwipeOrRotateDialogBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button abandonButton;

  @NonNull
  public final LinearLayout gamePausedDialogRoot;

  @NonNull
  public final Button resumeButton;

  private GamePausedSwipeOrRotateDialogBinding(@NonNull LinearLayout rootView,
      @NonNull Button abandonButton, @NonNull LinearLayout gamePausedDialogRoot,
      @NonNull Button resumeButton) {
    this.rootView = rootView;
    this.abandonButton = abandonButton;
    this.gamePausedDialogRoot = gamePausedDialogRoot;
    this.resumeButton = resumeButton;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static GamePausedSwipeOrRotateDialogBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static GamePausedSwipeOrRotateDialogBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.game_paused_swipe_or_rotate_dialog, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static GamePausedSwipeOrRotateDialogBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.abandonButton;
      Button abandonButton = ViewBindings.findChildViewById(rootView, id);
      if (abandonButton == null) {
        break missingId;
      }

      LinearLayout gamePausedDialogRoot = (LinearLayout) rootView;

      id = R.id.resumeButton;
      Button resumeButton = ViewBindings.findChildViewById(rootView, id);
      if (resumeButton == null) {
        break missingId;
      }

      return new GamePausedSwipeOrRotateDialogBinding((LinearLayout) rootView, abandonButton,
          gamePausedDialogRoot, resumeButton);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}