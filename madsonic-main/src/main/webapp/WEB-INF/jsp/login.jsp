<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
	<meta name="viewport" content="width=device-width, user-scalable=no, minimum-scale=1.0, maximum-scale=1.0">
    <%@ include file="head.jsp" %>
    <script type="text/javascript">
        if (window != window.top) {
            top.location.href = location.href;
        }
    </script>
	<style>
	body.splash canvas {
		position: fixed;
		top: 0;
		left: 0;
		width: auto;
		height: auto;
		/*height: 92.5213675214%;*/
		z-index: -999;
		opacity: 0.38;
	}
	
	#container {
		display: inline;
		margin-top: -70px;
		width: 100%;
		height: 100%;
		/*height: 92.6282051282%;
		min-height: 92.6282051282%;*/
	}
	.centered {
	    display: inline-block; vertical-align: middle; width: 60%;
	}

	#index-content div.centered {
		display: block;
		margin: 0 auto;
	}

	body.movie #index-content div.centered #title {
		width: 75%;		
	}

	.bgcolor2 {
		opacity: 0.88;
	}

	</style>
</head>

<body onLoad="document.getElementById('j_username').focus(); start();" class="mainframe splash"> 

<div id="startup">
  <div id="container">
	<center>
		<div class="centered">
			<form action="<c:url value="/j_acegi_security_check"/>" method="POST">
				<div class="bgcolor2" align="center" style="border:1px solid black; padding:20px 50px 20px 50px; margin-top:200px">
					<div style="margin-bottom:1em;max-width:50em;text-align:left;"><sub:wiki text="${model.loginMessage}"/></div> 
					<table>
					<tr>
						<td colspan="2" align="left" style="padding-bottom:10px">
							<img src="<spring:theme code="logoImage"/>" alt="">
						</td>
					</tr>
					<tr>
						<td align="left" style="padding-right:10px"><fmt:message key="login.username"/></td>
						<td align="left"><input type="text" id="j_username" name="j_username" style="width:12em" tabindex="1"></td>
					</tr>
					<tr>
						<td align="left" style="padding-bottom:10px"><fmt:message key="login.password"/></td>
						<td align="left" style="padding-bottom:10px"><input type="password" name="j_password" style="width:12em" tabindex="2"></td>
					</tr>
					<tr>
						<td align="left"><input name="submit" type="submit" value="<fmt:message key="login.login"/>" tabindex="4"></td>
						<td align="left" class="detail">
							<input type="checkbox" name="_acegi_security_remember_me" id="remember" class="checkbox" tabindex="3">
							<label for="remember"><fmt:message key="login.remember"/></label>
						</td>
					</tr>
						<tr>
							<td></td>
							<td align="left" class="detail"><a href="recover.view"><fmt:message key="login.recover"/></a></td>
						</tr>
						<c:if test="${model.logout}">
							<tr><td colspan="2" style="padding-top:10px"><b><fmt:message key="login.logout"/></b></td></tr>
						</c:if>
						<c:if test="${model.error}">
							<tr><td colspan="2" style="padding-top:10px"><b class="warning"><fmt:message key="login.error"/></b></td></tr>
						</c:if>
					</table>
					<c:if test="${model.insecure}">
						<p><b class="warning"><fmt:message key="login.insecure"><fmt:param value="${model.brand}"/></fmt:message></b></p>
					</c:if>
				</div>
			</form>
		</div>
	</center>
  </div>
</div>
<script type="text/javascript" src="<c:url value="/script/libs/three.min.js"/>"></script>
<!--<script type="text/javascript" src="<c:url value="/script/libs/stats.min.js"/>"></script> -->
<script type="text/javascript" src="<c:url value="/script/libs/Detector.js"/>"></script>
<script type="text/javascript" src="<c:url value="/script/libs/Splash.js"/>"></script>

<script id="oceanSurfaceVert" type="x-shader/x-vertex">
	varying vec3 v_objCoor;
	void main()	{
		v_objCoor = position;
		gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
	}
</script>
	
<script id="oceanSurfaceFrag" type="x-shader/x-fragment">
	uniform vec3 u_color;
	varying vec3 v_objCoor;
	float remap (float value, float initStart, float initEnd, float finalStart, float finalEnd)
	{
		float mapped = (( (value - initStart) *(finalEnd - finalStart) ) / (initEnd- initStart)) + finalStart;
		return mapped;
	}
	void main()	{
		vec3 cameraPos = vec3(-338.0, -48.0, 2000.0);
		float border = -100.0;

		float alpha = remap(v_objCoor.y+border, 0.0, cameraPos.z*3.0, 0.0, 1.0);
		float red = remap(v_objCoor.z, -500.0, 500.0, 0.8, 1.0);
		float green = remap(v_objCoor.z, -500.0, 500.0, 0.8, 1.0);
		
		gl_FragColor=vec4(red, green,248.0/256.0, alpha);
	}
</script> 
</body>
</html>
