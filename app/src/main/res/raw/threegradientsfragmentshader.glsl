
precision mediump float;


uniform float uTime;
uniform vec2 uResolution;

vec3 rgbToHsv(vec3 rgb)
{
	float M = max(rgb.r,max(rgb.g,rgb.b));
	float m = min(rgb.r,min(rgb.g,rgb.b));
	float C = M - m;
	float Hp = 0.;
	if (C == 0.)
		Hp = 0.;
	else if (M == rgb.r)
		Hp = mod(((rgb.g - rgb.b)/C),6.);
	else if (M == rgb.g)
		Hp = ((rgb.b - rgb.r)/C) + 2.;
	else //if (M == rgb.b)
		Hp = ((rgb.r - rgb.g)/C) + 4.;
	float H = 60. * Hp;
	float S = 0.;
	if (M != 0.)
		S = C / M;
	return vec3(H,S,M);
}

vec3 hsvToRgb(vec3 hsv)
{
	hsv.x = mod(hsv.x,360.);
	float C = hsv.z * hsv.y;
	float Hp = hsv.x / 60.;
	float X = C * (1. - abs(mod(Hp,2.) - 1.));
	vec3 r1g1b1 = vec3(0.);
	if (Hp <= 1.)
		r1g1b1 = vec3(C,X,0.);
	else if (Hp <= 2.)
		r1g1b1 = vec3(X,C,0.);
	else if (Hp <= 3.)
		r1g1b1 = vec3(0.,C,X);
	else if (Hp <= 4.)
		r1g1b1 = vec3(0.,X,C);
	else if (Hp <= 5.)
		r1g1b1 = vec3(X,0.,C);
	else if (Hp <= 6.)
		r1g1b1 = vec3(C,0.,X);
	float m = hsv.z - C;
	return vec3(r1g1b1+m);
}

float cycleh(float h,float timx)
{
	return mod(h + ((timx) + 1.) ,360.);
}

vec4 gradVal(vec4 col1,vec4 col2,vec4 col3,float dist,float radius,float col2stop)
{
	float stop = (radius * col2stop);
	float amt = clamp(dist / stop,0.,1.);
	vec4 outcol = (1. - amt) * col1 + (amt) * col2;
	amt = clamp((dist - stop) / (radius - stop),0.,1.);
	outcol = (1. - amt) * outcol + (amt) * col3;
	return outcol;
}
void main(void)
{
	float uTimex = uTime*.1;
	vec2 position = (gl_FragCoord.xy / uResolution);

	vec2 source1 = vec2(0.1,0.1);	
	vec2 source2 = vec2(0.3,0.3);	
	vec2 source3 = vec2(0.7,0.6);
	source1.xy += cos(uTimex/1.);
	source2.y += cos(uTimex/5.);
	source3.x += cos(uTimex/5.);
	
	float d1 = (distance(position,source1));
	float d2 = (distance(position,source2));	
	float d3 = (distance(position,source3));
	
	float h0 = mod(cycleh(320.,uTimex),360.);
	float h1 = mod(cycleh(0.,uTimex) + cos(d1*20.) ,360.);
	float h2 = mod(cycleh(180.,uTimex) + cos(d2*30.) ,360.);
	float h3 = mod(cycleh(240.,uTimex) + cos(d3*25.) ,360.);
	
	vec4 g1rgb1 = vec4(hsvToRgb(vec3(h1,1.,1.)),1.);	
	vec4 g2rgb1 = vec4(hsvToRgb(vec3(h2,1.,1.)),1.);	
	vec4 g3rgb1 = vec4(hsvToRgb(vec3(h3,1.,1.)),1.);
	
	vec4 g1rgb2 = vec4(hsvToRgb(vec3(h1+180.,1.,1.)),1.);	
	vec4 g2rgb2 = vec4(hsvToRgb(vec3(h2+180.,1.,1.)),1.);	
	vec4 g3rgb2 = vec4(hsvToRgb(vec3(h3+180.,1.,1.)),1.);
	
	vec4 rgb0 = vec4(hsvToRgb(vec3(h0,1.,1.)),1.);
	//rgb0 = vec4(hsvToRgb(vec3(180.,1.,1.)),1.);
	float radius = 1.;
	
	vec4 rgb1 = gradVal(g1rgb1,g1rgb2,vec4(g1rgb2.rgb,0.),d1,radius,0.5);
	vec4 rgb2 = gradVal(g2rgb1,g2rgb2,vec4(g2rgb2.rgb,0.),d2,radius,0.5);
	vec4 rgb3 = gradVal(g3rgb1,g3rgb2,vec4(g3rgb2.rgb,0.),d3,radius,0.5);
	
	vec3 rgb = rgb0.rgb;
	rgb = (rgb * (1. - rgb1.a)) + (rgb1.rgb * rgb1.a);
	rgb = (rgb * (1. - rgb2.a)) + (rgb2.rgb * rgb2.a);
	rgb = (rgb * (1. - rgb3.a)) + (rgb3.rgb * rgb3.a);

    //rgb = vec3(0.,1.,0.);
    //vec3 hsv9 = rgbToHsv(rgb);
    //rgb = hsvToRgb(hsv9);

	gl_FragColor = clamp(vec4(rgb,1.),0.,1.);
}
