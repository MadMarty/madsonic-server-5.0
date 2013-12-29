<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>

    <c:if test="${model.customScrollbar}">
	<style type="text/css">
		.content_main{position:absolute; left:0px; top:0px; margin-left:10px; margin-top:5px; width:99%; height:95%; padding:0 0;overflow:auto;}
	</style>
	<script type="text/javascript" src="<c:url value="/script/jquery.mousewheel.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/jquery.mCustomScrollbar.js"/>"></script>
    </c:if>	
	
		
<style type="text/css">
span.off {
    cursor: pointer;
    float:left;
    padding: 2px 6px;
    margin: 2px;
    background: #FFF;
    color: #000;
    -webkit-border-radius: 7px;
    -moz-border-radius: 7px;
    border-radius: 7px;
    border: solid 1px #CCC;
    -webkit-transition-duration: 0.1s;
    -moz-transition-duration: 0.1s;
    transition-duration: 0.1s;
    -webkit-user-select:none;
    -moz-user-select:none;
    -ms-user-select:none;
    user-select:none;
    white-space: nowrap;
}

span.on {
    cursor: pointer;
    float:left;
    padding: 2px 6px;
    margin: 2px;
    background: #F5C70F;
    color: #000;
    -webkit-border-radius: 7px;
    -moz-border-radius: 7px;
    border-radius: 7px;
    border: solid 1px #999;
    -webkit-transition-duration: 0.1s;
    -moz-transition-duration: 0.1s;
    transition-duration: 0.1s;
    -webkit-user-select:none;
    -moz-user-select:none;
    -ms-user-select:none;
    user-select:none;
    white-space: nowrap;
}

span.off:hover {
    background: #F5C70F;opacity:0.7;
    border: solid 1px #999;
    text-decoration: none;
	}
	</style>
	
	<script type="text/javascript">

function changeClass(elem, className1,className2) {
    elem.className = (elem.className == className1)?className2:className1;
}
function playGenreRadio() {
	var genres = new Array();
	var e = document.getElementsByTagName("span");
	for (var i = 0; i < e.length; i++) {
		if (e[i].className == "on") {
			genres.push(e[i].firstChild.data);
		}
	}
	var num = document.getElementsByName("GenreRadioPlayCount")[0].selectedIndex;
	var playcount = document.getElementsByName("GenreRadioPlayCount")[0].options[num].text;

	parent.playQueue.onPlayGenreRadio(genres, playcount);
}
	</script>
</head>
<body class="mainframe bgcolor1">

<!-- content block -->

<div id="content_2" class="content_main">
<!-- CONTENT -->

<h1>
	<img src="<spring:theme code="radioImage"/>" alt="">
	Radio
</h1>
<c:choose>
	<c:when test="${empty model.genres}">
		<p>Please scan your library before</a>.
	</c:when>
	<c:otherwise>
		<p>Choose one or more genres.</p>
<!--	<table>
		<tr>
		<td width="750"> -->
		<c:forEach items="${model.genres}" var="genre">
			<span class="off" onclick='changeClass(this,"on","off");'>${genre}</span>
		</c:forEach>
		<div style="clear:both"/>
		<br>

		<form>		
		<select name="GenreRadioPlayCount">
			<option>10</option>
			<option>15</option>
			<option>25</option>
			<option>50</option>
			<option>75</option>
			<option>100</option>
			<option>150</option>
			<option>200</option>
			</select>

		<input type="button" value="Play Genre Radio!" onClick="playGenreRadio();">
		</form>
<!--	</td>
		</tr>	
		</table> -->
	</c:otherwise>
</c:choose>
<!-- CONTENT -->
</div>
</body>

<c:if test="${model.customScrollbar}">
<script type="text/javascript">        
(function($){
	$(window).load(function(){
		$("#content_2").mCustomScrollbar({
			set_width:false, /*optional element width: boolean, pixels, percentage*/
			set_height:false, /*optional element height: boolean, pixels, percentage*/
			horizontalScroll:false, /*scroll horizontally: boolean*/
			scrollInertia:200, /*scrolling inertia: integer (milliseconds)*/
			scrollEasing:"easeOutCubic", /*scrolling easing: string*/
			mouseWheel:"auto", /*mousewheel support and velocity: boolean, "auto", integer*/
			autoDraggerLength:true, /*auto-adjust scrollbar dragger length: boolean*/
			scrollButtons:{ /*scroll buttons*/
				enable:true, /*scroll buttons support: boolean*/
				scrollType:"pixels", /*scroll buttons scrolling type: "continuous", "pixels"*/
				scrollSpeed:55, /*scroll buttons continuous scrolling speed: integer*/
				scrollAmount:250 /*scroll buttons pixels scroll amount: integer (pixels)*/
			},
			advanced:{
				updateOnBrowserResize:true, /*update scrollbars on browser resize (for layouts based on percentages): boolean*/
				updateOnContentResize:true, /*auto-update scrollbars on content resize (for dynamic content): boolean*/
				autoExpandHorizontalScroll:false /*auto expand width for horizontal scrolling: boolean*/
			},
			callbacks:{
				onScroll:function(){}, /*user custom callback function on scroll event*/
				onTotalScroll:function(){}, /*user custom callback function on bottom reached event*/
				onTotalScrollOffset:0 /*bottom reached offset: integer (pixels)*/
			}
		});
	});
})(jQuery);

$(".content_main").resize(function(e){
	$(".content_main").mCustomScrollbar("update");
});
</script>
</c:if>	
</html>