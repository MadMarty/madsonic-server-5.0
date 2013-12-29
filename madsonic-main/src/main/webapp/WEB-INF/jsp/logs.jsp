<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>

<!--	
    <c:if test="${model.customScrollbar}">
	<style type="text/css">
		.content_main{position:absolute; left:0px; top:0px; margin-left:10px; margin-top:5px; width:99%; height:95%; padding:0 0;overflow:auto;}
	</style>
	<script type="text/javascript" src="<c:url value="/script/jquery.mousewheel.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/jquery.mCustomScrollbar.js"/>"></script>
    </c:if>		
-->	
    <script type="text/javascript" src="<c:url value="/script/scripts.js"/>"></script>
</head>
<body class="mainframe bgcolor1">

<!-- content block -->

<div id="content_2" class="content_main">
<!-- CONTENT -->
<h1>
    <img src="<spring:theme code="helpImage"/>" alt="">
    <fmt:message key="log.title"><fmt:param value="${model.brand}"/></fmt:message>
</h1>

<c:if test="${model.user.adminRole}">
    
<h2><img src="<spring:theme code="logImage"/>" alt="">&nbsp;<fmt:message key="help.log"/></h2>

<c:if test="${model.logReverse}">
<div class="forward"><a href="log.view?"><fmt:message key="common.refresh"/></a></div>
</c:if>

<table style="border-collapse:collapse; white-space:nowrap; border-spacing:2px;margin-top:5px;margin-bottom:5px" cellpadding="2" class="detailmini">
    <c:forEach items="${model.logEntries}" var="entry">
        <tr> 
            <td>[<fmt:formatDate value="${entry.date}" dateStyle="short" timeStyle="short" type="both"/>]</td>
			
			<c:if test="${entry.level eq 'INFO'}">
            <td class="bgcolor2" style="color: #298A08;">${entry.level}</td><td>${entry.category}</td><td>${entry.message}</td>
			</c:if>

			<c:if test="${entry.level eq 'DEBUG'}">
            <td class="bgcolor2" style="color: #0084C1;">${entry.level}</td><td>${entry.category}</td><td>${entry.message}</td>
			</c:if>

			<c:if test="${entry.level eq 'WARN'}">
            <td class="bgcolor2" style="color: #DF7401;">${entry.level}</td><td>${entry.category}</td><td>${entry.message}</td>
			</c:if>

			<c:if test="${entry.level eq 'ERROR'}">
            <td class="bgcolor2" style="color: #FF0303;">${entry.level}</td><td>${entry.category}</td><td>${entry.message}</td>
			</c:if>

        </tr>
    </c:forEach>
</table>

<p><fmt:message key="log.logfile"><fmt:param value="${model.logFile}"/></fmt:message> </p>

<c:if test="${not model.logReverse}">
<div class="forward"><a href="log.view?"><fmt:message key="common.refresh"/></a></div>
</c:if>

</c:if>

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