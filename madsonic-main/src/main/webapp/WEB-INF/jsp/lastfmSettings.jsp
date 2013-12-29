<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1" %>

<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>
	
	<link href="<c:url value="/style/customScrollbar.css"/>" rel="stylesheet">	
</head>
<body class="mainframe bgcolor1">

<div id="content_2" class="content_main">
<!-- CONTENT -->

<c:import url="settingsHeader.jsp">
    <c:param name="cat" value="lastfm"/>
    <c:param name="toast" value="${model.toast}"/>
    <c:param name="done" value="${model.done}"/>
    <c:param name="warn" value="${model.warn}"/>
    <c:param name="warnInfo" value="${model.warnInfo}"/>
    <c:param name="bug" value="${model.bug}"/>	
    <c:param name="bugInfo" value="${model.bugInfo}"/>	
</c:import>
<br>
<p class="forward"><a href="lastfmSettings.view?ScanNow"><fmt:message key="lastfmSettings.artistcover.title"/></a></p>
<p class="detail" style="padding-left: 20px;width:80%;white-space:normal;margin-top:-5px;"><fmt:message key="lastfmSettings.artistcover"/></p>
<br>
<p class="forward"><a href="lastfmSettings.view?ScanInfo"><fmt:message key="lastfmSettings.artistsummary.title"/> (Full)</a></p>
<p class="detail" style="padding-left: 20px;width:80%;white-space:normal;margin-top:-5px;"><fmt:message key="lastfmSettings.artistsummaryinfo1"/></p>
<p class="detail" style="padding-left: 20px;width:80%;white-space:normal;margin-top:-5px;"><fmt:message key="lastfmSettings.artistsummaryinfo2"/></p>
<br>
<p class="forward"><a href="lastfmSettings.view?ScanNewInfo"><fmt:message key="lastfmSettings.artistsummary.title"/> (only new)</a></p>
<p class="detail" style="padding-left: 20px;width:80%;white-space:normal;margin-top:-5px;"><fmt:message key="lastfmSettings.artistsummaryinfo1"/></p>
<p class="detail" style="padding-left: 20px;width:80%;white-space:normal;margin-top:-5px;"><fmt:message key="lastfmSettings.artistsummaryinfo2"/></p>
<br>
<p class="forward"><a href="lastfmSettings.view?CleanupArtist">LastFM Artist Cleanup</a></p>
<p class="detail" style="padding-left: 20px;width:80%;white-space:normal;margin-top:-5px;">Cleanup unknown/incomplete artist entries</p>
<br>
<p>
<!-- deprecated 
<select name="LastFMLocale">
<option>EN</option>
<option>DE</option>
<option>FR</option>
</select>
Select your LastFM Language </p>
<br>
-->
<c:if test="${not empty model.error}">
    <p class="warning"><fmt:message key="${model.error}"/></p>
</c:if>

<c:if test="${model.reload}">
    <script language="javascript" type="text/javascript">parent.frames.upper.location.href="top.view?"</script>
    <script language="javascript" type="text/javascript">parent.frames.left.location.href="left.view?"</script>
</c:if>

<!-- CONTENT -->
</div>

</body></html>