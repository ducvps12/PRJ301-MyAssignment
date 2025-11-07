<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/_taglibs.jsp" %>

<jsp:include page="/WEB-INF/views/common/_admin_header.jsp"/>

<c:set var="cp" value="${pageContext.request.contextPath}"/>
<fmt:setLocale value="vi_VN"/>

<style>
  .wrap{max-width:1100px;margin:16px auto;padding:0 16px}
  .toolbar{display:flex;gap:8px;align-items:center;margin-bottom:12px;flex-wrap:wrap}
  .inline{display:flex;gap:8px;align-items:center}
  .input{border:1px solid #e5e7eb;border-radius:10px;padding:8px 10px;min-width:90px}
  .btn{border:1px solid #334155;background:#334155;color:#fff;border-radius:10px;padding:8px 14px;cursor:pointer}
  .btn.pri{background:#2563eb;border-color:#2563eb}
  .panel{background:#fff;border:1px solid #e5e7eb;border-radius:14px;padding:14px}
  .table{width:100%;border-collapse:collapse}
  .table th,.table td{border-bottom:1px solid #e5e7eb;padding:10px;text-align:left}
  .table thead th{background:#f8fafc;font-weight:600}
  .empty{padding:18px;border:1px dashed #cbd5e1;border-radius:12px;color:#475569;background:#f8fafc}
  tfoot td{font-weight:700;background:#fafafa}
</style>

<div class="wrap">
  <div class="toolbar">
    <form method="get" class="inline" action="${cp}/payroll">
      <input class="input" type="number" name="y" value="${y}" style="width:110px" />
      <input class="input" type="number" name="m" value="${m}" style="width:90px" />
      <button class="btn">Xem</button>
    </form>

    <form method="post" action="${cp}/payroll/run" class="inline">
      <input type="hidden" name="csrf" value="${csrf}" />
      <input type="hidden" name="y" value="${y}" />
      <input type="hidden" name="m" value="${m}" />
      <button class="btn pri">Tạo/Tính bảng lương</button>
    </form>
  </div>

  <c:choose>
    <c:when test="${empty runId}">
      <div class="empty">Chưa có kỳ lương cho tháng này. Nhấn “Tạo/Tính bảng lương”.</div>
    </c:when>

    <c:otherwise>
      <div class="panel">
        <table class="table">
          <thead>
          <tr>
            <th>Nhân viên</th>
            <th>Cơ bản</th>
            <th>Phụ cấp</th>
            <th>OT</th>
            <th>Thưởng</th>
            <th>Phạt</th>
            <th>Bảo hiểm</th>
            <th>Thuế</th>
            <th>Thực lĩnh</th>
          </tr>
          </thead>
          <tbody>
          <c:set var="sumBase" value="0"/><c:set var="sumAllow" value="0"/>
          <c:set var="sumOt" value="0"/><c:set var="sumBonus" value="0"/>
          <c:set var="sumPen" value="0"/><c:set var="sumIns" value="0"/>
          <c:set var="sumTax" value="0"/><c:set var="sumNet" value="0"/>

          <c:forEach var="i" items="${items}">
            <!-- Hỗ trợ cả object lẫn map -->
            <c:set var="uid"   value="${empty i.userId ? (empty i['user_id'] ? null : i['user_id']) : i.userId}"/>
            <c:set var="name"  value="${empty i.fullName ? (empty i.full_name ? i['full_name'] : i.full_name) : i.fullName}"/>

            <c:set var="base"  value="${empty i.baseSalary ? (empty i['base_salary'] ? 0 : i['base_salary']) : i.baseSalary}"/>
            <c:set var="allow" value="${empty i.allowance  ? (empty i['allowance']   ? 0 : i['allowance'])   : i.allowance}"/>
            <c:set var="ot"    value="${empty i.otPay      ? (empty i['ot_pay']      ? 0 : i['ot_pay'])      : i.otPay}"/>
            <c:set var="bonus" value="${empty i.bonus      ? (empty i['bonus']       ? 0 : i['bonus'])       : i.bonus}"/>
            <c:set var="pen"   value="${empty i.penalty    ? (empty i['penalty']     ? 0 : i['penalty'])     : i.penalty}"/>
            <c:set var="ins"   value="${empty i.insurance  ? (empty i['insurance']   ? 0 : i['insurance'])   : i.insurance}"/>
            <c:set var="tax"   value="${empty i.tax        ? (empty i['tax']         ? 0 : i['tax'])         : i.tax}"/>
            <c:set var="net"   value="${empty i.netPay     ? (empty i['net_pay']     ? 0 : i['net_pay'])     : i.netPay}"/>

            <tr>
              <td>
                <c:choose>
                  <c:when test="${not empty name}">
                    <c:out value="${name}"/> (<c:out value="${uid}"/>)
                  </c:when>
                  <c:otherwise>
                    #<c:out value="${uid}"/>
                  </c:otherwise>
                </c:choose>
              </td>
              <td><fmt:formatNumber value="${base}"  type="currency"/></td>
              <td><fmt:formatNumber value="${allow}" type="currency"/></td>
              <td><fmt:formatNumber value="${ot}"    type="currency"/></td>
              <td><fmt:formatNumber value="${bonus}" type="currency"/></td>
              <td><fmt:formatNumber value="${pen}"   type="currency"/></td>
              <td><fmt:formatNumber value="${ins}"   type="currency"/></td>
              <td><fmt:formatNumber value="${tax}"   type="currency"/></td>
              <td><strong><fmt:formatNumber value="${net}" type="currency"/></strong></td>
            </tr>

            <!-- Cộng dồn -->
            <c:set var="sumBase"  value="${sumBase  + base}"/>
            <c:set var="sumAllow" value="${sumAllow + allow}"/>
            <c:set var="sumOt"    value="${sumOt    + ot}"/>
            <c:set var="sumBonus" value="${sumBonus + bonus}"/>
            <c:set var="sumPen"   value="${sumPen   + pen}"/>
            <c:set var="sumIns"   value="${sumIns   + ins}"/>
            <c:set var="sumTax"   value="${sumTax   + tax}"/>
            <c:set var="sumNet"   value="${sumNet   + net}"/>
          </c:forEach>
          </tbody>

          <tfoot>
          <tr>
            <td>Tổng</td>
            <td><fmt:formatNumber value="${sumBase}"  type="currency"/></td>
            <td><fmt:formatNumber value="${sumAllow}" type="currency"/></td>
            <td><fmt:formatNumber value="${sumOt}"    type="currency"/></td>
            <td><fmt:formatNumber value="${sumBonus}" type="currency"/></td>
            <td><fmt:formatNumber value="${sumPen}"   type="currency"/></td>
            <td><fmt:formatNumber value="${sumIns}"   type="currency"/></td>
            <td><fmt:formatNumber value="${sumTax}"   type="currency"/></td>
            <td><fmt:formatNumber value="${sumNet}"   type="currency"/></td>
          </tr>
          </tfoot>
        </table>
      </div>
    </c:otherwise>
  </c:choose>
</div>
