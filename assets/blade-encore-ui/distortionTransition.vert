attribute vec4 a_position;
attribute vec2 a_texCoord0;

varying vec2 v_texCoord;
varying vec2 left_coord;
varying vec2 right_coord;
varying vec2 above_coord;
varying vec2 below_coord;
varying vec2 lefta_coord;
varying vec2 righta_coord;
varying vec2 leftb_coord;
varying vec2 rightb_coord;

uniform vec2 u_resolution;

void main() {
    v_texCoord = a_texCoord0;
    
    vec2 d = 1.0 / u_resolution;

    left_coord   = clamp(v_texCoord + vec2(-d.x,  0.0), 0.0, 1.0);
    right_coord  = clamp(v_texCoord + vec2( d.x,  0.0), 0.0, 1.0);
    above_coord  = clamp(v_texCoord + vec2( 0.0,  d.y), 0.0, 1.0);
    below_coord  = clamp(v_texCoord + vec2( 0.0, -d.y), 0.0, 1.0);

    lefta_coord  = clamp(v_texCoord + vec2(-d.x,  d.y), 0.0, 1.0);
    righta_coord = clamp(v_texCoord + vec2( d.x,  d.y), 0.0, 1.0);
    leftb_coord  = clamp(v_texCoord + vec2(-d.x, -d.y), 0.0, 1.0);
    rightb_coord = clamp(v_texCoord + vec2( d.x, -d.y), 0.0, 1.0);

    gl_Position = a_position;
}
