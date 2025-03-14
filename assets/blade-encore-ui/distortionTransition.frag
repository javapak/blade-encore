#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord;
varying vec2 left_coord;
varying vec2 right_coord;
varying vec2 above_coord;
varying vec2 below_coord;
varying vec2 lefta_coord;
varying vec2 righta_coord;
varying vec2 leftb_coord;
varying vec2 rightb_coord;

uniform sampler2D u_texture;
uniform sampler2D u_maskBuffer;
uniform sampler2D u_delayBuffer;

uniform float amt;
uniform float inputScale;
uniform float inputOffset;
uniform float inputLambda;

float gray(vec4 n) {
    return (n.r + n.g + n.b) / 3.0;
}

void main() {
    vec2 texCoord = v_texCoord;

    vec4 currentPixel = texture2D(u_texture, texCoord);
    vec4 prevPixel = texture2D(u_delayBuffer, texCoord);

    float diff = gray(prevPixel) - gray(currentPixel);

    vec2 offsetX = vec2(inputOffset / float(textureSize(u_texture, 0).x), 0.0);
    vec2 offsetY = vec2(0.0, inputOffset / float(textureSize(u_texture, 0).y));

    float gradX = gray(texture2D(u_texture, texCoord + offsetX)) - gray(texture2D(u_texture, texCoord - offsetX));
    float gradY = gray(texture2D(u_texture, texCoord + offsetY)) - gray(texture2D(u_texture, texCoord - offsetY));

    vec2 grad = vec2(gradX, gradY);
    float gradMag = length(grad) + inputLambda;

    vec2 distortion = (diff / gradMag) * grad * inputScale;

    vec2 distortedCoord = clamp(texCoord + distortion * amt, 0.0, 1.0);

    vec4 sampleColor = texture2D(u_texture, distortedCoord);

    gl_FragColor = sampleColor;
}
