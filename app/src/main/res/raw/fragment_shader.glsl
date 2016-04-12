precision mediump float;

varying float v_Color;

void main()
{

    gl_FragColor = vec4(vec3(v_Color) ,1.0);
}