// Generated code from Butter Knife. Do not modify!
package com.shenkar.aroundme;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class MessageActivity$$ViewBinder<T extends com.shenkar.aroundme.MessageActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296379, "field 'sendMsgEditText'");
    target.sendMsgEditText = finder.castView(view, 2131296379, "field 'sendMsgEditText'");
    view = finder.findRequiredView(source, 2131296376, "field 'relativeLayout'");
    target.relativeLayout = finder.castView(view, 2131296376, "field 'relativeLayout'");
    view = finder.findRequiredView(source, 2131296377, "field 'lv'");
    target.lv = finder.castView(view, 2131296377, "field 'lv'");
  }

  @Override public void unbind(T target) {
    target.sendMsgEditText = null;
    target.relativeLayout = null;
    target.lv = null;
  }
}
