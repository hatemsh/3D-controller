attribute vec4 a_Position;
attribute vec3 a_Normal;

 uniform mat4 u_Matrix;
 uniform mat4 u_View_Matrix;

 varying float v_Color;


 void main()
 {
     vec3 position = vec3(u_View_Matrix * a_Position);
     vec3 normal = vec3(u_View_Matrix * vec4(-a_Normal,0.0));

     float dist = length(position) / 20.0;

     vec3 light = normalize(vec3(0.0,0.0,-1.0));

     float l = dot(light, normal);

     l = l / 3.0 + 0.666;

     v_Color = l - dist;

     gl_Position = u_Matrix * a_Position;
 }