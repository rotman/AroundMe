// Generated code from Butter Knife. Do not modify!
package com.shenkar.aroundme;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class SettingsActivity$$ViewBinder<T extends com.shenkar.aroundme.SettingsActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296389, "field 'listView'");
    target.listView = finder.castView(view, 2131296389, "field 'listView'");
  }

  @Override public void unbind(T target) {
    target.listView = null;
  }
}
