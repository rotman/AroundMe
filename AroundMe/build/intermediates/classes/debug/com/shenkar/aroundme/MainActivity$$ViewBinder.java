// Generated code from Butter Knife. Do not modify!
package com.shenkar.aroundme;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class MainActivity$$ViewBinder<T extends com.shenkar.aroundme.MainActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296371, "field 'fab'");
    target.fab = finder.castView(view, 2131296371, "field 'fab'");
    view = finder.findRequiredView(source, 2131296370, "field 'listView'");
    target.listView = finder.castView(view, 2131296370, "field 'listView'");
    view = finder.findRequiredView(source, 2131296372, "field 'tvEmpty'");
    target.tvEmpty = finder.castView(view, 2131296372, "field 'tvEmpty'");
  }

  @Override public void unbind(T target) {
    target.fab = null;
    target.listView = null;
    target.tvEmpty = null;
  }
}
