// Generated code from Butter Knife. Do not modify!
package com.shenkar.aroundme;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class RegistrationActivity$$ViewBinder<T extends com.shenkar.aroundme.RegistrationActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296384, "field 'fname'");
    target.fname = finder.castView(view, 2131296384, "field 'fname'");
    view = finder.findRequiredView(source, 2131296383, "field 'email'");
    target.email = finder.castView(view, 2131296383, "field 'email'");
    view = finder.findRequiredView(source, 2131296385, "field 'pword'");
    target.pword = finder.castView(view, 2131296385, "field 'pword'");
  }

  @Override public void unbind(T target) {
    target.fname = null;
    target.email = null;
    target.pword = null;
  }
}
