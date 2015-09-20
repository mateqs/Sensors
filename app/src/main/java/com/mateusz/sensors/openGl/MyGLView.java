package com.mateusz.sensors.openGl;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class MyGLView extends GLSurfaceView {
    private final MyGLRenderer renderer;

    MyGLView(Context context) {
        super(context);
        renderer = new MyGLRenderer(context);
        setRenderer(renderer);
    }

    public MyGLRenderer getRenderer(){
        return renderer;
    }
}