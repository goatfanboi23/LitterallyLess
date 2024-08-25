package software.enginer.litterallyless.opengl;

import android.opengl.GLSurfaceView;

import software.enginer.litterallyless.opengl.renderers.SampleRender;

/** Interface to be implemented for rendering callbacks. */
public interface Renderer {
    /**
     * Called by {@link SampleRender} when the GL render surface is created.
     *
     * <p>See {@link GLSurfaceView.Renderer#onSurfaceCreated}.
     */
    void onSurfaceCreated(SampleRender render);

    /**
     * Called by {@link SampleRender} when the GL render surface dimensions are changed.
     *
     * <p>See {@link GLSurfaceView.Renderer#onSurfaceChanged}.
     */
    void onSurfaceChanged(SampleRender render, int width, int height);

    /**
     * Called by {@link SampleRender} when a GL frame is to be rendered.
     *
     * <p>See {@link GLSurfaceView.Renderer#onDrawFrame}.
     */
    void onDrawFrame(SampleRender render);
}
