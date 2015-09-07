// Generated code from Butter Knife. Do not modify!
package com.shenkar.aroundme;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class RegisterWithGPlusActivity$$ViewBinder<T extends com.shenkar.aroundme.RegisterWithGPlusActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296382, "field 'progressTextView'");
    target.progressTextView = finder.castView(view, 2131296382, "field 'progressTextView'");
    view = finder.findRequiredView(source, 2131296381, "field 'mProgress'");
    target.mProgress = finder.castView(view, 2131296381, "field 'mProgress'");
  }

  @Override public void unbind(T target) {
    target.progressTextView = null;
    target.mProgress = null;
  }
}
