package com.kaka.annotation_processor;

class ViewInfo {
    private int viewId;
    private String viewName;

    public ViewInfo(int viewId, String viewName) {
        this.viewId = viewId;
        this.viewName = viewName;
    }

    public int getViewId() {
        return viewId;
    }

    public void setViewId(int viewId) {
        this.viewId = viewId;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}
