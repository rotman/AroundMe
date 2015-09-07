// Generated code from Butter Knife. Do not modify!
package com.shenkar.aroundme;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class LoginActivity$$ViewBinder<T extends com.shenkar.aroundme.LoginActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296366, "field 'email'");
    target.email = finder.castView(view, 2131296366, "field 'email'");
    view = finder.findRequiredView(source, 2131296367, "field 'pword'");
    target.pword = finder.castView(view, 2131296367, "field 'pword'");
  }

  @Override public void unbind(T target) {
    target.email = null;
    target.pword = null;
  }
}
