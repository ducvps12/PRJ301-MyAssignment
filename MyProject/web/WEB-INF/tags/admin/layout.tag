<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ attribute name="title" required="false" %>
<jsp:include page="/WEB-INF/views/common/_admin_header.jsp" />
<jsp:include page="/WEB-INF/views/common/_admin_sidebar.jsp" />

<main class="main"><jsp:doBody/></main>

<jsp:include page="/WEB-INF/views/common/_admin_footer.jsp" />
