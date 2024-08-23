package software.enginer.litterallyless.common.kt;

import android.graphics.Paint;

import com.google.ar.core.Pose;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import software.enginer.litterallyless.common.gl.Mesh;
import software.enginer.litterallyless.common.gl.SampleRender;
import software.enginer.litterallyless.common.gl.Shader;
import software.enginer.litterallyless.common.gl.VertexBuffer;
import software.enginer.litterallyless.ui.LabeledAnchor;

public class MyLabelRender {
    private static final int COORDS_BUFFER_SIZE = 2 * 4 * 4;
    private static final FloatBuffer NDC_QUAD_COORDS_BUFFER = ByteBuffer.allocateDirect(COORDS_BUFFER_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer().put(new float[]{
            /*0:*/
            -1.5f, -1.5f,
            /*1:*/
            1.5f, -1.5f,
            /*2:*/
            -1.5f, 1.5f,
            /*3:*/
            1.5f, 1.5f
    });
    /**
     * Vertex buffer data for texture coordinates.
     */
    private static final FloatBuffer SQUARE_TEX_COORDS_BUFFER = ByteBuffer.allocateDirect(COORDS_BUFFER_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer().put(new float[]{
            /*0:*/
            0f, 0f,
            /*1:*/
            1f, 0f,
            /*2:*/
            0f, 1f,
            /*3:*/
            1f, 1f
    });

    TextTextureCache cache = new TextTextureCache();

    Mesh mesh;
    Shader shader;

    public void onSurfaceCreated(SampleRender render) throws IOException {
        shader = Shader.createFromAssets(render, "shaders/label.vert", "shaders/label.frag", null)
                .setBlend(
                        Shader.BlendFactor.ONE, // ALPHA (src)
                        Shader.BlendFactor.ONE_MINUS_SRC_ALPHA // ALPHA (dest)
                )
                .setDepthTest(false)
                .setDepthWrite(false);

        VertexBuffer[] vertexBuffers = new VertexBuffer[]{
                new VertexBuffer(render, 2, NDC_QUAD_COORDS_BUFFER),
                new VertexBuffer(render, 2, SQUARE_TEX_COORDS_BUFFER),
        };
        mesh = new Mesh(render, Mesh.PrimitiveMode.TRIANGLE_STRIP, null, vertexBuffers);
    }

    float[] labelOrigin = new float[3];

    /**
     * Draws a label quad with text [label] at [pose]. The label will rotate to face [cameraPose] around the Y-axis.
     */
    public void draw(SampleRender render, float[] viewProjectionMatrix, LabeledAnchor labeledAnchor, Pose cameraPose){
        draw(render, viewProjectionMatrix, labeledAnchor.getAnchorPose(), cameraPose, labeledAnchor.getLabel(), labeledAnchor.getColor());
    }

    /**
     * Draws a label quad with text [label] at [pose]. The label will rotate to face [cameraPose] around the Y-axis.
     */
    public void draw(SampleRender render, float[] viewProjectionMatrix, Pose pose, Pose cameraPose, String label, Paint textPaint){
        labelOrigin[0] = pose.tx();
        labelOrigin[1] = pose.ty();
        labelOrigin[2] = pose.tz();
        shader
                .setMat4("u_ViewProjection", viewProjectionMatrix)
                .setVec3("u_LabelOrigin", labelOrigin)
                .setVec3("u_CameraPos", cameraPose.getTranslation())
                .setTexture("uTexture", cache.get(render, label, textPaint));
        render.draw(mesh, shader);
    }
}
