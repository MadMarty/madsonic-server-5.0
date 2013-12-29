<%--@elvariable id="model" type="java.util.Map"--%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1" %>

<html>
<head>
    <%@ include file="head.jsp" %>
    <script type="text/javascript" src="<c:url value="/script/jwplayer.js"/>"></script> 
    <script type="text/javascript" src="<c:url value="/script/jwplayer.html5.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/swfobject.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/prototype.js"/>"></script>

    <sub:url value="/coverArt.view" var="coverArtUrl">
        <c:if test="${not empty model.coverArt}">
            <sub:param name="path" value="${model.coverArt.path}"/>
        </c:if>
        <sub:param name="size" value="200"/>
    </sub:url>

    <meta name="og:title" content="${fn:escapeXml(model.songs[0].artist)} &mdash; ${fn:escapeXml(model.songs[0].albumName)}"/>
    <meta name="og:type" content="album"/>
    <meta name="og:image" content="${model.redirectFrom}${coverArtUrl}"/>

	<script type="text/javascript">
	var is_android = false;
	  
	if (navigator.userAgent.match(/android/i)) {
		href = "madsonic://madsonic/get/share/" + "${model.share.name}";
		is_android = true;
	}
	if (is_android) {
		location.href = href;
	}
	</script>	
	
    <script type="text/javascript">
        function init() {
		jwplayer('player1').setup({
                'autostart': 'true',
                'bufferlength': 3,
				'logo.timeout': 1,
                'playlist.size': '300',
				'controlbar.forcenextprev': 'true',
                'playlistposition': 'bottom',
				'repeat': 'list',
				'width': ${model.popout ? '100%' : '600'},
				'height': ${model.popout ? '85%' : '560'},
				'playlist': [
				<c:forEach items="${model.songs}" var="song" varStatus="loopStatus">
					<%--@elvariable id="song" type="net.sourceforge.subsonic.domain.MediaFile"--%>
					<sub:url value="/stream" var="streamUrl">
						<sub:param name="path" value="${song.path}"/>
						<sub:param name="player" value="${model.player}"/>
					</sub:url>
					<sub:url value="/coverArt.view" var="coverUrl">
						<sub:param name="size" value="200"/>
						<c:if test="${not empty model.coverArts[loopStatus.count - 1]}">
							<sub:param name="path" value="${model.coverArts[loopStatus.count - 1].path}"/>
						</c:if>
					</sub:url>
						<!-- TODO: Use video provider for aac, m4a -->
					{
						file: "${streamUrl}",
						image: "${coverUrl}",
						title: "${fn:escapeXml(song.title)}",
						type: "${song.video ? "webm" : "mp3"}",
						description: "${fn:escapeXml(song.artist)}"<c:if test="${not empty song.durationSeconds}">,
						duration: ${song.durationSeconds}</c:if>
					},
				</c:forEach> ],
				'modes.type': 'html5'
				});
	    jwplayer().onReady(playerReady);
		}

        function playerReady(thePlayer) {
            jwplayer().onTime(timeListener);
			<c:if test="${not (model.trial and model.trialExpired)}">
            play();
			</c:if>
			}
			function play() {

            jwplayer().play();
        }
		
		function timeListener(obj) {
	        position = Math.floor(jwplayer().getPosition());
            updatePosition();
        }

        function updatePosition() {
            var pos = getPosition();

            var minutes = Math.round(pos / 60);
            var seconds = pos % 60;

            var result = minutes + ":";
            if (seconds < 10) {
                result += "0";
            }
            result += seconds;
            $("position").innerHTML = result;
        }

        function changeTimeOffset() {
            timeOffset = $("timeOffset").getValue();
            play();
        }

        function getPosition() {
            return parseInt(timeOffset) + parseInt(position);
        }
		
    </script>
</head>

<body class="mainframe bgcolor1" style="padding-top:2em" onload="init();">

<div style="margin:auto;width:500px">
    <h1 >${model.songs[0].artist}</h1>
    <div style="float:left;padding-right:1.5em">
        <h2 style="margin:0;">${model.songs[0].albumName}</h2>
    </div>
    <div class="detail" style="float:right">Streaming by <a href="http://madsonic.org/" target="_blank"><b>Madsonic</b></a></div>

    <div style="clear:both;padding-top:1em">
	<div id="wrapper" style="padding-top:1em">
		<div id="player1">HTML5 player</div>
	</div>
    </div>
    <div style="padding-top: 2em">${fn:escapeXml(model.share.description)}</div>
</div>
</body>
</html>
